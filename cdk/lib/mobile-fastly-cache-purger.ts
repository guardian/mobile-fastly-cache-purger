import { join } from 'path';
import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import { GuLambdaFunction } from '@guardian/cdk/lib/constructs/lambda';
import type { App } from 'aws-cdk-lib';
import {CfnParameter, Duration} from 'aws-cdk-lib';
import { Runtime } from 'aws-cdk-lib/aws-lambda';

export class MobileFastlyCachePurger extends GuStack {
	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);

		const fastlyServiceId = new CfnParameter(this, 'FastlyServiceId', {
			type: 'String',
			description: 'Id of service to purge',
		});
		const fastlyAPIKey = new CfnParameter(this, 'FastlyAPIKey', {
			type: 'String',
			description:
				'API key with purge writes for service with id FastlyServiceId',
			noEcho: true,
		});

		new GuLambdaFunction(this, 'mobile-fastly-cache-purger', {
			handler: 'PurgerLambda::handleRequest',
			functionName: `mobile-fastly-cache-purger-cdk-${this.stage}`,
			timeout: Duration.seconds(60),
			environment: {
				FastlyServiceId: fastlyServiceId.valueAsString,
				FastlyAPIKey: fastlyAPIKey.valueAsString,
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
