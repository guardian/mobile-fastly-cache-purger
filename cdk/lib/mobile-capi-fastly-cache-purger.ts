import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import { GuLambdaFunction } from '@guardian/cdk/lib/constructs/lambda';
import type { App } from 'aws-cdk-lib';
import { Duration } from 'aws-cdk-lib';
import { Runtime } from 'aws-cdk-lib/aws-lambda';

export class MobileCapiFastlyCachePurger extends GuStack {
	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);

		new GuLambdaFunction(this, 'capi-fastly-cache-purger-lambda', {
			app: 'mobile-capi-fastly-cache-purger',
			functionName: `mobile-capi-fastly-cache-purger-${this.stage}`,
			fileName: 'mobile-capi-fastly-cache-purger-lambda.zip',
			handler: 'index.handler',
			runtime: Runtime.NODEJS_18_X,
			timeout: Duration.seconds(60),
			memorySize: 512,
		});
	}
}
