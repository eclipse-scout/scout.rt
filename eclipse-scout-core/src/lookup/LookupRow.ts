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
import LookupRowModel from './LookupRowModel';

export default class LookupRow<Key> implements LookupRowModel<Key> {
  declare model: LookupRowModel<Key>;

  key: Key;
  text: string;
  parentKey: Key;
  enabled: boolean;
  active: boolean;
  additionalTableRowData: any;
  cssClass: string;
  iconId: string;
  tooltipText: string;
  backgroundColor: string;
  foregroundColor: string;
  font: string;

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

  init(model?: LookupRowModel<Key>) {
    $.extend(this, model);
  }

  setKey(key: Key) {
    this.key = key;
  }

  setText(text: string) {
    this.text = text;
  }

  setParentKey(parentKey: Key) {
    this.parentKey = parentKey;
  }

  setCssClass(cssClass: string) {
    this.cssClass = cssClass;
  }

  setAdditionalTableRowData(additionalTableRowData: any) {
    this.additionalTableRowData = additionalTableRowData;
  }

  setIconId(iconId: string) {
    this.iconId = iconId;
  }

  setTooltipText(tooltipText: string) {
    this.tooltipText = tooltipText;
  }

  setBackgroundColor(backgroundColor: string) {
    this.backgroundColor = backgroundColor;
  }

  setForegroundColor(foregroundColor: string) {
    this.foregroundColor = foregroundColor;
  }

  setFont(font: string) {
    this.font = font;
  }

  equals(other: any): boolean {
    return objects.propertiesEquals(this, other, Object.keys(this));
  }

  toString(): string {
    return 'scout.LookupRow[key=' + this.key + ' text=' + this.text + ']';
  }
}
