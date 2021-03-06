/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
const baseConfig = require('@eclipse-scout/cli/scripts/karma-defaults');

module.exports = (config, specEntryPoint) => {
  baseConfig(config, specEntryPoint);
  // noinspection JSUnresolvedVariable
  config.webpack.externals = {
    // jQuery is the only external, all other dependencies are imported regularly by the specs
    'jquery': 'jQuery'
  };
};
