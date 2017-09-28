/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableHeaderMenuButton = function() {
  scout.TableHeaderMenuButton.parent.call(this);
  this.text;
  this.cssClass;
  this.enabled = true;
  this.visible = true;
  this.selected = false;
  this.togglable = false;
  this.icon;
};
scout.inherits(scout.TableHeaderMenuButton, scout.Widget);

scout.TableHeaderMenuButton.prototype._init = function(options) {
  scout.TableHeaderMenuButton.parent.prototype._init.call(this, options);
  this.text = scout.nvl(this.text, this.session.text(this.textKey));
};

/**
 * @override
 */
scout.TableHeaderMenuButton.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.TableHeaderMenuButton.prototype._initKeyStrokeContext = function() {
  scout.TableHeaderMenuButton.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke([new scout.TableHeaderMenuButtonKeyStroke(this)]);
};

scout.TableHeaderMenuButton.prototype._render = function() {
  this.$container = this.$parent.appendDiv('table-header-menu-command')
    .unfocusable()
    .on('click', this._onClick.bind(this))
    .on('mouseenter', this._onMouseOver.bind(this))
    .on('mouseleave', this._onMouseOut.bind(this));
  if (this.cssClass) {
    this.$container.addClass(this.cssClass);
  }
  this._renderSelected();
  this._renderTogglable();
  this._renderIcon();
};

scout.TableHeaderMenuButton.prototype._onClick = function() {
  if (this.enabled) {
    this.clickHandler.call(this);
  }
};

// Show 'remove' text when button is already selected
scout.TableHeaderMenuButton.prototype._onMouseOver = function() {
  if (this.enabled) {
    var text = this.selected ?
        this.session.text('ui.remove') : this.text;
    this.parent.appendText(text);
  }
};

scout.TableHeaderMenuButton.prototype._onMouseOut = function() {
  this.parent.resetText();
};

scout.TableHeaderMenuButton.prototype._renderSelected = function() {
  this.$container.select(this.selected);
};

scout.TableHeaderMenuButton.prototype._renderTogglable = function() {
  this.$container.toggleClass('togglable', this.togglable);
};

scout.TableHeaderMenuButton.prototype._renderIcon = function() {
  if (this.icon) {
    this.$container.attr('data-icon', this.icon);
  } else {
    this.$container.removeAttr('data-icon');
  }
};

scout.TableHeaderMenuButton.prototype.setSelected = function(selected) {
  this.setProperty('selected', selected);
  this._updateEnabled();
};

scout.TableHeaderMenuButton.prototype._updateEnabled = function() {
  var enabled = true;
  if (this.selected) {
    enabled = this.togglable;
  }
  this.enabled = enabled;
  if (this.rendered) {
    this._renderEnabled();
  }
};

/**
 * @override
 */
scout.TableHeaderMenuButton.prototype._renderEnabled = function() {
  this.$container.toggleClass('disabled', !this.enabled);
  this.$container.setTabbable(this.enabled && !scout.device.supportsTouch());
};

scout.TableHeaderMenuButton.prototype.setIcon = function(icon) {
  this.setProperty('icon', icon);
};

scout.TableHeaderMenuButton.prototype.toggle = function() {
  this.setSelected(!this.selected);
};
