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
import {ActionKeyStroke, HAlign} from '../../index';

export default class ViewButtonActionKeyStroke extends ActionKeyStroke {

  constructor(action) {
    super(action);
  }

  _postRenderKeyBox($drawingArea) {
    let width = $drawingArea.outerWidth();
    let keyBoxWidth = $drawingArea.find('.key-box').outerWidth();
    let left = width / 2 - keyBoxWidth / 2;
    $drawingArea.find('.key-box').cssLeft(left);
  }
}
