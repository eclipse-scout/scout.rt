/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, ActionKeyStroke} from '../../index';

export class ViewButtonActionKeyStroke extends ActionKeyStroke {

  constructor(action: Action) {
    super(action);
  }

  protected override _postRenderKeyBox($drawingArea: JQuery, $keyBox?: JQuery) {
    let width = $drawingArea.outerWidth();
    let keyBoxWidth = $drawingArea.find('.key-box').outerWidth();
    let left = width / 2 - keyBoxWidth / 2;
    $drawingArea.find('.key-box').cssLeft(left);
  }
}
