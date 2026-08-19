import HtmlWebpackPlugin from 'html-webpack-plugin';
import process from 'node:process';
import posthtml from 'posthtml';
import type { Compiler } from 'webpack';
import { DEFAULT_PREFIX, getAppConfig } from './process-env';

const PLUGIN_NAME: string = 'posthtml';

function run(prefix: string, content: string): string {
    const postHtmlConfig: { plugins: Record<string, unknown> } = { plugins: {} };

    // Load posthtml-expressions only during local mode. In build mode, we want to keep expressions
    // so that they can be replaced at runtime.
    if (process && process.env && process.env.LOCAL_MODE) {
        const appConfig = getAppConfig(prefix);
        const appConfigJson = JSON.stringify(appConfig);
        const appConfigScript = `<script>window.${prefix} = ${appConfigJson};</script>`;

        postHtmlConfig.plugins['posthtml-expressions'] = {
            strictMode: false,
            unescapeDelimiters: ['${', '}'],
            locals: {
                ...appConfig,
                [`${prefix}_JSON`]: appConfigJson,
                [`${prefix}_SCRIPT`]: appConfigScript,
            },
        };
    }

    let processor = posthtml();

    Object.keys(postHtmlConfig.plugins).forEach((pluginName) => {
        // eslint-disable-next-line @typescript-eslint/no-require-imports,import/no-dynamic-require,global-require
        processor = processor.use(require(pluginName)(postHtmlConfig.plugins[pluginName]));
    });

    return (processor.process(content, { sync: true }) as unknown as Awaited<ReturnType<typeof processor['process']>>)
        .html;
}

export function createPostHtmlPlugin(prefix: string = DEFAULT_PREFIX) {
    return (compiler: Compiler): void => {
        compiler.hooks.compilation.tap(PLUGIN_NAME, (compilation) => {
            // Static Plugin interface |compilation |HOOK NAME | register listener
            HtmlWebpackPlugin.getHooks(compilation).beforeEmit.tap(
                PLUGIN_NAME,
                (data) => {
                    // eslint-disable-next-line no-param-reassign
                    data.html = run(prefix, data.html);
                    return data;
                },
            );
        });
    };
}
