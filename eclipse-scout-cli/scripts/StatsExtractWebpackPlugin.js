/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

const pluginName = 'StatsExtractWebpackPlugin';
/**
 * Webpack plugin used to store the build stats (results) on build completion.
 * Result may be obtained using 'statsExtractWebpackPlugin.stats'.
 */
module.exports = class StatsExtractWebpackPlugin {
  apply(compiler) {
    compiler.hooks.done.tap(pluginName, stats => {
      this.stats = stats;
    });
  }
};
