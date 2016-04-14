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
scout.ViewArea = function() {
  scout.ViewArea.parent.call(this);
  this.$body;
  this.$title;
  this.$container;
  this.htmlComp;
  this.viewStack = [];

};
scout.inherits(scout.ViewArea, scout.ModelAdapter);

scout.ViewArea.prototype._init = function(model) {
  scout.ViewArea.parent.prototype._init.call(this, model);
  //  this.menuBar = scout.create('MenuBar', {
  //    parent: this,
  //    menuOrder: new scout.GroupBoxMenuItemsOrder()
  //  });
};

/**
 * @override ModelAdapter.js
 */
scout.ViewArea.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.ViewArea.parent.prototype._initKeyStrokeContext.call(this, this.keyStrokeContext);
  this.keyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.keyStrokeContext.$bindTarget = this._keyStrokeBindTarget.bind(this);
};

/**
 * Returns a $container used as a bind target for the key-stroke context of the group-box.
 * By default this function returns the container of the form, or when group-box is has no
 * form as a parent the container of the group-box.
 */
scout.ViewArea.prototype._keyStrokeBindTarget = function() {
  return this.$container;
};

scout.ViewArea.prototype._render = function($parent) {
  var htmlBody, i,
    env = scout.HtmlEnvironment;

  this.$container = $parent.appendDiv('viewArea');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.SingleLayout());
};

scout.ViewArea.prototype._remove = function() {
  scout.ViewArea.parent.prototype._remove.call(this);
  if (this.scrollable) {
    scout.scrollbars.uninstall(this.$body);
  }
};

scout.ViewArea.prototype._renderProperties = function() {
  scout.ViewArea.parent.prototype._renderProperties.call(this);

  //  this._renderBorderVisible();
  //  this._renderExpandable();
  //  this._renderExpanded();
  //  this._renderMenuBarVisible();
};

scout.ViewArea.prototype._createLayout = function() {
  return new scout.GroupBoxLayout(this);
};

scout.ViewArea.prototype.activateView = function(view) {

};

scout.ViewArea.prototype.showView = function(view) {
  // render
  if (!this.rendered) {
    this.render(this.parent.$container);
  }

  view.render(this.$container);
  view.setParent(this);
  view.$container.addClass('view');
  view.validateRoot = true;
  // add to view stack
  this.viewStack.unshift(view);
  this.htmlComp.invalidateLayoutTree();
  // Layout immediate to prevent 'laggy' form visualization,
  // but not initially while desktop gets rendered because it will be done at the end anyway
  this.htmlComp.validateLayoutTree();
};

scout.ViewArea.prototype.removeView = function(view) {
  var index = this.viewStack.indexOf(view);
  if (index > -1) {
    this.viewStack.splice(index, 1);
    if (view.rendered) {
      view.remove();
    }

    // remove if empty
    if (!this.hasViews() && this.rendered) {
      this.remove();
    }

    this.htmlComp.invalidateLayoutTree();
    this.htmlComp.validateLayoutTree();
  }
};

scout.ViewArea.prototype.viewCount = function(){
  return this.viewStack.length;
};

scout.ViewArea.prototype.hasViews = function(view) {
  return this.viewStack.length > 0;
};
