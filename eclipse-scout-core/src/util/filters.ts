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
import $ from 'jquery';

export const filters = {
  /**
   * Returns a function that always evaluates to 'true'.
   */
  returnTrue(): boolean {
    return true;
  },

  /**
   * Returns a function that always evaluates to 'false'.
   */
  returnFalse(): boolean {
    return false;
  },

  /**
   * Returns a filter to accept only elements which are located outside the given container, meaning not the container itself nor one of its children.
   */
  outsideFilter(container: JQuery | Element): () => boolean {
    let c: Element = container instanceof $ ? container[0] : container;
    return function() {
      return this !== c && !$.contains(c, this);
    };
  },

  /**
   * Returns a filter to accept only elements which are not the given element.
   *
   * @param DOM or jQuery element.
   */
  notSameFilter(element: JQuery | Element): () => boolean {
    let e: Element = element instanceof $ ? element[0] : element;
    return function() {
      return this !== e;
    };
  }
};
