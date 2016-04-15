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
scout.DesktopToolBox = function(menuBar) {
  scout.DesktopToolBox.parent.call(this);
};
scout.inherits(scout.DesktopToolBox, scout.MenuBox);

scout.DesktopToolBox.prototype._init = function(options) {
  options.customMenuCssClasses = options.customMenuCssClasses || '';
  options.customMenuCssClasses += ' desktop-tool-box-item';
  scout.DesktopToolBox.parent.prototype._init.call(this, options);
};

/**
 * @override
 */
scout.DesktopToolBox.prototype._initMenu = function(menu) {
  scout.DesktopToolBox.parent.prototype._initMenu.call(this, menu);
  menu.popupOpeningDirectionX = 'left';
};

/**
 * @override
 */
scout.DesktopToolBox.prototype._render = function($parent) {
  scout.DesktopToolBox.parent.prototype._render.call(this, $parent);
  this.$container.addClass('desktop-tool-box');
};
