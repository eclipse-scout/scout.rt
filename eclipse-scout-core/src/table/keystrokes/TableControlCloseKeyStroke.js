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

export default class TableControlCloseKeyStroke extends KeyStroke {

  constructor(tableControl) {
    super();
    this.field = tableControl;
    this.which = [keys.ESC];
    this.stopPropagation = true;
    this.renderingHints.render = false;
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    this.field.toggle();
  }
}
