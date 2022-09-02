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

export default interface ViewportScrollerModel {

  viewportWidth: number;

  viewportHeight: number;

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
