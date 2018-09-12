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
scout.LookupRow = function() {
  this.key = null;
  this.text = null;
  this.parentKey = null;
  this.enabled = true;
  this.active = true;
  this.additionalTableRowData = null;
  this.cssClass = null;
  this.iconId = null;
  this.tooltipText = null;
  this.backgroundColor = null;
  this.foregroundColor = null;
  this.font = null;
};

scout.LookupRow.prototype.init = function(model) {
  $.extend(this, model);
};

scout.LookupRow.prototype.setKey = function(key) {
  this.key = key;
};

scout.LookupRow.prototype.setText = function(text) {
  this.text = text;
};

scout.LookupRow.prototype.setParentKey = function(parentKey) {
  this.parentKey = parentKey;
};

scout.LookupRow.prototype.setCssClass = function(cssClass) {
  this.cssClass = cssClass;
};

scout.LookupRow.prototype.setAdditionalTableRowData = function(additionalTableRowData) {
  this.additionalTableRowData = additionalTableRowData;
};

scout.LookupRow.prototype.setIconId = function(iconId) {
  this.iconId = iconId;
};

scout.LookupRow.prototype.setTooltipText = function(tooltipText) {
  this.tooltipText = tooltipText;
};

scout.LookupRow.prototype.setBackgroundColor = function(backgroundColor) {
  this.backgroundColor = backgroundColor;
};

scout.LookupRow.prototype.setForegroundColor = function(foregroundColor) {
  this.foregroundColor = foregroundColor;
};

scout.LookupRow.prototype.setFont = function(font) {
  this.font = font;
};

scout.LookupRow.prototype.toString = function() {
  return 'scout.LookupRow[key=' + this.key + ' text=' + this.text + ']';
};
