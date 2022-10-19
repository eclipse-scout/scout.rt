/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {KeyStroke, SplitBox} from '../../../index';

export default class SplitBoxFirstCollapseKeyStroke extends KeyStroke {
  declare field: SplitBox;

  constructor(splitBox: SplitBox, keyStroke: string) {
    super();
    this.field = splitBox;
    this.parseAndSetKeyStroke(keyStroke);
    this.inheritAccessibility = false;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.collapseHandleButtonPressed({
      left: true
    });
  }

  protected override _postRenderKeyBox($drawingArea: JQuery, $keyBox?: JQuery) {
    // @ts-ignore
    let $collapseHandle = this.field._collapseHandle.$container;
    $keyBox.addClass('split-box-collapse-key-box left');
    let handleOffset = $collapseHandle.offsetTo(this.field.$container);

    $keyBox
      .cssLeft(handleOffset.left - $keyBox.outerWidth())
      .cssTop(handleOffset.top);
  }
}
