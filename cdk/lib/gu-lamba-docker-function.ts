import type {GuStack} from "@guardian/cdk/lib/constructs/core";
import {Repository} from "aws-cdk-lib/aws-ecr";
import type { FunctionProps} from "aws-cdk-lib/aws-lambda";
import {Code, Function, Handler, Runtime} from "aws-cdk-lib/aws-lambda";

interface GuFunctionDockerProps  extends Omit<FunctionProps, "code" | "handler" | "runtime">{
    app: string;
    repositoryArn: string;
    repositoryName: string;
}
export class GuLambdaDockerFunction extends Function {
    constructor(scope: GuStack, id: string, props: GuFunctionDockerProps) {

        super(scope, `${id}-`, {
            ...props,
            code: Code.fromEcrImage(Repository.fromRepositoryAttributes(scope, id, {
                repositoryArn: props.repositoryArn,
                repositoryName: props.repositoryName,
            })),
            runtime: Runtime.FROM_IMAGE,
            handler: Handler.FROM_IMAGE
        })


    }

}


