/*******************************************************************************
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

const baseConfig = require('./scripts/webpack-defaults');
module.exports = (env, args) => {
  let config = baseConfig(env, args);

  config.externals = {
    // The value of this property (jQuery) is the name of the global variable (window scope)
    // where the declared external library will be available in the browser. This is required
    // for the ES5-client case, where a developer will add eclipse-scout.js and jquery.js from
    // a CDN source and develops a Scout app without a build stack.
    // see: https://webpack.js.org/configuration/externals/
    jquery: 'jQuery'
  };
  config.optimization.splitChunks = undefined; // no splitting for scout

  return config;
};
