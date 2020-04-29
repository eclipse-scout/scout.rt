/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ActionKeyStroke} from '../index';

export default class FormMenuActionKeyStroke extends ActionKeyStroke {

  constructor(action) {
    super(action);
  }

  handle(event) {
    this.field.toggle();
  }

  _postRenderKeyBox($drawingArea) {
    if (this.field.iconId) {
      let wIcon = $drawingArea.find('.icon').width();
      let wKeybox = $drawingArea.find('.key-box').outerWidth();
      let containerPadding = $drawingArea.cssPaddingLeft();
      let leftKeyBox = wIcon / 2 - wKeybox / 2 + containerPadding;
      $drawingArea.find('.key-box').cssLeft(leftKeyBox);
    }
  }
}
