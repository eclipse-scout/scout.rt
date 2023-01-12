/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, ActionKeyStroke} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class FormMenuActionKeyStroke extends ActionKeyStroke {

  constructor(action: Action) {
    super(action);
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.toggle();
  }

  protected override _postRenderKeyBox($drawingArea: JQuery, $keyBox?: JQuery) {
    if (this.field.iconId) {
      let wIcon = $drawingArea.find('.icon').width();
      let wKeybox = $drawingArea.find('.key-box').outerWidth();
      let containerPadding = $drawingArea.cssPaddingLeft();
      let leftKeyBox = wIcon / 2 - wKeybox / 2 + containerPadding;
      $drawingArea.find('.key-box').cssLeft(leftKeyBox);
    }
  }
}
