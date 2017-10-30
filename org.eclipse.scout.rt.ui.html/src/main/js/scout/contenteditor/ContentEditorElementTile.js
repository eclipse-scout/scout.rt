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
scout.ContentEditorElementTile = function() {
  scout.ContentEditorElementTile.parent.call(this);
};
scout.inherits(scout.ContentEditorElementTile, scout.Tile);

scout.ContentEditorElementTile.prototype._init = function(model) {
  scout.ContentEditorElementTile.parent.prototype._init.call(this, model);
  this.contentElementDesignHtml = model.contentElementDesignHtml;
  this.identifier = model.identifier;
  this.iconId = model.iconId;
  this.description = model.description;
};

scout.ContentEditorElementTile.prototype._render = function() {
  this.$container = this.$parent.appendDiv('ce-element-tile');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);

  this._renderIcon();
  this._renderLabel();
  this._renderDescription();

  this.$container.attr('draggable', 'true');
  this.$container
    .on('dragstart', this._onDragStart.bind(this))
    .on('dragend', this._onDragEnd.bind(this))
    .on('mousedown', this._onMouseDown.bind(this))
    .on('mouseup', this._onMouseUp.bind(this));

};

scout.ContentEditorElementTile.prototype._renderProperties = function() {
  scout.ContentEditorElementTile.parent.prototype._renderProperties.call(this);
  this._renderLabel();
  this._renderIcon();
};

scout.ContentEditorElementTile.prototype._renderLabel = function() {
  if (!this.$label) {
    this.$label = this.$container.appendDiv('ce-element-tile-label');
  }
  this.$label.text(this.label);
};

scout.ContentEditorElementTile.prototype._renderDescription = function() {
  if (!this.$description) {
    this.$description = this.$container.appendDiv('ce-element-tile-description');
  }
  this.$description.text(this.description);
};

scout.ContentEditorElementTile.prototype._renderIcon = function() {
  var icon = scout.icons.parseIconId(this.iconId);
  if (!this.$icon) {
    this.$icon = this.$container.appendDiv('ce-element-tile-icon');
  }
  this.$icon.text(icon.iconCharacter);
};

scout.ContentEditorElementTile.prototype._onDragStart = function(event) {
  this.$container.addClass('grabbing');
  var textData = this.contentElementDesignHtml;
  var type = this.identifier;
  event.originalEvent.dataTransfer.setData(type, textData);
  event.stopPropagation();
};

scout.ContentEditorElementTile.prototype._onMouseDown = function(event) {
  this.$container.addClass('grabbing');
  // Prevent FocusManager mousedown event
  event.stopPropagation();
};

scout.ContentEditorElementTile.prototype._onMouseUp = function(event) {
  this.$container.removeClass('grabbing');
  event.stopPropagation();
};

scout.ContentEditorElementTile.prototype._onDragEnd = function(event) {
  this.$container.removeClass('grabbing');
  event.stopPropagation();
};

scout.ContentEditorElementTile.prototype._setContentElementDesignHtml = function(contentElementDesignHtml) {
  this._setProperty('contentElementDesignHtml', contentElementDesignHtml);
  this.contentElementDesignHtml = contentElementDesignHtml;
};

scout.ContentEditorElementTile.prototype.setContentElementDesignHtml = function(contentElementDesignHtml) {
  this.setProperty('contentElementDesignHtml', contentElementDesignHtml);
};
