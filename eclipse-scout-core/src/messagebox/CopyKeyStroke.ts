/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, KeyStroke} from '../index';

export default class CopyKeyStroke extends KeyStroke {

  constructor(field) {
    super();
    this.field = field;
    this.which = [keys.C];
    this.ctrl = true;
    this.preventDefault = false;
    this.renderingHints.render = true;
    this.renderingHints.$drawingArea = ($drawingArea, event) => field.$container;
  }

  handle(event) {
    this.field.copy();
  }
}
