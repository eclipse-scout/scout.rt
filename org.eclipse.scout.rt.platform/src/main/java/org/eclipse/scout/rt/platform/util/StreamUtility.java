/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Collection of additional {@link Stream} support that is missing in Java 8.
 */
public final class StreamUtility {

  private StreamUtility() {
  }

  /**
   * Returns a sequential ordered <code>Stream</code> produced by consecutive invocation of the <code>next</code>
   * function to an initial element, as long as the given <code>terminate</code> predicate returns <code>false</code>.
   * <p>
   * <b>Note:</b> This method is motivated by Java 9's <code>Stream#iterate(Object, Predicate, UnaryOperator)</code> and
   * can be removed when upgrading.
   */
  public static <T> Stream<T> iterate(T initialElement, Predicate<? super T> hasNext, UnaryOperator<T> next) {
    Assertions.assertNotNull(next);
    Assertions.assertNotNull(hasNext);

    Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.IMMUTABLE) {
      private T m_prev;
      private boolean m_started;
      private boolean m_exhausted;

      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (m_exhausted) {
          return false;
        }
        T currentElement;
        if (m_started) {
          currentElement = next.apply(m_prev);
        }
        else {
          currentElement = initialElement;
          m_started = true;
        }
        if (!hasNext.test(currentElement)) {
          m_prev = null;
          m_exhausted = true;
          return false;
        }
        m_prev = currentElement;
        action.accept(currentElement);
        return true;
      }

      @Override
      public void forEachRemaining(Consumer<? super T> action) {
        Assertions.assertNotNull(action);
        if (m_exhausted) {
          return;
        }
        m_exhausted = true;
        T currentElement = m_started ? next.apply(m_prev) : initialElement;
        m_prev = null;
        while (hasNext.test(currentElement)) {
          action.accept(currentElement);
          currentElement = next.apply(currentElement);
        }
      }
    };

    return StreamSupport.stream(spliterator, false);
  }

  /**
   * Wraps the given stream into one that streams elements as long as the given predicate evaluates to
   * <code>true</code>. The stream returned does not support parallel execution and invoking {@link Stream#parallel()}
   * does not have any effects.
   * <p>
   * <b>Note:</b> This method is motivated by Java 9's <code>Stream#takeWhile(Predicate)</code> and can be removed when
   * upgrading.
   */
  public static <T> Stream<T> takeWhile(Stream<T> stream, Predicate<? super T> predicate) {
    Assertions.assertNotNull(predicate);
    Spliterator<T> streamSpliterator = Assertions.assertNotNull(stream).spliterator();

    Spliterator<T> takeWhileSpleiterator = new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.IMMUTABLE) {
      private boolean m_exhausted;

      @Override
      public Spliterator<T> trySplit() {
        // this implementation does not support parallel execution
        return null;
      }

      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
        Assertions.assertNotNull(action);
        if (m_exhausted) {
          return false;
        }

        return streamSpliterator.tryAdvance(t -> {
          if (predicate.test(t)) {
            action.accept(t);
          }
          else {
            m_exhausted = true;
          }
        });
      }
    };

    return StreamSupport.stream(takeWhileSpleiterator, false);
  }

  /**
   * Returns a {@link Collector} that accumulates input elements into a new {@link LinkedList}, by adding each of them
   * at the beginning.
   * <p>
   * <b>Note:</b> Other than {@link Collectors#toList()}, this collector adds stream elements at the beginning of the
   * resulting list, which effectively results in a reverse ordered list. Hence this collector is a replacement for the
   * following sequence:
   *
   * <pre>
   * Stream&lt;?&gt; s = ...;
   * List&lt;?&gt; list = s.collect(Collectors.toList());
   * Collections.reverse(list);
   * ...
   * </pre>
   */
  public static <T> Collector<T, ?, LinkedList<T>> toReverseList() {
    // Use LinkedList because it allows O(1) insertion at the beginning.
    // ArrayList would require O(n) for every insertion at the beginning, resulting in total costs of O(n^2)!
    return Collector.of(
        LinkedList::new,
        (l, b) -> l.addFirst(b),
        (l1, l2) -> {
          l2.addAll(l1);
          return l2;
        });
  }

  /**
   * Returns a {@code Collector} that accumulates elements into a {@code LinkedHashMap} whose keys and values are the
   * result of applying the provided mapping functions to the input elements.
   *
   * @see Collectors#toMap(Function, Function, BinaryOperator, java.util.function.Supplier)
   */
  public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toLinkedHashMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new);
  }

  /**
   * Returns a merge function which always throws {@code IllegalStateException}.
   *
   * @see Collectors#throwingMerger
   */
  public static <T> BinaryOperator<T> throwingMerger() {
    return (u, v) -> {
      throw new IllegalStateException(String.format("Duplicate key %s", u));
    };
  }
}
