/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {KeyStroke, SplitBox} from '../../../index';

export class SplitBoxFirstCollapseKeyStroke extends KeyStroke {
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
    let $collapseHandle = this.field.collapseHandle.$container;
    $keyBox.addClass('split-box-collapse-key-box left');
    let handleOffset = $collapseHandle.offsetTo(this.field.$container);

    $keyBox
      .cssLeft(handleOffset.left - $keyBox.outerWidth())
      .cssTop(handleOffset.top);
  }
}
