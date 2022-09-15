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
import {keys, KeyStroke} from '../index';
import {KeystrokeRenderAreaProvider} from './KeyStroke';
import {CloseableWidget} from '../types';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class CloseKeyStroke extends KeyStroke {

  declare field: CloseableWidget;

  constructor(field: CloseableWidget, $drawingArea: KeystrokeRenderAreaProvider) {
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
