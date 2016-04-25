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
scout.ViewTab = function() {
  scout.ViewTab.parent.call(this);

  this.title;
  this.subTitle;
  this.iconId;

  this._mouseListener;

  // Container for the _Tab_ (not for the view).
  this.$container;

  this._addEventSupport();
};
scout.inherits(scout.ViewTab, scout.Widget);

scout.ViewTab.prototype._init = function(options) {
  scout.ViewTab.parent.prototype._init.call(this, options);
  this.title = options.title;
  this.subTitle = options.subTitle;
  this.iconId = options.iconId;
  this.selected = false;

};

scout.ViewTab.prototype.renderAfter = function($parent, sibling) {
  this.render($parent);
  if (sibling) {
    this.$container.insertAfter(sibling.$container);
  }
};

scout.ViewTab.prototype._render = function($parent) {
  this.$container = $parent.prependDiv('desktop-view-tab');
  this._mouseListener = this._onMouseDown.bind(this);
  this.$container.on('mousedown', this._mouseListener);
  this._$title = this.$container.appendDiv('title');
  this._$subTitle = this.$container.appendDiv('sub-title');
  this._titlesUpdated();
  this._renderSelection();
  this._cssClassUpdated(this.view.cssClass, null);
};

scout.ViewTab.prototype._renderSelection = function() {
  if (this.$container) {
    if (this.$container.select() === this.selected) {
      return;
    }
    this.$container.select(this.selected);
  }
};

scout.ViewTab.prototype.select = function() {
  this.selected = true;
  this._renderSelection();
};

scout.ViewTab.prototype.deselect = function() {
  this.selected = false;
  this._renderSelection();
};


scout.ViewTab.prototype._onMouseDown = function(event) {
  this.trigger('tabClicked');
};

scout.ViewTab.prototype.setTitle = function(title) {
  if (this.title === title) {
    return;
  }
  this.title = title;
  this._titlesUpdated();
};

scout.ViewTab.prototype.setSubTitle = function(subTitle) {
  if (this.subTitle === subTitle) {
    return;
  }
  this.subTitle = subTitle;
  this._titlesUpdated();
};

scout.ViewTab.prototype.setIconId = function(iconId) {
  if (this.iconId === iconId) {
    return;
  }
  this.iconId = iconId;
  this._titlesUpdated();
};

scout.ViewTab.prototype._titlesUpdated = function() {
  if (!this.$container) {
    return;
  }

  // Titles
  setTitle(this._$title, this.title);
  setTitle(this._$subTitle, this.subTitle);

  // Icon
  this.$container.icon(this.view.iconId);

  // ----- Helper functions -----

  function setTitle($titleElement, title) {
    $titleElement.textOrNbsp(title);
  }
};

scout.ViewTab.prototype.getMenuText = function() {
  var text = this.title;
  if (this.subTitle) {
    text += ' (' + this.subTitle + ')';
  }
  return text;
};
