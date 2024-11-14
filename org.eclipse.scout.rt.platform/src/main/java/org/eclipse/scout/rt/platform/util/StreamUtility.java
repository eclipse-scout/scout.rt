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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collection of additional {@link Stream} support methods.
 */
public final class StreamUtility {

  private StreamUtility() {
  }

  /**
   * Returns a {@link Collector} that accumulates input elements into a new {@link LinkedList}, by adding each of them
   * at the beginning.
   * <p>
   * <b>Note:</b> Unlike {@link Collectors#toList()}, this collector adds stream elements at the beginning of the
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
        LinkedList::addFirst,
        (l1, l2) -> {
          l2.addAll(l1);
          return l2;
        });
  }

  /**
   * Returns a {@code Collector} that accumulates elements into a {@code HashMap} whose keys and values are the result
   * of applying the provided mapping functions to the input elements.
   * <p>
   * <b>Note:</b> Unlike {@link Collectors#toMap(Function, Function)}, this collector supports map values that are
   * {@code null}.
   * <p>
   * If the mapped keys contains duplicates (according to {@link Object#equals(Object)}), a remapping function which
   * always overrides the existing element is used (e.g. the last value for each key is used). To handle duplicate keys
   * differently, use {@link #toMap(Function, Function, BiFunction)} instead.
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
  public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper) {
    return toMap(HashMap::new, keyMapper, valueMapper, replacingMerger());
  }

  /**
   * Returns a {@code Collector} that accumulates elements into a {@code Map} whose keys and values are the result of
   * applying the provided mapping functions to the input elements.
   * <p>
   * <b>Note:</b> Unlike {@link Collectors#toMap(Function, Function)}, this collector supports map values that are
   * {@code null}.
   * <p>
   * If the mapped keys contains duplicates (according to {@link Object#equals(Object)}), a remapping function which
   * always overrides the existing element is used (e.g. the last value for each key is used). To handle duplicate keys
   * differently, use {@link #toMap(Supplier, Function, Function, BiFunction, Characteristics...)} instead.
   *
   * @param <T>
   *          the type of the input elements
   * @param <K>
   *          the output type of the key mapping function
   * @param <U>
   *          the output type of the value mapping function
   * @param <M>
   *          the type of the resulting {@link Map}
   * @param mapSupplier
   *          a supplier for the resulting map, e.g. <code>HashMap::new</code>
   * @param keyMapper
   *          a function to produce map keys from stream elements
   * @param valueMapper
   *          a function to produce map values from stream elements
   * @return a {@code Collector} which collects elements into a {@code Map} whose keys and values are the result of
   *         applying mapping functions to the input elements
   */
  public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(
      Supplier<M> mapSupplier,
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper) {
    return toMap(mapSupplier, keyMapper, valueMapper, replacingMerger());
  }

