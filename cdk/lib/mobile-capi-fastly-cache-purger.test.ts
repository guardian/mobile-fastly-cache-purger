import { App } from 'aws-cdk-lib';
import { Template } from 'aws-cdk-lib/assertions';
import { MobileCapiFastlyCachePurger } from './mobile-capi-fastly-cache-purger';

describe('The MobileCapiFastlyCachePurger stack', () => {
	it('matches the snapshot', () => {
		const app = new App();
		const stack = new MobileCapiFastlyCachePurger(
			app,
			'MobileCapiFastlyCachePurger',
			{
				stack: 'mobile-capi-fastly-cache-purger',
				stage: 'TEST',
			},
		);
		const template = Template.fromStack(stack);
		expect(template.toJSON()).toMatchSnapshot();
	});
});
