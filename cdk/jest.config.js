module.exports = {
    setupFilesAfterEnv: ["./jest.setup.js"],
    transformIgnorePatterns: ["node_modules/(?!@guardian/private-infrastructure-config)"],
    testMatch: ["<rootDir>/lib/**/*.test.ts"],
    transform: {"^.+\\.tsx?$": "ts-jest"},
};

