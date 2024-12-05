import type {GuStackProps} from '@guardian/cdk/lib/constructs/core';
import {GuStack} from '@guardian/cdk/lib/constructs/core';
import {GuLambdaFunction} from '@guardian/cdk/lib/constructs/lambda';
import {GuardianAwsAccounts} from '@guardian/private-infrastructure-config';
import type {App} from 'aws-cdk-lib';
import {CfnParameter, Duration, Fn} from 'aws-cdk-lib';
// eslint-disable-next-line import/no-namespace -- sdf
import * as iam from 'aws-cdk-lib/aws-iam';
import {Runtime} from 'aws-cdk-lib/aws-lambda';
// eslint-disable-next-line import/no-namespace -- sdf
import * as lambdaEventSources from 'aws-cdk-lib/aws-lambda-event-sources';
import {Topic} from 'aws-cdk-lib/aws-sns';
import {SqsSubscription} from "aws-cdk-lib/aws-sns-subscriptions";
// eslint-disable-next-line import/no-namespace -- sdf
import * as sqs from 'aws-cdk-lib/aws-sqs';
import {GuLambdaDockerFunction} from "./gu-lamba-docker-function";


export class MobileFastlyCachePurger extends GuStack {

	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);

		const faciaID = this.stage == "CODE" ? "StorageConsumerRole-1JWVQ2NTELFT7" : "StorageConsumerRole-1R9GQEVJIM323";
		const buildId = new CfnParameter(this, 'BuildId', {
			type: 'String',
			default: 'dev',
			description: 'Tag to be used for the image URL, e.g. riff raff build id',
		}).value.toString();

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

		const imageRepositoryArn = Fn.importValue('mobile-fastly-cache-purger-repository-arn')
		const imageRepositoryName = Fn.importValue('mobile-fastly-cache-purger-repository-name')
		const lambdaFunction: GuLambdaDockerFunction = new GuLambdaDockerFunction(this, 'mobile-fastly-cache-purger-v2', {
			functionName: `mobile-fastly-cache-purger-cdk-${this.stage}-v2`,
			timeout: Duration.seconds(60),
			environment: {
				App: 'mobile-fastly-cache-purger-v2',
				Stack: this.stack,
				Stage: this.stage,
			},
			app: 'mobile-fastly-cache-purger-v2',
			repositoryArn: `${imageRepositoryArn}`,
			repositoryName: `${imageRepositoryName}`,
			imageTag: `${buildId}`,
			role: executionRole,
			memorySize: 1024
		});

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
		lambdaFunction.addEventSource(eventSource);
	}
}
