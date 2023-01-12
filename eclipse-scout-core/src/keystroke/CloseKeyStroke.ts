/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CloseableWidget, keys, KeyStroke, KeystrokeRenderAreaProvider} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class CloseKeyStroke extends KeyStroke {

  declare field: CloseableWidget;

  constructor(field: CloseableWidget, $drawingArea?: KeystrokeRenderAreaProvider) {
    super();
    this.field = field;
    this.which = [keys.ESC];
    this.stopPropagation = true;
    this.renderingHints = {
      render: !!$drawingArea,
      $drawingArea: $drawingArea
    };
    this.inheritAccessibility = false;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.close();
  }
}
