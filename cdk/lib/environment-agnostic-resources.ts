import {Repository, TagMutability} from "aws-cdk-lib/aws-ecr";
import * as iam from "aws-cdk-lib/aws-iam";
import {GuardianAwsAccounts} from "@guardian/private-infrastructure-config";
import {GuStack, GuStackProps} from "@guardian/cdk/lib/constructs/core";
import {App} from "aws-cdk-lib";

export class EnvironmentAgnosticResources extends GuStack {
    constructor(scope: App, id: string, props: GuStackProps) {
        super(scope, id, props);
        const ecrRepository = new Repository(this, 'mobile-fastly-cache-purger-repo', {
            repositoryName: 'mobile-fastly-cache-purger',
            imageScanOnPush: true,
            imageTagMutability: TagMutability.IMMUTABLE
        })
        new iam.Role(this, 'CIRole', {
            assumedBy:  new iam.FederatedPrincipal(
                `arn:aws:iam::${GuardianAwsAccounts.Mobile}:oidc-provider/token.actions.githubusercontent.com`,
                {
                    "StringEquals": { "token.actions.githubusercontent.com:aud": "sts.amazonaws.com" },
                    "StringLike": { "token.actions.githubusercontent.com:sub": "repo:guardian/mobile-fastly-cache-purger:*"}
                },
                "sts:AssumeRoleWithWebIdentity"
            ),
            inlinePolicies: {
                ecrToken: new iam.PolicyDocument({
                    statements: [
                        new iam.PolicyStatement({
                            actions: ['ecr:GetAuthorizationToken'],
                            resources: ['*']
                        })
                    ]
                }),
                ecrUpload: new iam.PolicyDocument({
                    statements: [
                        new iam.PolicyStatement({
                            actions: [
                                'ecr:CompleteLayerUpload',
                                'ecr:UploadLayerPart',
                                'ecr:InitiateLayerUpload',
                                'ecr:BatchCheckLayerAvailability',
                                'ecr:PutImage'
                            ],
                            resources: [ecrRepository.repositoryArn]
                        })
                    ]
                })

            }
        })
    }
}


