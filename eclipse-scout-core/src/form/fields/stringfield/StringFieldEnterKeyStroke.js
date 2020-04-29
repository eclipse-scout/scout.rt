/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, KeyStroke} from '../../../index';

export default class StringFieldEnterKeyStroke extends KeyStroke {

  constructor(stringField) {
    super();
    this.field = stringField;
    this.which = [keys.ENTER];
    this.renderingHints.render = false;
    this.preventDefault = false;
  }

  _applyPropagationFlags(event) {
    super._applyPropagationFlags(event);

    let activeElement = this.field.$container.activeElement(true);
    this.preventInvokeAcceptInputOnActiveValueField = !event.isPropagationStopped() && activeElement.tagName.toLowerCase() === 'textarea';
    if (this.preventInvokeAcceptInputOnActiveValueField) {
      event.stopPropagation();
    }
  }

  handle(event) {
    // NOP
  }
}
