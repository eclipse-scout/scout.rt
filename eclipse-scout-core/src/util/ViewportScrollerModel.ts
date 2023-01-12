/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {ObjectModel, ViewportScroller} from '../index';

export interface ViewportScrollerModel extends ObjectModel<ViewportScroller> {

  viewportWidth?: number;

  viewportHeight?: number;

  /**
   * distance from the viewport edge (in pixel) where we start to scroll automatically
   */
  e?: number;

  /**
   * position of "fast scroll" area. Same dimension as e. Negative values are outside the viewport.
   */
  f?: number;

  /**
   * milliseconds
   */
  initialDelay?: number;

  /**
   * Function that returns "false", if the scrolling should no longer be active (e.g. because the elements were removed from the DOM) or "true" otherwise.
   */
  active?: () => boolean;

  /**
   * Function that receives the computed delta scroll positions (positive or negative) when automatic scrolling is active.
   */
  scroll?: (dx: number, dy: number) => void;
}
