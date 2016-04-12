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
scout.DesktopToolBar = function(menuBar) {
  scout.DesktopToolBar.parent.call(this);
};
scout.inherits(scout.DesktopToolBar, scout.ActionBox);

scout.DesktopToolBar.prototype._init = function(options) {
  options.customActionCssClasses = options.customActionCssClasses || '';
  options.customActionCssClasses += ' header-tool-item';
  scout.DesktopToolBar.parent.prototype._init.call(this, options);
};

/**
 * @override
 */
scout.DesktopToolBar.prototype._initAction = function(action) {
  scout.DesktopToolBar.parent.prototype._initAction.call(this, action);
  action.popupOpeningDirectionX = 'left';
};

/**
 * @override
 */
scout.DesktopToolBar.prototype._render = function($parent) {
  scout.DesktopToolBar.parent.prototype._render.call(this, $parent);
  this.$container.addClass('header-tools');
};


