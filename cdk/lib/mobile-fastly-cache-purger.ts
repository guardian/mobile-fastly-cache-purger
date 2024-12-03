import type {GuStackProps} from '@guardian/cdk/lib/constructs/core';
import {GuStack} from '@guardian/cdk/lib/constructs/core';
import {GuLambdaFunction} from '@guardian/cdk/lib/constructs/lambda';
import {GuardianAwsAccounts} from '@guardian/private-infrastructure-config';
import type {App} from 'aws-cdk-lib';
import {Duration} from 'aws-cdk-lib';
import {Repository, TagMutability} from 'aws-cdk-lib/aws-ecr'
// eslint-disable-next-line import/no-namespace -- sdf
import * as iam from 'aws-cdk-lib/aws-iam';
import type { FunctionProps} from 'aws-cdk-lib/aws-lambda';
import {Code, Function, Handler, Runtime} from 'aws-cdk-lib/aws-lambda';
// eslint-disable-next-line import/no-namespace -- sdf
import * as lambdaEventSources from 'aws-cdk-lib/aws-lambda-event-sources';
import {Topic} from 'aws-cdk-lib/aws-sns';
import {SqsSubscription} from "aws-cdk-lib/aws-sns-subscriptions";
// eslint-disable-next-line import/no-namespace -- sdf
import * as sqs from 'aws-cdk-lib/aws-sqs';

interface GuFunctionDockerProps  extends Omit<FunctionProps, "code" | "handler" | "runtime">{
	app: string;
	repositoryArn: string;
}
export class GuLambdaDockerFunction extends Function {
	constructor(scope: GuStack, id: string, props: GuFunctionDockerProps) {
		const repository = Repository.fromRepositoryArn(scope, id, props.repositoryArn)
		const code = Code.fromEcrImage(repository)
		super(scope, id, {
			...props,
			code: code,
			runtime: Runtime.FROM_IMAGE,
			handler: Handler.FROM_IMAGE
		})
	}

}
export class MobileFastlyCachePurger extends GuStack {

	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);

		const faciaID = this.stage == "CODE" ? "StorageConsumerRole-1JWVQ2NTELFT7" : "StorageConsumerRole-1R9GQEVJIM323";

		const executionRole: iam.Role = new iam.Role(this, 'ExecutionRole', {
			assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com'),
			path: "/",
			inlinePolicies: {
				logs: new iam.PolicyDocument({
					statements: [
						new iam.PolicyStatement({
							actions: [ 'logs:CreateLogGroup' ],
							resources: [ `arn:aws:logs:eu-west-1:${this.account}:*` ]
						}),
						new iam.PolicyStatement({
							actions: [ 'logs:CreateLogStream', 'logs:PutLogEvents' ],
							resources: [ `arn:aws:logs:eu-west-1:${this.account}:log-group:/aws/lambda/*:*` ]
						})
					] }),
				Conf: new iam.PolicyDocument({
					statements: [
						new iam.PolicyStatement({
							actions: [ 'ssm:GetParametersByPath' ],
							resources: [ `arn:aws:ssm:${this.region}:${this.account}:parameter/cache-purger/${this.stage}` ]
						})
					] }),
				Assume: new iam.PolicyDocument({
					statements: [
						new iam.PolicyStatement({
							actions: ['sts:AssumeRole'],
							resources: [`arn:aws:iam::${GuardianAwsAccounts.CMSFronts}:role/facia-${this.stage}-${faciaID}`]
						})
					]
				})
			}
		})

		const ecrRepository = new Repository(this, 'mobile-fastly-cache-purger-repo', {
			repositoryName: 'mobile-fastly-cache-purger',
			imageScanOnPush: true,
			imageTagMutability: TagMutability.IMMUTABLE
		})

		new iam.Role(this, 'CIRole', {
			assumedBy:  new iam.FederatedPrincipal(
				`arn:aws:iam::${GuardianAwsAccounts.Mobile}:oidc-provider/token.actions.githubusercontent.com`,
				{
					"StringEquals": { "token.actions.githubusercontent.com:aud": "sts.amazonaws.com" },
					"StringLike": { "token.actions.githubusercontent.com:sub": "repo:guardian/mobile-fastly-cache-purger:*"}
				},
				"sts:AssumeRoleWithWebIdentity"
				),
			inlinePolicies: {
				ecrToken: new iam.PolicyDocument({
					statements: [
						new iam.PolicyStatement({
							actions: ['ecr:GetAuthorizationToken'],
							resources: ['*']
						})
					]
				}),
				ecrUpload: new iam.PolicyDocument({
					statements: [
						new iam.PolicyStatement({
							actions: [
								'ecr:CompleteLayerUpload',
								'ecr:UploadLayerPart',
								'ecr:InitiateLayerUpload',
								'ecr:BatchCheckLayerAvailability',
								'ecr:PutImage'
							],
							resources: [ecrRepository.repositoryArn]
						})
					]
				})

			}
		})

		const handler: GuLambdaFunction = new GuLambdaFunction(this, 'mobile-fastly-cache-purger', {
			handler: 'PurgerLambda::handleRequest',
			functionName: `mobile-fastly-cache-purger-cdk-${this.stage}`,
			timeout: Duration.seconds(60),
			environment: {
				App: 'mobile-fastly-cache-purger',
				Stack: this.stack,
				Stage: this.stage,
			},
			runtime: Runtime.JAVA_11,
			app: 'mobile-fastly-cache-purger',
			fileName: `mobile-fastly-cache-purger.jar`,
			role: executionRole,
		});

		// const lambdaFunction: GuLambdaDockerFunction = new GuLambdaDockerFunction(this, 'mobile-fastly-cache-purger', {
		// 	functionName: `mobile-fastly-cache-purger-cdk-${this.stage}`,
		// 	timeout: Duration.seconds(60),
		// 	environment: {
		// 		App: 'mobile-fastly-cache-purger',
		// 		Stack: this.stack,
		// 		Stage: this.stage,
		// 	},
		// 	app: 'mobile-fastly-cache-purger',
		// 	repositoryArn: 'arn',
		// 	role: executionRole,
		// });

		const dlq: sqs.Queue = new sqs.Queue(this, "frontsPurgeDlq")

		const queue: sqs.Queue = new sqs.Queue(this, "frontsPurgeSqs", {
			visibilityTimeout: Duration.seconds(70), 	//default for a queue is 30s, and the lambda is 60s
			deadLetterQueue: {
				maxReceiveCount: 3,
				queue: dlq,
			}
		});

		const frontsUpdateTopicName=
			this.stage == "CODE" ? "FrontsUpdateSNSTopic-RepwK3g95V3w" : "FrontsUpdateSNSTopic-kWN6oX2kvOmI";

		const frontsUpdateTopic = Topic.fromTopicArn(
			this,
			"FrontsUpdateSNSTopic",
			`arn:aws:sns:${this.region}:${GuardianAwsAccounts.CMSFronts}:facia-${this.stage}-${frontsUpdateTopicName}`
		)
		frontsUpdateTopic.addSubscription(new SqsSubscription(queue));

		const eventSource: lambdaEventSources.SqsEventSource = new lambdaEventSources.SqsEventSource(queue);

		handler.addEventSource(eventSource);
		// lambdaFunction.addEventSource(eventSource);
	}
}
