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
const webpackNonce = '__webpack_nonce__';

/**
 * Plugin that instructs webpack to NOT replace '__webpack_nonce__' with '__webpack_require__.nc'.
 * This is required for all library builds because the variable should only be replaced for application builds targeting the browser.
 * @type {KeepNonceVariablePlugin}
 */
module.exports = class KeepNonceVariablePlugin {
  apply(compiler) {
    // see https://github.com/webpack/webpack/blob/v5.113.0/lib/javascript/APIPlugin.js
    // see https://webpack.js.org/api/parser/#expression
    const {ConstDependency} = compiler.webpack.dependencies;
    const handler = parser => {
      parser.hooks.expression.for(webpackNonce).tap(pluginName, expression => {
        const dep = new ConstDependency(webpackNonce, expression.range);
        dep.loc = parser.getLocation(expression);
        parser.state.module.addPresentationalDependency(dep);
        return true;
      });
    };
    compiler.hooks.normalModuleFactory.tap(pluginName, factory => {
      for (const type of ['javascript/auto', 'javascript/dynamic', 'javascript/esm']) {
        factory.hooks.parser.for(type).tap(pluginName, handler);
      }
    });
  }
};
