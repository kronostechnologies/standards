/// <reference lib="esnext" />
import dotenv, { DotenvParseOutput } from 'dotenv';
import process from 'node:process';
import path from 'path';

export const DEFAULT_PREFIX = 'APP_CONFIG';
export const DEFAULT_FILENAME = 'app-config.json';

export function getAppConfig(
    prefix: string = DEFAULT_PREFIX,
    environment: string | undefined = process.env.NODE_ENV,
): Record<string, string> {
    const prefixRegexp = new RegExp(`^${prefix}_`, 'i');
    const envFiles: string[] = [
        '.env',
        '.env.local',
        environment ? `.env.${environment}` : undefined,
        environment ? `.env.${environment}.local` : undefined,
    ].filter((fileName): fileName is string => Boolean(fileName));

    const envVars: DotenvParseOutput = envFiles
        .map((fileName) => path.resolve(process.cwd(), fileName))
        .map((fileName) => dotenv.config({ path: fileName, quiet: true }).parsed)
        .reduce((mergedConfigs: DotenvParseOutput, config) => Object.assign(mergedConfigs, config), {});

    return Object.fromEntries(
        Object.entries(envVars)
            .filter(([key]) => prefixRegexp.test(key))
            .map(([key, value]) => ([key.replace(prefixRegexp, ''), value])),
    );
}
