/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, KeyStroke} from '../../../index';

/**
 * Closes the popup without accepting the proposal
 */
export default class SmartFieldCancelKeyStroke extends KeyStroke {

  constructor(field) {
    super();
    this.field = field;
    this.which = [keys.ESC];
    this.stopPropagation = true;
    this.preventInvokeAcceptInputOnActiveValueField = true;

    this.renderingHints.$drawingArea = function($drawingArea, event) {
      return this.field.$fieldContainer;
    }.bind(this);
  }

  _accept(event) {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    if (!this.field.popup) {
      return false;
    }
    return true;
  }

  /**
   * @override
   */
  handle(event) {
    this.field.closePopup();
    this.field.resetDisplayText();
  }
}
