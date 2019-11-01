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
import {HAlign} from '../../index';
import {ActionKeyStroke} from '../../index';

export default class ViewButtonActionKeyStroke extends ActionKeyStroke {

constructor(action) {
  super( action);

}


_postRenderKeyBox($drawingArea) {
  if (this.field.iconId && !this.field._isMenuItem) {
    var width = $drawingArea.outerWidth();
    var wKeybox = $drawingArea.find('.key-box').outerWidth();
    var leftKeyBox = width / 2 - wKeybox / 2;
    $drawingArea.find('.key-box').cssLeft(leftKeyBox);
  }
}

renderKeyBox($drawingArea, event) {
  if (this.field._isMenuItem) {
    this.renderingHints.hAlign = HAlign.RIGHT;
  }
  return super.renderKeyBox( $drawingArea, event);
}
}
