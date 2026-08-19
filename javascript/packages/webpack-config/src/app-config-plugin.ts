import { type Compilation, type Compiler, sources } from 'webpack';
import { DEFAULT_FILENAME, getAppConfig } from './process-env';

const PLUGIN_NAME: string = 'app-config';

function run(compilation: Compilation, filename: string): void {
    const appConfig = getAppConfig();
    compilation.emitAsset(
        filename,
        new sources.RawSource(JSON.stringify(appConfig)),
        { minimized: true },
    );
}

export function createAppConfigPlugin(filename: string = DEFAULT_FILENAME) {
    return (compiler: Compiler): void => {
        compiler.hooks.compilation.tap(PLUGIN_NAME, (compilation) => {
            compilation.hooks.finishModules.tap(PLUGIN_NAME, () => run(compilation, filename));
        });
    };
}
