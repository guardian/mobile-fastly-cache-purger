import type {GuStack} from "@guardian/cdk/lib/constructs/core";
import {Repository} from "aws-cdk-lib/aws-ecr";
import type { FunctionProps} from "aws-cdk-lib/aws-lambda";
import {Code, Function, Handler, Runtime} from "aws-cdk-lib/aws-lambda";

interface GuFunctionDockerProps  extends Omit<FunctionProps, "code" | "handler" | "runtime">{
    app: string;
    repositoryArn: string;
    repositoryName: string;
    imageTag: string;
    memorySize: number;
}
export class GuLambdaDockerFunction extends Function{

    constructor(scope: GuStack, id: string, props: GuFunctionDockerProps) {
        const defaultEnvironmentVariables = {
            STACK: scope.stack,
            STAGE: scope.stage,
            APP: props.app,
        };
         super(scope, id, {
             code: Code.fromEcrImage(
                 Repository.fromRepositoryAttributes(scope, 'mobile-fastly-cache-purger-ecr', {
                     repositoryArn: props.repositoryArn,
                     repositoryName: props.repositoryName,
                 }), {
                     tagOrDigest: props.imageTag,
                 }
             ),
             environment: {
                 ...props.environment,
                 ...defaultEnvironmentVariables,
             },
             runtime: Runtime.FROM_IMAGE,
             handler: Handler.FROM_IMAGE,
             ...props,
         }
        );
    }
}


