/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import $ from 'jquery';
import objects from '../util/objects';

export default class LookupRow {

  constructor() {
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
  }

  init(model) {
    $.extend(this, model);
  }

  setKey(key) {
    this.key = key;
  }

  setText(text) {
    this.text = text;
  }

  setParentKey(parentKey) {
    this.parentKey = parentKey;
  }

  setCssClass(cssClass) {
    this.cssClass = cssClass;
  }

  setAdditionalTableRowData(additionalTableRowData) {
    this.additionalTableRowData = additionalTableRowData;
  }

  setIconId(iconId) {
    this.iconId = iconId;
  }

  setTooltipText(tooltipText) {
    this.tooltipText = tooltipText;
  }

  setBackgroundColor(backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  setForegroundColor(foregroundColor) {
    this.foregroundColor = foregroundColor;
  }

  setFont(font) {
    this.font = font;
  }

  equals(other) {
    return objects.propertiesEquals(this, other, Object.keys(this));
  }

  toString() {
    return 'scout.LookupRow[key=' + this.key + ' text=' + this.text + ']';
  }
}
