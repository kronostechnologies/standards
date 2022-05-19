/**
 * Stylelint does not allow multiple format and outputs simultaneously.
 * This custom formatter aggregates all the formatters we require under a single file.
 */

const junitFormatter = require('stylelint-junit-formatter');
const sarifFormatter = require('stylelint-sarif-formatter');
const { formatters: { string: stringFormatter, json: jsonFormatter } } = require('stylelint');
const fs = require('fs');
const path = require('path');

const formatters = [
    { formatter: junitFormatter, destination: 'build/stylelint/junit.xml' },
    { formatter: jsonFormatter, destination: 'build/stylelint/report.json' },
    { formatter: sarifFormatter, destination: 'build/stylelint/report.sarif' },
];

module.exports = (results, returnResult) => {
    const formattedResults = results.map((suite) => ({
        ...suite,
        source: path.relative(`${__dirname}/../../`, suite.source),
    }));

    formatters.forEach((config) => {
        fs.mkdirSync(path.dirname(config.destination), { recursive: true, mode: 0o755 });
        fs.writeFileSync(
            path.normalize(config.destination),
            config.formatter(formattedResults, returnResult),
            { encoding: 'utf8' },
        );
    });

    return stringFormatter(formattedResults, returnResult);
};
