import type {GuStack} from "@guardian/cdk/lib/constructs/core";
import {Repository} from "aws-cdk-lib/aws-ecr";
import type { FunctionProps} from "aws-cdk-lib/aws-lambda";
import {Code, Function, Handler, Runtime} from "aws-cdk-lib/aws-lambda";

interface GuFunctionDockerProps  extends Omit<FunctionProps, "code" | "handler" | "runtime">{
    app: string;
    repositoryArn: string;
    repositoryName: string;
    imageTag: string;
}
export class GuLambdaDockerFunction extends Function {
    constructor(scope: GuStack, id: string, props: GuFunctionDockerProps) {
        const repo = Code.fromEcrImage(Repository.fromRepositoryAttributes(scope, id, {
            repositoryArn: props.repositoryArn,
            repositoryName: props.repositoryName
        }), {
            tagOrDigest: props.imageTag
        })

        super(scope, `${id}-`, {
            ...props,
            code: repo,
            runtime: Runtime.FROM_IMAGE,
            handler: Handler.FROM_IMAGE
        })
    }
}


