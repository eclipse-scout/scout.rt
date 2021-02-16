/*
 * Copyright (c) 2014-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {strings} from '../index';
import $ from 'jquery';

/**
 * @class
 * @constructor
 */
export default class Cell {

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

  init(model) {
    this._init(model);
  }

  _init(model) {
    $.extend(this, model);
  }

  update(model) {
    this.setText(model.text);
    $.extend(this, model);
  }

  setEditable(editable) {
    this.editable = editable;
  }

  setMandatory(mandatory) {
    this.mandatory = mandatory;
  }

  setHorizontalAlignment(hAlign) {
    this.horizontalAlignment = hAlign;
  }

  setValue(value) {
    this.value = value;
  }

  setErrorStatus(errorStatus) {
    this.errorStatus = errorStatus;
  }

  setText(text) {
    let oldText = this.text;
    this.text = text;

    // reset cached encodedText, so when encodedText() is called the next time
    // it will be set to the a new value
    if (oldText !== this.text) {
      this._cachedEncodedText = null;
    }
  }

  setIconId(iconId) {
    this.iconId = iconId;
  }

  encodedText() {
    if (!this._cachedEncodedText) {
      // Encode text and cache it, encoding is expensive
      this._cachedEncodedText = strings.encode(this.text);
    }
    return this._cachedEncodedText;
  }

  setCssClass(cssClass) {
    this.cssClass = cssClass;
  }

  setSortCode(sortCode) {
    this.sortCode = sortCode;
  }
}
