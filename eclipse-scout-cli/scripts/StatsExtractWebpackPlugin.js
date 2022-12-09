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
