import { GuRootExperimental } from '@guardian/cdk/lib/experimental/constructs/root';
import 'source-map-support/register';
import { MobileCapiFastlyCachePurger } from '../lib/mobile-capi-fastly-cache-purger';
import { MobileFaciaFastlyCachePurger } from '../lib/mobile-facia-fastly-cache-purger';

const app = new GuRootExperimental();

// CAPI Fastly Cache Purger
new MobileCapiFastlyCachePurger(app, 'MobileCapiFastlyCachePurger-CODE', {
	env: { region: 'eu-west-1' },
	stack: 'mobile-capi-fastly-cache-purger',
	stage: 'CODE',
});

// Facia Fastly Cache Purger
new MobileFaciaFastlyCachePurger(app, 'MobileFaciaFastlyCachePurger-CODE', {
	env: { region: 'eu-west-1' },
	stack: 'mobile-fastly-cache-purger',
	stage: 'CODE',
});

new MobileFaciaFastlyCachePurger(app, 'MobileFaciaFastlyCachePurger-PROD', {
	env: { region: 'eu-west-1' },
	stack: 'mobile-fastly-cache-purger',
	stage: 'PROD',
});
