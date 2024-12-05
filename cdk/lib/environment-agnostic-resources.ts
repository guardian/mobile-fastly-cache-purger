import type { GuStackProps} from "@guardian/cdk/lib/constructs/core";
import {GuStack} from "@guardian/cdk/lib/constructs/core";
import {GuardianAwsAccounts} from "@guardian/private-infrastructure-config";
import type {App} from "aws-cdk-lib";
import {Repository, TagMutability} from "aws-cdk-lib/aws-ecr";
import { FederatedPrincipal, PolicyDocument, PolicyStatement, Role } from "aws-cdk-lib/aws-iam";
import { CfnOutput } from 'aws-cdk-lib/core'

export class EnvironmentAgnosticResources extends GuStack {
    constructor(scope: App, id: string, props: GuStackProps) {
        super(scope, id, props);
        const ecrRepository = new Repository(this, 'mobile-fastly-cache-purger-repo', {
            repositoryName: 'mobile-fastly-cache-purger',
            imageScanOnPush: true,
            imageTagMutability: TagMutability.IMMUTABLE
        })
        new Role(this, 'CIRole', {
            assumedBy:  new FederatedPrincipal(
                `arn:aws:iam::${GuardianAwsAccounts.Mobile}:oidc-provider/token.actions.githubusercontent.com`,
                {
                    "StringEquals": { "token.actions.githubusercontent.com:aud": "sts.amazonaws.com" },
                    "StringLike": { "token.actions.githubusercontent.com:sub": "repo:guardian/mobile-fastly-cache-purger:*"}
                },
                "sts:AssumeRoleWithWebIdentity"
            ),
            inlinePolicies: {
                ecrToken: new PolicyDocument({
                    statements: [
                        new PolicyStatement({
                            actions: ['ecr:GetAuthorizationToken'],
                            resources: ['*']
                        })
                    ]
                }),
                ecrUpload: new PolicyDocument({
                    statements: [
                        new PolicyStatement({
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
        new CfnOutput(this, "mobile-fastly-cache-purger-repository-arn", {
            value: ecrRepository.repositoryArn,
            exportName: "mobile-fastly-cache-purger-repository-arn",
        });
        new CfnOutput(this, "mobile-fastly-cache-purger-repository-name", {
            value: ecrRepository.repositoryName,
            exportName: "mobile-fastly-cache-purger-repository-name",
        });
    }
}


