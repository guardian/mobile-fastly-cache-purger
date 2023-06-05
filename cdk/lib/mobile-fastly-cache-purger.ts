import { join } from 'path';
import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import { GuLambdaFunction } from '@guardian/cdk/lib/constructs/lambda';
import type { App } from 'aws-cdk-lib';
import { Duration } from 'aws-cdk-lib';
import { Runtime } from 'aws-cdk-lib/aws-lambda';

export class MobileFastlyCachePurger extends GuStack {
	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);
		new GuLambdaFunction(this, 'mobile-fastly-cache-purger', {
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
		});
	}
}
