import { GuRoot } from '@guardian/cdk/lib/constructs/root';
import 'source-map-support/register';
import { MobileFaciaFastlyCachePurger } from '../lib/mobile-facia-fastly-cache-purger';

const app = new GuRoot();

/**
 * GuRootExperimental will generate a `riff-raff.yaml` configuration file to deploy this project with Riff-Raff.
 *
 * @see https://github.com/guardian/cdk/blob/main/src/experimental/riff-raff-yaml-file/README.md
 */

// Facia Fastly Cache Purger
new MobileFaciaFastlyCachePurger(app, 'MobileFastlyCachePurger-CODE', {
	env: { region: 'eu-west-1' },
	stack: 'mobile-fastly-cache-purger',
	stage: 'CODE',
});

new MobileFaciaFastlyCachePurger(app, 'MobileFastlyCachePurger-PROD', {
	env: { region: 'eu-west-1' },
	stack: 'mobile-fastly-cache-purger',
	stage: 'PROD',
});
