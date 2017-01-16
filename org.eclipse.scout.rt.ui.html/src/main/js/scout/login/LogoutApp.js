/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * init options:
 * - loginUrl: URL to redirect after login again button click
 * - logoUrl: default points to 'res/logo.png'
 */
scout.LogoutApp = function() {
  scout.LogoutApp.parent.call(this);
};
scout.inherits(scout.LogoutApp, scout.App);

/**
 * Default adds polyfills too, not required here
 * @override
 */
scout.LogoutApp.prototype._prepareEssentials = function(options) {
  scout.objectFactory.init();
};

/**
 * No bootstrapping required
 * @override
 */
scout.LogoutApp.prototype._doBootstrap = function(options) {
  return [];
};

scout.LogoutApp.prototype._init = function(options) {
  options = options || {};
  options.texts = $.extend({}, scout.texts.readFromDOM(), options.texts);
  this._prepareDOM();

  var logoutBox = scout.create('LogoutBox', options);
  logoutBox.render($('body'));
};
