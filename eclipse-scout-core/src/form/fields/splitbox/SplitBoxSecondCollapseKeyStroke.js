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
import {KeyStroke} from '../../../index';

export default class SplitBoxSecondCollapseKeyStroke extends KeyStroke {

  constructor(splitBox, keyStroke) {
    super();
    this.field = splitBox;
    this.parseAndSetKeyStroke(keyStroke);
    this.inheritAccessibility = false;
  }

  handle(event) {
    this.field.collapseHandleButtonPressed({
      right: true
    });
  }

  _postRenderKeyBox($drawingArea, $keyBox) {
    let handleOffset,
      $collapseHandle = this.field._collapseHandle.$container;

    $keyBox.addClass('split-box-collapse-key-box right');
    handleOffset = $collapseHandle.offsetTo(this.field.$container);

    $keyBox
      .cssLeft(handleOffset.left + $collapseHandle.width() - $keyBox.outerWidth())
      .cssTop(handleOffset.top - $keyBox.outerHeight());
  }
}
