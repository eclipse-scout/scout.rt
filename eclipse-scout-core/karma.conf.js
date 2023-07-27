/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
const baseConfig = require('@eclipse-scout/cli/scripts/karma-defaults');
const path = require('path');

module.exports = (config, specEntryPoint) => {
  baseConfig(config, specEntryPoint);

  const resDir = path.resolve('test/_res').replace(/\\/g, '/') + '/';
  config.files.push({pattern: resDir + '**/*.*', watched: false, included: false});
  config.proxies['/_res/'] = '/absolute' + resDir;
};
