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

const baseConfig = require('@eclipse-scout/cli/scripts/webpack-defaults');
module.exports = (env, args) => {
  const config = baseConfig(env, args);

  // This build creates resources that can directly be included in a html file without needing a build stack (webpack, babel etc.).
  // The resources are available by a CDN that provides npm modules (e.g. https://www.jsdelivr.com/package/npm/@eclipse-scout/core)
  return {
    ...config,
    entry: {
      'eclipse-scout-chart': './src/index.ts',
      'eclipse-scout-chart-theme': './src/eclipse-scout-chart-theme.less',
      'eclipse-scout-chart-theme-dark': './src/eclipse-scout-chart-theme-dark.less'
    },
    optimization: {
      ...config.optimization,
      splitChunks: undefined // disable splitting
    },
    externals: {
      ...config.externals,
      'jquery': 'jQuery',
      '@eclipse-scout/core': 'scout',
      'chart.js': 'Chart',
      'chart.js/auto': 'Chart',
      'chartjs-plugin-datalabels': 'ChartDataLabels'
    }
  };
};
