/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';
import {InitModelOf, LookupRowModel, objects, SomeRequired} from '../index';

export class LookupRow<TKey> implements LookupRowModel<TKey> {
  declare model: LookupRowModel<TKey>;
  declare initModel: SomeRequired<this['model'], 'key' | 'text'>;

  key: TKey;
  text: string;
  parentKey: TKey;
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

  init(model?: InitModelOf<this>) {
    $.extend(this, model);
  }

  setKey(key: TKey) {
    this.key = key;
  }

  setText(text: string) {
    this.text = text;
  }

  setParentKey(parentKey: TKey) {
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
