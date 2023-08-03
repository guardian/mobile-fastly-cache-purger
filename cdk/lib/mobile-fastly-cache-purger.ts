import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import { GuLambdaFunction } from '@guardian/cdk/lib/constructs/lambda';
import type { App } from 'aws-cdk-lib';
import {Duration} from 'aws-cdk-lib';
import { Runtime } from 'aws-cdk-lib/aws-lambda';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as sqs from 'aws-cdk-lib/aws-sqs';
import * as lambdaEventSources from 'aws-cdk-lib/aws-lambda-event-sources';

export class MobileFastlyCachePurger extends GuStack {
	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);

		const executionRole = new iam.Role(this, 'ExecutionRole', {
			assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com'),
			path: "/",
			inlinePolicies: {
				Conf: new iam.PolicyDocument({
					statements: [
						new iam.PolicyStatement({
							actions: [ 'ssm:GetParametersByPath' ],
							resources: [ `arn:aws:ssm:${this.region}:${this.account}:parameter/cache-purger/${this.stage}` ]
						})
					] }),
			}
		})

		const handler = new GuLambdaFunction(this, 'mobile-fastly-cache-purger', {
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

		const queue = new sqs.Queue(this, "frontsPurgeSqs", {
			queueName: 'sqs',
		});

		const eventSource = new lambdaEventSources.SqsEventSource(queue);

		handler.addEventSource(eventSource);


	}
}
