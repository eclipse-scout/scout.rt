/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static java.util.stream.StreamSupport.stream;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

  /**
   * Converts the given {@link Enumeration} into a {@link Stream}. The {@link Enumeration} is evaluated lazy by the
   * {@link Stream}.<br>
   * Because {@link Enumeration enumerations} cannot be reset a fresh instance should be passed to this method.
   * Otherwise the resulting {@link Stream} only processes the remaining elements of the {@link Enumeration}.
   *
   * @param e
   *     The {@link Enumeration} to convert. Must not be {@code null}.
   * @return A non-parallel, ordered {@link Stream} backed by the {@link Enumeration} given.
   */
  public static <T> Stream<T> asStream(Enumeration<T> e) {
    Assertions.assertNotNull(e);
    Spliterator<T> spliterator = new AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {
      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
        if (!e.hasMoreElements()) {
          return false;
        }
        action.accept(e.nextElement());
        return true;
      }

      @Override
      public void forEachRemaining(Consumer<? super T> action) {
        while (e.hasMoreElements()) {
          action.accept(e.nextElement());
        }
      }
    };
    return stream(spliterator, false);
  }
}
