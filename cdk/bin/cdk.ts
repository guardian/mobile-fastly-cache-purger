import 'source-map-support/register';
import { GuRootExperimental } from '@guardian/cdk/lib/experimental/constructs/root';
import { MobileFastlyCachePurger } from '../lib/mobile-fastly-cache-purger';
import {EnvironmentAgnosticResources} from "../lib/environment-agnostic-resources";

const app = new GuRootExperimental();

new EnvironmentAgnosticResources(app, 'EnvironmentAgnosticResources', {
	env: { region: 'eu-west-1' },
	stack: 'mobile-fastly-cache-purger',
	stage: 'CODE'
})
new MobileFastlyCachePurger(app, 'MobileFastlyCachePurger-CODE', {
	env: { region: 'eu-west-1' },
	stack: 'mobile-fastly-cache-purger',
	stage: 'CODE',
});

new MobileFastlyCachePurger(app, 'MobileFastlyCachePurger-PROD', {
	env: { region: 'eu-west-1' },
	stack: 'mobile-fastly-cache-purger',
	stage: 'PROD',
});
