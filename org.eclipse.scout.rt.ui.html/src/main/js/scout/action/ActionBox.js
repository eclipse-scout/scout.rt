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
scout.ActionBox = function(menuBar) {
  scout.ActionBox.parent.call(this);
  this.compact = false;
};
scout.inherits(scout.ActionBox, scout.Widget);

scout.ActionBox.prototype._init = function(options) {
  scout.ActionBox.parent.prototype._init.call(this, options);
  this.actions = options.actions || [];
  this.customActionCssClasses = options.customActionCssClasses || '';
  this.customActionCssClasses += ' ' + 'action-box-item';
  this._initActions(this.actions);
};

scout.ActionBox.prototype._initActions = function(actions) {
  actions.forEach(this._initAction.bind(this));
};

scout.ActionBox.prototype._initAction = function(action) {
  action.setParent(this);
  action._customCssClasses = this.customActionCssClasses;
};

/**
 * @override Widget.js
 */
scout.ActionBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('action-box header-tools');

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ActionBoxLayout(this));
};

scout.ActionBox.prototype._renderProperties = function() {
  this._renderActions();
  this._renderCompact();
};

scout.ActionBox.prototype._renderActions = function() {
  this.actions.forEach(function(action) {
    action.render(this.$container);
  }, this);
};

scout.ActionBox.prototype._renderCompact = function() {
  this.$container.toggleClass('compact', this.compact);
  this.invalidateLayoutTree();
};

scout.ActionBox.prototype.setCompact = function(compact) {
  if (this.compact === compact) {
    return;
  }
  this.compact = compact;
  if (this.rendered) {
    this._renderCompact();
  }
};
