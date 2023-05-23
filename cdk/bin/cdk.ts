import "source-map-support/register";
import { App } from "aws-cdk-lib";
import { MobileFastlyCachePurger } from "../lib/mobile-fastly-cache-purger";

const app = new App();
new MobileFastlyCachePurger(app, "MobileFastlyCachePurger-CODE", { stack: "mobile-fastly-cache-purger", stage: "CODE" });
