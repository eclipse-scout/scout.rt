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
module.exports = (env, args) => {
  args.resDirArray = ['res'];
  if (args.run === 'global') {
    return require('./webpack.config.global.js')(env, args);
  }
  if (args.run === 'testing') {
    return require('./webpack.config.testing.js')(env, args);
  }
  const baseConfig = require('@eclipse-scout/cli/scripts/webpack-defaults');
  const config = baseConfig(env, args);
  return {
    entry: {
      'eclipse-scout-core.esm': './src/index.ts'
    },
    // Clean is false because the last config will clean it
    ...baseConfig.libraryConfig(config)
  };
};
