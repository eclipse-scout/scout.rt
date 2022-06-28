/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
const scoutBuildConstants = require('./constants');
const baseConfig = require('./webpack-defaults');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = (env, args) => {
    const defaultConfig = baseConfig(env, args);
    defaultConfig.target = 'es6';
    defaultConfig.output.chunkFormat = 'module';
    defaultConfig.output.library = {
        type: 'module'
    };
    delete defaultConfig.optimization.splitChunks;

    // No hash for a library to ensure stable name (required for package.json).
    // Furthermore, there is no need for a hash anyway as the lib is not delivered to a browser.
    const hashSuffix = scoutBuildConstants.contentHashSuffix;
    if (defaultConfig.output.filename) {
        defaultConfig.output.filename = defaultConfig.output.filename.replace(hashSuffix, '');
    }
    defaultConfig.plugins
        .filter(plugin => plugin instanceof MiniCssExtractPlugin)
        .forEach(cssExtractPlugin => {
            if (cssExtractPlugin.options.filename) {
                cssExtractPlugin.options.filename = cssExtractPlugin.options.filename.replace(hashSuffix, '');
            }
            if (cssExtractPlugin.options.chunkFilename) {
                cssExtractPlugin.options.chunkFilename = cssExtractPlugin.options.chunkFilename.replace(hashSuffix, '');
            }
        });

    return defaultConfig;
};
