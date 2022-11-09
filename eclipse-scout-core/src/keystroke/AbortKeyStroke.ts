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
