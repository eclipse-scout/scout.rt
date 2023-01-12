/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * Rule describing how to resolve a focusable element.
 *
 * @see FocusManager
 */
export enum FocusRule {
  /**
   * Indicates to focus the first focusable element.
   */
  AUTO = 'auto',
  /**
   * Indicates to not focus any element.
   */
  NONE = 'none',
  /**
   * If prepare is used when installing a focus context, the context won't be ready until explicitly told. During that time, focus context listeners are not attached yet and focus requests are not allowed to be executed.
   * But: the element of the focused request will be stored as usual in lastValidFocusedElement. So as soon as the context is ready, a call to restoreFocus would focus that element.
   *
   * This is useful to disable focus requests during a preparation phase without losing the element which should be focused.
   *
   * <b>Important</b>: {@link FocusContext.ready} must be called manually as soon as the focus context is ready.
   */
  PREPARE = 'prepare'
}
