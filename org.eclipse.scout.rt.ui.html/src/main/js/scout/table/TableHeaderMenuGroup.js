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
scout.TableHeaderMenuGroup = function() {
  scout.TableHeaderMenuGroup.parent.call(this);
  this.text;
  this.textKey;
  this.cssClass;
  this.visible = true;
  this.last = false;
};
scout.inherits(scout.TableHeaderMenuGroup, scout.Widget);

scout.TableHeaderMenuGroup.prototype._init = function(options) {
  scout.TableHeaderMenuGroup.parent.prototype._init.call(this, options);
  $.extend(this, options);
  this.text = this.session.text(this.textKey);
};

scout.TableHeaderMenuGroup.prototype._render = function() {
  this.$container = this.$parent.appendDiv('table-header-menu-group buttons');
  this.$text = this.$container.appendDiv('table-header-menu-group-text');
  if (this.cssClass) {
    this.$container.addClass(this.cssClass);
  }
  this._renderText();
  this.children.forEach(function(child) {
    child.render();
  }, this);
};

scout.TableHeaderMenuGroup.prototype.appendText = function(text) {
  this.text = this.session.text(this.textKey) + ' ' + text;
  if (this.rendered) {
    this._renderText();
  }
};

scout.TableHeaderMenuGroup.prototype.resetText = function() {
  this.text = this.session.text(this.textKey);
  if (this.rendered) {
    this._renderText();
  }
};

scout.TableHeaderMenuGroup.prototype._renderText = function() {
  this.$text.text(this.text);
};

scout.TableHeaderMenuGroup.prototype.setLast = function(last) {
  this.setProperty('last', last);
};

scout.TableHeaderMenuGroup.prototype._renderLast = function() {
  this.$container.toggleClass('last', this.last);
};
