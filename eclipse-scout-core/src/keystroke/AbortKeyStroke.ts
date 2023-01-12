/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbortableWidget, CloseKeyStroke, KeystrokeRenderAreaProvider} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class AbortKeyStroke extends CloseKeyStroke {

  declare field: AbortableWidget;

  constructor(field: AbortableWidget, $drawingArea: KeystrokeRenderAreaProvider) {
    super(field, $drawingArea);
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.abort();
  }
}
