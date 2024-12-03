import {App} from "aws-cdk-lib";
import {MobileFastlyCachePurger} from "./mobile-fastly-cache-purger";
import {Template} from "aws-cdk-lib/assertions";
import {EnvironmentAgnosticResources} from "./environment-agnostic-resources";

describe('The EnvironmentAgnosticResources stack', () => {
    it('matches the snapshot', () => {
        const app = new App();
        const stack = new EnvironmentAgnosticResources(app, 'EnvironmentAgnosticResources', {
            stack: 'mobile-fastly-cache-purger',
            stage: 'TEST',
        });
        const template = Template.fromStack(stack);
        expect(template.toJSON()).toMatchSnapshot();
    });
})
