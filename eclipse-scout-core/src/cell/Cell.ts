/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CellModel, InitModelOf, Status, strings, ValueField} from '../index';
import $ from 'jquery';

/**
 * -1 for left, 0 for center and 1 for right.
 */
export type Alignment = -1 | 0 | 1;

export class Cell<TValue = any> implements CellModel<TValue> {
  declare model: CellModel<TValue>;

  cssClass: string;
  editable: boolean;
  errorStatus: Status;
  horizontalAlignment: Alignment;
  htmlEnabled: boolean;
  iconId: string;
  mandatory: boolean;
  text: string;
  flowsLeft: boolean;
  empty: boolean;
  value: TValue;
  tooltipText: string;
  foregroundColor: string;
  backgroundColor: string;
  font: string;
  sortCode: number;
  field: ValueField<TValue>;
  protected _cachedEncodedText: string;

  constructor() {
    this.cssClass = null;
    this.editable = null; /* do not initialize with false. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
    this.errorStatus = null;
    this.horizontalAlignment = null; /* do not initialize with -1. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
    this.htmlEnabled = null; /* do not initialize with false. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
    this.iconId = null;
    this.mandatory = null; /* do not initialize with false. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
    this._cachedEncodedText = null;
    this.text = null;
    this.value = null;
    this.tooltipText = null;
    this.sortCode = null;
  }

  init(model: InitModelOf<this>) {
    this._init(model);
  }

  protected _init(model: InitModelOf<this>) {
    $.extend(this, model);
  }

  update(model: CellModel<TValue>) {
    this.setText(model.text);
    $.extend(this, model);
  }

  setEditable(editable: boolean) {
    this.editable = editable;
  }

  setMandatory(mandatory: boolean) {
    this.mandatory = mandatory;
  }

  setHorizontalAlignment(hAlign: Alignment) {
    this.horizontalAlignment = hAlign;
  }

  setValue(value: TValue) {
    this.value = value;
  }

  setErrorStatus(errorStatus: Status) {
    this.errorStatus = errorStatus;
  }

  setText(text: string) {
    let oldText = this.text;
    this.text = text;

    // reset cached encodedText, so when encodedText() is called the next time, it will be set to the new value
    if (oldText !== this.text) {
      this._cachedEncodedText = null;
    }
  }

  setIconId(iconId: string) {
    this.iconId = iconId;
  }

  encodedText(): string {
    if (!this._cachedEncodedText) {
      // Encode text and cache it, encoding is expensive
      this._cachedEncodedText = strings.encode(this.text);
    }
    return this._cachedEncodedText;
  }

  setCssClass(cssClass: string) {
    this.cssClass = cssClass;
  }

  setSortCode(sortCode: number) {
    this.sortCode = sortCode;
  }
}
