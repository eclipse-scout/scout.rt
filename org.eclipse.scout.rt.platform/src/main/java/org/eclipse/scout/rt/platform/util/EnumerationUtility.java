/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Collection of additional {@link Enumeration} support that is missing in Java 8.
 */
public final class EnumerationUtility {

  private EnumerationUtility() {
  }

  /**
   * Returns an {@link Iterator} that traverses the remaining elements covered by this enumeration. Traversal is
   * undefined if any methods are called on this enumeration after the call to {@code asIterator}.
   *
   * @return an Iterator representing the remaining elements of this Enumeration
   */
  public static <T> Iterator<T> asIterator(final Enumeration<? extends T> enumeration) {
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return enumeration.hasMoreElements();
      }

      @Override
      @SuppressWarnings("squid:S2272")
      public T next() {
        return enumeration.nextElement();
      }
    };
  }

  /**
   * Returns an {@link Enumeration} that traverses the remaining elements covered by this iterator. Traversal is
   * undefined if any methods are called on this iterator after the call to {@code asEnumeration}.
   *
   * @return an Enumeration representing the remaining elements of this Iterator
   */
  public static <T> Enumeration<T> asEnumeration(final Iterator<? extends T> iterator) {
    return new Enumeration<T>() {
      @Override
      public boolean hasMoreElements() {
        return iterator.hasNext();
      }

      @Override
      public T nextElement() {
        return iterator.next();
      }
    };
  }

}
