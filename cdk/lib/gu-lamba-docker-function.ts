import type {GuStack} from "@guardian/cdk/lib/constructs/core";
import {Repository} from "aws-cdk-lib/aws-ecr";
import type { FunctionProps} from "aws-cdk-lib/aws-lambda";
import {Code, DockerImageCode, DockerImageFunction, Function, Handler, Runtime} from "aws-cdk-lib/aws-lambda";

interface GuFunctionDockerProps  extends Omit<FunctionProps, "code" | "handler" | "runtime">{
    app: string;
    repositoryArn: string;
    repositoryName: string;
    imageTag: string;
}
export class GuLambdaDockerFunction extends DockerImageFunction{
    constructor(scope: GuStack, id: string, props: GuFunctionDockerProps) {
         super(scope, id, {
            code: DockerImageCode.fromEcr(
                Repository.fromRepositoryAttributes(scope, 'mobile-fastly-cache-purger-ecr', {
                    repositoryArn: props.repositoryArn,
                    repositoryName: props.repositoryName,
                }), {
                    tagOrDigest:  props.imageTag,
                }
            ),
        });
    }
}


