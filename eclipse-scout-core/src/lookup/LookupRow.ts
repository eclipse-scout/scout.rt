/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';
import {Constructor, InitModelOf, LookupRowModel, objects, ObjectWithType, SomeRequired} from '../index';

export class LookupRow<TKey> implements LookupRowModel<TKey>, ObjectWithType {
  declare model: LookupRowModel<TKey>;
  declare initModel: SomeRequired<this['model'], 'key' | 'text'>;

  id: string;
  objectType: string;
  key: TKey;
  text: string;
  parentKey: TKey;
  enabled: boolean;
  active: boolean;
  additionalTableRowData: Record<string, any>;
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

  /**
   * @returns a deep clone of this LookupRow instance. E.g. called when using {@link objects#valueCopy}.
   */
  clone(): this {
    return objects.copyPropertiesRecursive(this, new (this.constructor as Constructor<this>)());
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

  setEnabled(enabled: boolean) {
    this.enabled = enabled;
  }

  setCssClass(cssClass: string) {
    this.cssClass = cssClass;
  }

  setAdditionalTableRowData(additionalTableRowData: Record<string, any>) {
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
