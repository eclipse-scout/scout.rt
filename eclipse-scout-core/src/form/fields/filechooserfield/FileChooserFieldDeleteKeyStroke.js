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
import {HAlign, keys, KeyStroke} from '../../../index';

export default class FileChooserFieldDeleteKeyStroke extends KeyStroke {

  constructor(field) {
    super();
    this.field = field;
    this.which = [keys.DELETE];
    this.stopPropagation = true;

    this.renderingHints.offset = 25;
    this.renderingHints.hAlign = HAlign.RIGHT;
    this.renderingHints.$drawingArea = ($drawingArea, event) => this.field.$fieldContainer;
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    this.field.clear();
  }
}
