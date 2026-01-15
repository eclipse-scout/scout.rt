/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
const pluginName = 'KeepNonceVariablePlugin';
const {toConstantDependency} = require('webpack/lib/javascript/JavascriptParserHelpers');

/**
 * Plugin that instructs webpack to NOT replace '__webpack_nonce__' with '__webpack_require__.nc'.
 * This is required for all library builds because the variable should only be replaced for application builds targeting the browser.
 * @type {KeepNonceVariablePlugin}
 */
module.exports = class KeepNonceVariablePlugin {
  apply(compiler) {
    // see https://github.com/webpack/webpack/blob/v5.104.1/lib/APIPlugin.js#L207
    // see https://webpack.js.org/api/parser/#expression
    compiler.hooks.normalModuleFactory.tap(pluginName, factory => {
      factory.hooks.parser.for('javascript/auto').tap(pluginName, (parser, options) => {
        const webpackNonce = '__webpack_nonce__';
        parser.hooks.expression.for(webpackNonce).tap(pluginName, expression => {
          const dep = toConstantDependency(parser, webpackNonce, [webpackNonce]);
          return dep(expression);
        });
      });
    });
  }
};
