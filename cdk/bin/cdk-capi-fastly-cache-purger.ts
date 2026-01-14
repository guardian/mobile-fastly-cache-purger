import { RiffRaffYamlFile } from '@guardian/cdk/lib/riff-raff-yaml-file';
import { App } from 'aws-cdk-lib';
import 'source-map-support/register';
import { MobileCapiFastlyCachePurger } from '../lib/mobile-capi-fastly-cache-purger';

const app = new App({ outdir: 'cdk.out/mobile-capi-fastly-cache-purger' });

// CAPI Fastly Cache Purger
new MobileCapiFastlyCachePurger(app, 'MobileCapiFastlyCachePurger-CODE', {
	env: { region: 'eu-west-1' },
	stack: 'mobile',
	stage: 'CODE',
});

new RiffRaffYamlFile(app).synth();
