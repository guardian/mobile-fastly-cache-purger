import { GuRoot } from '@guardian/cdk/lib/constructs/root';
import 'source-map-support/register';
import { MobileFastlyCachePurger } from '../lib/mobile-facia-fastly-cache-purger';

const app = new GuRoot();

/**
 * GuRootExperimental will generate a `riff-raff.yaml` configuration file to deploy this project with Riff-Raff.
 *
 * @see https://github.com/guardian/cdk/blob/main/src/experimental/riff-raff-yaml-file/README.md
 */

// Facia Fastly Cache Purger
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
