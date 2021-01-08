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

export default class FileChooserFieldBrowseKeyStroke extends KeyStroke {

  constructor(field) {
    super();
    this.field = field;
    this.which = [keys.SPACE];
    this.stopPropagation = true;

    this.renderingHints.hAlign = HAlign.LEFT;
    this.renderingHints.$drawingArea = ($drawingArea, event) => this.field.$fieldContainer;
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    this.field.fileInput.browse();
  }
}
