/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import $ from 'jquery';

export class FocusManagerSpecHelper {
  handlersRegistered($comp: JQuery): boolean {
    let i,
      expectedHandlers = ['keydown', 'focusin', 'focusout', 'hide'],
      handlerCount = 0,
      // @ts-expect-error
      events = $._data($comp[0], 'events'),
      expectedCount = expectedHandlers.length;
    if (events) {
      for (i = 0; i < expectedCount; i++) {
        if (events[expectedHandlers[i]]) {
          handlerCount++;
        }
      }
    }
    return handlerCount === expectedCount;
  }
}
