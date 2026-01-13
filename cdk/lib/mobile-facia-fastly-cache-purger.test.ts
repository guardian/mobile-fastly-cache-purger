import { App } from 'aws-cdk-lib';
import { Template } from 'aws-cdk-lib/assertions';
import { MobileFaciaFastlyCachePurger } from './mobile-facia-fastly-cache-purger';

describe('The MobileFaciaFastlyCachePurger stack', () => {
	it('matches the snapshot', () => {
		const app = new App();
		const stack = new MobileFaciaFastlyCachePurger(
			app,
			'MobileFaciaFastlyCachePurger',
			{
				stack: 'mobile-fastly-cache-purger',
				stage: 'TEST',
			},
		);
		const template = Template.fromStack(stack);
		expect(template.toJSON()).toMatchSnapshot();
	});
});
