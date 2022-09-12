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
import {keys, KeyStroke} from '../index';

export default class CloseKeyStroke extends KeyStroke {

  constructor(field, $drawingArea) {
    super();
    this.field = field;
    this.which = [keys.ESC];
    this.renderingHints.render = true;
    this.stopPropagation = true;
    this.renderingHints = {
      render: !!$drawingArea,
      $drawingArea: $drawingArea
    };
    this.inheritAccessibility = false;
  }

  handle(event) {
    this.field.close();
  }
}
