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

export default class StringFieldCtrlEnterKeyStroke extends KeyStroke {

  constructor(stringField) {
    super();
    this.field = stringField;
    this.which = [keys.ENTER];
    this.ctrl = true;
  }

  _accept(event) {
    let accepted = super._accept(event);
    return accepted && this.field.hasAction;
  }

  handle(event) {
    this.field._onIconClick();
  }
}
