import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { MobileFastlyCachePurger } from "./mobile-fastly-cache-purger";

describe("The MobileFastlyCachePurger stack", () => {
  it("matches the snapshot", () => {
    const app = new App();
    const stack = new MobileFastlyCachePurger(app, "MobileFastlyCachePurger", { stack: "mobile-fastly-cache-purger", stage: "TEST" });
    const template = Template.fromStack(stack);
    expect(template.toJSON()).toMatchSnapshot();
  });
});
