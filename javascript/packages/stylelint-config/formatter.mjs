/**
 * Stylelint does not allow multiple format and outputs simultaneously.
 * This custom formatter aggregates all the formatters we require under a single file.
 */

import fs from 'fs';
import path from 'path';

import stylelint from 'stylelint';
import junitFormatter from 'stylelint-junit-formatter';
import sarifFormatter from 'stylelint-sarif-formatter';

const INIT_CWD = process.env.INIT_CWD;
const jsonFormatter = stylelint.formatters.json;

const formatters = [
    { formatter: junitFormatter, destination: `${INIT_CWD}/build/stylelint/junit.xml` },
    { formatter: jsonFormatter, destination: `${INIT_CWD}/build/stylelint/report.json` },
    { formatter: sarifFormatter, destination: `${INIT_CWD}/build/stylelint/report.sarif` },
];

export default stylelint.formatters.string.then((stringFormatter) => {
    return function formatter(results, returnResult) {
        const formattedResults = results.map((suite) => ({
            ...suite,
            source: path.relative(`${import.meta.dirname}/../../`, suite.source),
        }));

        Promise.all(
            formatters.map(async (config) => {
                const formatter = await config.formatter;
                fs.mkdirSync(path.dirname(config.destination), { recursive: true, mode: 0o755 });
                fs.writeFileSync(
                    path.normalize(config.destination),
                    formatter(formattedResults, returnResult),
                    { encoding: 'utf8' },
                );
            }),
        );

        return stringFormatter(results, returnResult);
    };
});
