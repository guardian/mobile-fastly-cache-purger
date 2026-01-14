const baseConfig = {
	setupFilesAfterEnv: ['./jest.setup.js'],
	transform: {
		'^.+\\.tsx?$': [
			'ts-jest',
			{
				tsconfig: 'tsconfig.json',
			},
		],
	},
};

module.exports = {
	projects: [
		{
			...baseConfig,
			displayName: 'cdk-capi-fastly-cache-purger',
			testMatch: ['<rootDir>/lib/mobile-capi-fastly-cache-purger.test.ts'],
		},
		{
			...baseConfig,
			displayName: 'cdk-facia-fastly-cache-purger',
			testMatch: [
				'<rootDir>/lib/mobile-facia-fastly-cache-purger.test.ts',
				'<rootDir>/lib/environment-agnostic-resources.test.ts',
			],
		},
	],
};
