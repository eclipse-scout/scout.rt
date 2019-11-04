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
import {keys, KeyStroke} from '../../index';

export default class CellEditorCompleteEditKeyStroke extends KeyStroke {

  constructor(popup) {
    super();
    this.field = popup;
    this.which = [keys.ENTER];
    this.stopPropagation = true;
  }

  handle(event) {
    this.field.completeEdit();
  }
}