  /**
   * Returns a {@code Collector} that accumulates elements into a {@code HashMap} whose keys and values are the result
   * of applying the provided mapping functions to the input elements.
   * <p>
   * <b>Note:</b> Unlike {@link Collectors#toMap(Function, Function)}, this collector supports map values that are
   * {@code null}.
   * <p>
   * Duplicate keys are merged by applying the specified {@code remappingFunction}.
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
   * @param remappingFunction
   *          a function to handle duplicate keys, e.g. {@link #replacingMerger()} or {@link #throwingMerger()}
   * @return a {@code Collector} which collects elements into a {@code HashMap} whose keys and values are the result of
   *         applying mapping functions to the input elements
   * @see StreamUtility#toMap(Supplier, Function, Function, BiFunction, Characteristics...)
   */
  public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper,
      BiFunction<? super U, ? super U, ? extends U> remappingFunction) {
    return toMap(HashMap::new, keyMapper, valueMapper, remappingFunction);
  }

  /**
   * Returns a {@code Collector} that accumulates elements into a {@code Map} whose keys and values are the result of
   * applying the provided mapping functions to the input elements.
   * <p>
   * <b>Note:</b> Unlike {@link Collectors#toMap(Function, Function)}, this collector supports map values that are
   * {@code null}.
   * <p>
   * Duplicate keys are merged by applying the specified {@code remappingFunction}.
   *
   * @param <T>
   *          the type of the input elements
   * @param <K>
   *          the output type of the key mapping function
   * @param <U>
   *          the output type of the value mapping function
   * @param <M>
   *          the type of the resulting {@link Map}
   * @param mapSupplier
   *          a supplier for the resulting map, e.g. <code>HashMap::new</code>
   * @param keyMapper
   *          a function to produce map keys from stream elements
   * @param valueMapper
   *          a function to produce map values from stream elements
   * @param remappingFunction
   *          a function to handle duplicate keys, e.g. {@link #throwingMerger()}
   * @param characteristics
   *          the collector characteristics for the new collector
   * @return a {@code Collector} which collects elements into a {@code Map} whose keys and values are the result of
   *         applying mapping functions to the input elements
   */
  public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(
      Supplier<M> mapSupplier,
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper,
      BiFunction<? super U, ? super U, ? extends U> remappingFunction,
      Characteristics... characteristics) {
    return Collector.of(
        mapSupplier,
        (map, value) -> putEntry(map, keyMapper.apply(value), valueMapper.apply(value), remappingFunction),
        (map1, map2) -> mergeMap(map1, map2, remappingFunction),
        characteristics);
  }

  /**
   * @return Resulting merged map after putting all elements of {@code map2} into {@code map1}, applying the
   *         {@code remappingFunction} for duplicated keys.
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
   * Same as {@link #toMap(Function, Function)} but collects elements into a {@link LinkedHashMap}.
   * <p>
   * This is a convenience for:
   *
   * <pre>
   * toMap(LinkedHashMap::new, keyMapper, valueMapper)
   * </pre>
   */
  public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toLinkedHashMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return toMap(LinkedHashMap::new, keyMapper, valueMapper);
  }

  /**
   * Use this method together with <code>Collectors.toMap(keyMapper, valueMapper, mergeFunction, mapFactory)</code> as
   * argument for the 'mergeFunction'. It does the same thing as the default toMap() merge function
   * <code>Collectors.throwingMerger</code> in JDK 8 and is similar to the default merge function
   * <code>Collectors.uniqKeysMapMerger</code> in JDKs >=9.
   *
   * @return a merge function which always throws {@code IllegalStateException}.
   * @see Collectors#toMap(Function, Function, BinaryOperator, Supplier)
   */
  public static <T> BinaryOperator<T> throwingMerger() {
    return (u, v) -> {
      throw new IllegalStateException(String.format("Duplicate key %s", u));
    };
  }

  /**
   * Returns a merge function which always uses the existing element (<i>first-come-first-serve</i>).
   */
  public static <T> BinaryOperator<T> ignoringMerger() {
    return (u, v) -> u;
  }

  /**
   * Returns a merge function which always overrides the existing element (<i>last-wins</i>).
   */
  public static <T> BinaryOperator<T> replacingMerger() {
    return (u, v) -> v;
  }

  /**
   * Creates and returns a lazily concatenated stream whose elements are all the elements of the first stream followed
   * by all the elements of the second stream and so on. The {@link Stream#concat(Stream, Stream)} function is used to
   * concatenate the streams.
   *
   * @implNote Use caution when concatenating a large number of streams as repeated concatenation will result in a
   *           perfectly unbalanced tree of streams result in deep call chains, or even StackOverflowError. see
   *           {@link Stream#concat(Stream, Stream)} for more details
   */
  @SafeVarargs
  public static <T> Stream<T> concat(Stream<? extends T>... streams) {
    if (streams == null) {
      return Stream.empty();
    }
    return Arrays.stream(streams)
        .filter(Objects::nonNull)
        .reduce(Stream.empty(), Stream::concat, Stream::concat);
  }
}
