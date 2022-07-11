#!/usr/bin/env node

/**
 * ESLint does not allow multiple format and outputs simultaneously other than by its API.
 * This wrapper will create a JUnit formatted XML file, a fancy output to the console and a SARIF file.
 */

/* eslint-disable no-console */
/* eslint-disable @typescript-eslint/no-var-requires -- Waiting for ES modules support in PnP https://github.com/yarnpkg/berry/issues/638 */
const { ESLint } = require('eslint');
const fs = require('fs/promises');
const path = require('path');
const yargs = require('yargs/yargs');
const { hideBin } = require('yargs/helpers');

const argv = yargs(hideBin(process.argv))
    .usage('Usage: $0 [options] <pattern> ... <pattern>')
    .help('h')
    .alias('h', 'help')
    .option('config', { describe: 'eslint config file' })
    .option('fix', { describe: 'Fix found issues' })
    .option('quiet', { describe: 'Report only errors' })
    .demandCommand(1)
    .argv;

const eslintConfig = {
    config: argv.config,
    quiet: !!argv.quiet,
    fix: !!argv.fix,
    patterns: argv._,
};

function getLintDecorator() {
    if (eslintConfig.quiet) {
        return ESLint.getErrorResults;
    }
    return (x) => x;
}

(async function main() {
    const currentDir = process.cwd();
    const fileFormatters = {
        junit: 'build/eslint/junit.xml',
        json: 'build/eslint/report.json',
        '@microsoft/sarif': 'build/eslint/report.sarif',
    };

    const eslint = new ESLint({
        fix: eslintConfig.fix,
        extensions: ['.js', '.jsx', '.ts', '.tsx'],
        overrideConfigFile: eslintConfig.config,
    });

    const originalResults = getLintDecorator()(await eslint.lintFiles(eslintConfig.patterns));
    const results = originalResults.map((result) => ({
        ...result,
        filePath: path.relative(currentDir, result.filePath),
    }));

    if (eslintConfig.fix) {
        await ESLint.outputFixes(originalResults);
    }

    const consoleFormatter = await eslint.loadFormatter();
    console.log(consoleFormatter.format(results));

    const formatPromises = Object.entries(fileFormatters).map(async ([name, destination]) => {
        await fs.mkdir(path.dirname(destination), { recursive: true, mode: 0o755 });

        const formatter = await eslint.loadFormatter(name);
        const formattedResults = formatter.format(results);

        return fs.writeFile(
            path.normalize(destination),
            typeof formattedResults === 'string' ? formattedResults : await formattedResults,
            { encoding: 'utf8' },
        );
    });

    const errorsCount = results.reduce((errors, value) => errors + value.errorCount, 0);

    await Promise.allSettled(formatPromises)
        .then((formatResults) => {
            const rejected = formatResults.filter((result) => result.status === 'rejected');
            if (rejected.length) {
                rejected.forEach((reason) => console.error('Failed to create output file.', reason));
                return process.exit(1);
            }
            if (errorsCount) {
                return process.exit(2);
            }
            return process.exit(0);
        })
        .catch((error) => {
            console.error('Failed to create output files.', error);
            process.exit(1);
        });
}()).catch((error) => {
    console.error(error);
    process.exit(1);
});
