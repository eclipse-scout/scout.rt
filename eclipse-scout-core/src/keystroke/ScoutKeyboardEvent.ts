/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {KeyStrokeMode} from '../index';

/**
 * Common interface between {@link VirtualKeyStrokeEvent} and {@link JQuery.KeyboardEventBase}
 */
export interface ScoutKeyboardEvent {
  /**
   * @see JQuery.KeyboardEventBase.which
   */
  which: number;

  /**
   * @see JQuery.KeyboardEventBase.ctrlKey
   */
  ctrlKey: boolean;

  /**
   * @see JQuery.KeyboardEventBase.metaKey
   */
  metaKey: boolean;

  /**
   * @see JQuery.KeyboardEventBase.altKey
   */
  altKey: boolean;

  /**
   * @see JQuery.KeyboardEventBase.shiftKey
   */
  shiftKey: boolean;

  /**
   * @see JQuery.KeyboardEventBase.target
   */
  target: HTMLElement;

  /**
   * one of {@link KeyStrokeMode}
   */
  type: string;

  /**
   * Optional original {@link KeyboardEvent} enhanced by a custom smartfield event flag. May be undefined.
   * @see JQuery.KeyboardEventBase.originalEvent
   */
  originalEvent?: KeyboardEvent & { smartFieldEvent?: boolean } | undefined;

  /**
   * @see JQuery.Event.stopPropagation
   */
  stopPropagation();

  /**
   * @see JQuery.Event.isPropagationStopped
   */
  isPropagationStopped(): boolean;

  /**
   * @see JQuery.Event.stopImmediatePropagation
   */
  stopImmediatePropagation();

  /**
   * @see JQuery.Event.isImmediatePropagationStopped
   */
  isImmediatePropagationStopped(): boolean;

  /**
   * @see JQuery.Event.preventDefault
   */
  preventDefault();

  /**
   * @see JQuery.Event.isDefaultPrevented
   */
  isDefaultPrevented(): boolean;
}
