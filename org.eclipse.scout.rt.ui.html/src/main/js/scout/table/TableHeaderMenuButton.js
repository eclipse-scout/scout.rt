/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
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
  this.textVisible = false;
  this.tabbable = true;
};
scout.inherits(scout.TableHeaderMenuButton, scout.Action);

/**
 * @override
 */
scout.TableHeaderMenuButton.prototype._initKeyStrokeContext = function() {
  scout.TableHeaderMenuButton.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke([new scout.TableHeaderMenuButtonKeyStroke(this)]);
};

scout.TableHeaderMenuButton.prototype._render = function() {
  scout.TableHeaderMenuButton.parent.prototype._render.call(this);
  this.$container = this.$container.addClass('table-header-menu-command')
    .unfocusable()
    .on('mouseenter', this._onMouseOver.bind(this))
    .on('mouseleave', this._onMouseOut.bind(this));
  this.$icon = this.$container.appendSpan('icon');
};

scout.TableHeaderMenuButton.prototype._renderProperties = function() {
  scout.TableHeaderMenuButton.parent.prototype._renderProperties.call(this);
  this._renderToggleAction();
};

// Show 'remove' text when button is already selected
scout.TableHeaderMenuButton.prototype._onMouseOver = function() {
  var text = this.selected ?
    this.session.text('ui.remove') : this.text;
  this.parent.appendText(text);
};

scout.TableHeaderMenuButton.prototype._onMouseOut = function() {
  this.parent.resetText();
};

scout.TableHeaderMenuButton.prototype._renderToggleAction = function() {
  this.$container.toggleClass('togglable', this.toggleAction);
};

/**
 * @override
 */
scout.TableHeaderMenuButton.prototype._renderIconId = function() {
  if (this.iconId) {
    this.$icon.attr('data-icon', this.iconId);
  } else {
    this.$icon.removeAttr('data-icon');
  }
};
