module.exports = {
    setupFilesAfterEnv: ["./jest.setup.js"],
    testMatch: ["<rootDir>/lib/**/*.test.ts"],
    transform: {"^.+\\.tsx?$": "ts-jest"},
};

