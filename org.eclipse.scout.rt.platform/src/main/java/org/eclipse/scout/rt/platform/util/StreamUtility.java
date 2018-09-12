/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
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
   * Negates a predicate or method reference, e.g. not(Objects::equals).
   * <p>
   * <b>Note:</b> This method is motivated by Java 11's {@link Predicate#not(Predicate)} and can be removed when
   * upgrading.
   */
  public static <T> Predicate<T> not(Predicate<T> p) {
    return p.negate();
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
   * Returns a {@code Collector} that accumulates elements into a {@code HashMap} whose keys and values are the result
   * of applying the provided mapping functions to the input elements.
   * <p>
   * <b>In difference to the default {@link Collectors#toMap()} collector, the map values may be {@code null}.</b> *
   * <p>
   * If the mapped keys contains duplicates (according to {@link Object#equals(Object)}), an
   * {@code IllegalStateException} is thrown when the collection operation is performed. If the mapped keys may have
   * duplicates, use {@link #toMap(Supplier, Function, Function, BiFunction, Characteristics...)} instead.
   *
   * @param <T>
   *          the type of the input elements
   * @param <K>
   *          the output type of the key mapping function
   * @param <U>
   *          the output type of the value mapping function
   * @param keyMapper
   *          a mapping function to produce keys
   * @param valueMapper
   *          a mapping function to produce values
   * @return a {@code Collector} which collects elements into a {@code HashMap} whose keys and values are the result of
   *         applying mapping functions to the input elements
   * @see StreamUtility#toMap(Supplier, Function, Function, BiFunction, Characteristics...)
   */
  public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return toMap(HashMap::new, keyMapper, valueMapper, throwingMerger());
  }

  /**
   * Returns a {@code Collector} that accumulates elements into a {@code LinkedHashMap} whose keys and values are the
   * result of applying the provided mapping functions to the input elements.
   * <p>
   * <b>In difference to the default {@link Collectors#toMap()} collector, the map values may be {@code null}.</b>
   * <p>
   * If the mapped keys contains duplicates (according to {@link Object#equals(Object)}), an
   * {@code IllegalStateException} is thrown when the collection operation is performed. If the mapped keys may have
   * duplicates, use {@link #toMap(Supplier, Function, Function, BiFunction, Characteristics...)} instead.
   *
   * @param <T>
   *          the type of the input elements
   * @param <K>
   *          the output type of the key mapping function
   * @param <U>
   *          the output type of the value mapping function
   * @param keyMapper
   *          a mapping function to produce keys
   * @param valueMapper
   *          a mapping function to produce values
   * @return a {@code Collector} which collects elements into a {@code LinkedHashMap} whose keys and values are the
   *         result of applying mapping functions to the input elements
   * @see StreamUtility#toMap(Supplier, Function, Function, BiFunction, Characteristics...)
   */
  public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toLinkedHashMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return toMap(LinkedHashMap::new, keyMapper, valueMapper, throwingMerger());
  }

  /**
   * Returns a {@code Collector} that accumulates elements into a {@code Map} whose keys and values are the result of
   * applying the provided mapping functions to the input elements.
   * <p>
   * <b>In difference to the default {@link Collectors#toMap()} collector, the map values may be {@code null}.</b>
   * <p>
   * Duplicated keys are merged by applying the specified {@code remappingFunction}.
   *
   * @param <T>
   *          the type of the input elements
   * @param <K>
   *          the output type of the key mapping function
   * @param <U>
   *          the output type of the value mapping function
   * @param <M>
   *          the type of the resulting {@link Map}
   * @param keyMapper
   *          a mapping function to produce keys
   * @param valueMapper
   *          a mapping function to produce values
   * @return a {@code Collector} which collects elements into a {@code Map} whose keys and values are the result of
   *         applying mapping functions to the input elements
   */
  public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(Supplier<M> supplier, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper,
      BiFunction<? super U, ? super U, ? extends U> remappingFunction, Characteristics... characteristics) {
    return Collector.of(
        supplier,
        (map, value) -> putEntry(map, keyMapper.apply(value), valueMapper.apply(value), remappingFunction),
        (map1, map2) -> mergeMap(map1, map2, remappingFunction),
        characteristics);
  }

  /**
   * @returns Resulting merged map after putting all elements of {@code map2} into {@code map1}, applying the
   *          {@code remappingFunction} for duplicated keys.
   */
  private static <K, U, M extends Map<K, U>> M mergeMap(M map1, M map2, BiFunction<? super U, ? super U, ? extends U> remappingFunction) {
    map2.forEach((k, u) -> putEntry(map1, k, u, remappingFunction));
    return map1;
  }

  /**
   * Adds pair of {@code key} and {@code value} into {@code map} applying the {@code remapppingFunction} if the key is
   * already contained within the specified {@code map}.
   */
  private static <K, U> void putEntry(Map<K, U> map, K key, U value, BiFunction<? super U, ? super U, ? extends U> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    U newValue = value;
    if (map.containsKey(key)) {
      U oldValue = map.get(key);
      newValue = remappingFunction.apply(oldValue, value);
    }
    map.put(key, newValue);
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
