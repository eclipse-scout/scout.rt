/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.lookup;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.CollatorProvider;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.StreamUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Helper class filtering and mapping a stream of (in-memory available) data items to list of lookup rows.
 */
@ApplicationScoped
public class LookupHelper {

  protected static final int DEFAULT_MAX_ROWS = 100;

  protected static final String WILDCARD = "*";
  protected static final String WILDCARD_REPLACE = "@wildcard@";
  protected static final String MATCH_ALL_REGEX = ".*";

  /**
   * Filter stream of {@code data} according to specified {@code restriction} and converts the stream to a
   * LookupResponse containing a list of mapped lookup rows.
   *
   * @param <LOOKUP_ROW>
   *     Type of lookup row (subclass of {@link AbstractLookupRowDo})
   * @param <ID>
   *     Primary key type of data items
   * @param <RESTRICTION>
   *     Type of lookup restriction (subclass of {@link AbstractLookupRestrictionDo}
   * @param <DATA>
   *     Type of data items
   * @param restriction
   *     Lookup call restriction object used to filter the stream of data
   * @param data
   *     Stream of data items
   * @param idAccessor
   *     Accessor method to get the ID of a data item
   * @param textAccessor
   *     Accessor method to get the text of a data item
   * @param rowClass
   *     Class type of lookup row to create
   */
  public <LOOKUP_ROW extends AbstractLookupRowDo<ID>, ID, RESTRICTION extends AbstractLookupRestrictionDo<ID>, DATA> LookupResponse<LOOKUP_ROW> filterData(RESTRICTION restriction,
      Stream<DATA> data,
      Function<DATA, ID> idAccessor,
      Function<DATA, String> textAccessor,
      Class<LOOKUP_ROW> rowClass) {
    return filterData(restriction, data, idAccessor, textAccessor, null, truePredicate(), rowClass, identityMapper(), lookupRowDoComparatorByText());
  }

  /**
   * Filter stream of {@code data} according to specified {@code restriction} and converts the stream to a
   * LookupResponse containing a list of mapped lookup rows.
   *
   * @param <LOOKUP_ROW>
   *     Type of lookup row (subclass of {@link AbstractLookupRowDo})
   * @param <ID>
   *     Primary key type of data items
   * @param <RESTRICTION>
   *     Type of lookup restriction (subclass of {@link AbstractLookupRestrictionDo}
   * @param <DATA>
   *     Type of data items
   * @param restriction
   *     Lookup call restriction object used to filter the stream of data
   * @param data
   *     Stream of data items
   * @param idAccessor
   *     Accessor method to get the ID of a data item
   * @param textAccessor
   *     Accessor method to get the text of a data item
   * @param rowClass
   *     Class type of lookup row to create
   */
  public <LOOKUP_ROW extends AbstractLookupRowDo<ID>, ID, RESTRICTION extends AbstractLookupRestrictionDo<ID>, DATA> LookupResponse<LOOKUP_ROW> filterData(RESTRICTION restriction,
      Stream<DATA> data,
      Function<DATA, ID> idAccessor,
      Function<DATA, String> textAccessor,
      Class<LOOKUP_ROW> rowClass,
      Comparator<LOOKUP_ROW> lookupRowDoComparator) {
    return filterData(restriction, data, idAccessor, textAccessor, null, truePredicate(), rowClass, identityMapper(), lookupRowDoComparator);
  }

  /**
   * Filter stream of {@code data} according to specified {@code restriction} and converts the stream to a
   * LookupResponse containing a list of mapped lookup rows.
   *
   * @param <LOOKUP_ROW>
   *     Type of lookup row (subclass of {@link AbstractLookupRowDo})
   * @param <ID>
   *     Primary key type of data items
   * @param <RESTRICTION>
   *     Type of lookup restriction (subclass of {@link AbstractLookupRestrictionDo}
   * @param <DATA>
   *     Type of data items
   * @param restriction
   *     Lookup call restriction object used to filter the stream of data
   * @param data
   *     Stream of data items
   * @param idAccessor
   *     Accessor method to get the ID of a data item
   * @param textAccessor
   *     Accessor method to get the text of a data item
   * @param activeAccessor
   *     Accessor method to get the active state of a data item
   * @param rowClass
   *     Class type of lookup row to create
   */
  public <LOOKUP_ROW extends AbstractLookupRowDo<ID>, ID, RESTRICTION extends AbstractLookupRestrictionDo<ID>, DATA> LookupResponse<LOOKUP_ROW> filterData(RESTRICTION restriction,
      Stream<DATA> data,
      Function<DATA, ID> idAccessor,
      Function<DATA, String> textAccessor,
      Function<DATA, Boolean> activeAccessor,
      Class<LOOKUP_ROW> rowClass) {
    return filterData(restriction, data, idAccessor, textAccessor, activeAccessor, truePredicate(), rowClass, identityMapper(), lookupRowDoComparatorByText());
  }

  /**
   * Filter stream of {@code data} according to specified {@code restriction} and <b> using an additional custom filter
   * {@code additionalFilter}</b> and converts the stream to a LookupResponse containing a list of mapped lookup rows.
   *
   * @param additionalFilter
   *     Additional filter for stream of data
   * @see LookupHelper#filterData(AbstractLookupRestrictionDo, Stream, Function, Function, Class)
   */
  public <LOOKUP_ROW extends AbstractLookupRowDo<ID>, ID, RESTRICTION extends AbstractLookupRestrictionDo<ID>, DATA> LookupResponse<LOOKUP_ROW> filterData(RESTRICTION restriction,
      Stream<DATA> data,
      Function<DATA, ID> idAccessor,
      Function<DATA, String> textAccessor,
      Function<DATA, Boolean> activeAccessor,
      Predicate<DATA> additionalFilter,
      Class<LOOKUP_ROW> rowClass) {
    return filterData(restriction, data, idAccessor, textAccessor, activeAccessor, additionalFilter, rowClass, identityMapper(), lookupRowDoComparatorByText());
  }

  /**
   * Filter stream of {@code data} according to specified {@code restriction} and <b> using an additional custom filter
   * {@code additionalFilter}</b>. Converts the stream to a LookupResponse containing a list of mapped lookup rows
   * <b>using an additional custom {@code additionalMapper}</b>
   *
   * @param additionalFilter
   *     Additional filter for stream of data
   * @param additionalMapper
   *     Additional mapper to map custom properties from data object to lookup row type
   * @param lookupRowDoComparator
   *     Comparator the resulting {@link AbstractLookupRowDo} as sorted with. No sorting if Comparator is null.
   * @see LookupHelper#filterData(AbstractLookupRestrictionDo, Stream, Function, Function, Class)
   */
  @SuppressWarnings("squid:S00107")
  public <LOOKUP_ROW extends AbstractLookupRowDo<ID>, ID, RESTRICTION extends AbstractLookupRestrictionDo<ID>, DATA> LookupResponse<LOOKUP_ROW> filterData(RESTRICTION restriction,
      Stream<DATA> dataStream,
      Function<DATA, ID> idAccessor,
      Function<DATA, String> textAccessor,
      Function<DATA, Boolean> activeAccessor,
      Predicate<DATA> additionalFilter,
      Class<LOOKUP_ROW> rowClass,
      BiFunction<LOOKUP_ROW, DATA, LOOKUP_ROW> additionalMapper,
      Comparator<LOOKUP_ROW> lookupRowDoComparator) {

    Stream<LOOKUP_ROW> stream = dataStream
        .filter(restrictionPredicate(restriction, idAccessor, textAccessor, activeAccessor))
        .filter(additionalFilter)
        .map(data -> {
          LOOKUP_ROW row = BEANS.get(rowClass);
          row
              .withId(idAccessor.apply(data))
              .withText(textAccessor.apply(data));
          if (activeAccessor != null) {
            row.withActive(activeAccessor.apply(data));
          }
          return additionalMapper.apply(row, data);
        });
    if (lookupRowDoComparator != null) {
      stream = stream.sorted(lookupRowDoComparator);
    }
    List<LOOKUP_ROW> rows = stream.collect(Collectors.toList());
    return LookupResponse.create(rows);
  }

  /**
   * Convenience method for filtering values of an {@link IEnum} and transforming them into
   * {@link AbstractLookupRowDo}s.
   * <p>
   * This method does not support applying additional filters or mapping additional properties. Use one of the
   * {@code filterData} methods instead.
   *
   * @param restriction
   *     Lookup call restriction object used to filter the stream of data
   * @param enumClass
   *     {@link IEnum} class all values are taken from
   * @param rowClass
   *     Class type of lookup row to create
   */
  public <LOOKUP_ROW extends AbstractLookupRowDo<ENUM>, ENUM extends Enum<?> & IEnum, RESTRICTION extends AbstractLookupRestrictionDo<ENUM>> LookupResponse<LOOKUP_ROW> filterEnum(RESTRICTION restriction,
      Class<ENUM> enumClass,
      Class<LOOKUP_ROW> rowClass) {
    return filterData(restriction,
        Arrays.stream(enumClass.getEnumConstants()),
        Function.identity(),
        enumTextResolver(),
        rowClass);
  }

  /**
   * Convenience method for filtering values of an {@link IEnum} and transforming them into
   * {@link AbstractLookupRowDo}s. It keeps the natural sorting of the value as defined in {@link IEnum}.
   * <p>
   * This method does not support applying additional filters or mapping additional properties. Use one of the
   * {@code filterData} methods instead.
   *
   * @param restriction
   *     Lookup call restriction object used to filter the stream of data
   * @param enumClass
   *     {@link IEnum} class all values are taken from
   * @param rowClass
   *     Class type of lookup row to create
   */
  public <LOOKUP_ROW extends AbstractLookupRowDo<ENUM>, ENUM extends Enum<?> & IEnum, RESTRICTION extends AbstractLookupRestrictionDo<ENUM>> LookupResponse<LOOKUP_ROW> filterEnumKeepSorting(RESTRICTION restriction,
      Class<ENUM> enumClass,
      Class<LOOKUP_ROW> rowClass) {
    return filterData(restriction,
        Arrays.stream(enumClass.getEnumConstants()),
        Function.identity(),
        enumTextResolver(),
        null,
        truePredicate(),
        rowClass,
        identityMapper(),
        null);
  }

  /**
   * @return {@link AbstractLookupRestrictionDo#maxRowCount()} if not {@code null}, {@link #DEFAULT_MAX_ROWS} otherwise.
   */
  public int maxRowCount(AbstractLookupRestrictionDo<?> restriction) {
    return NumberUtility.nvl(restriction.getMaxRowCount(), DEFAULT_MAX_ROWS);
  }

  /**
   * {@link Predicate} which is always true
   */
  public static <T> Predicate<T> truePredicate() {
    return t -> true;
  }

  /**
   * Identity data mapper
   */
  public static <T, R> BiFunction<T, R, T> identityMapper() {
    return (r, d) -> r;
  }

  /**
   * {@link Function} that resolves the text of an {@link IEnum} using {@link IEnum#text()} and
   * {@link TEXTS#get(String)}. Both, <code>null</code> enums as well as <code>null</code> textKeys are resolved to
   * <code>null</code>.
   */
  public static <ENUM extends IEnum> Function<ENUM, String> enumTextResolver() {
    return e -> e == null ? null : e.text();
  }

  /**
   * {@link Comparator} working on {@link AbstractLookupRowDo#getText()}.
   */
  public static <LOOKUP_ROW extends AbstractLookupRowDo<?>> Comparator<LOOKUP_ROW> lookupRowDoComparatorByText() {
    Collator collator = BEANS.get(CollatorProvider.class).getInstance();
    collator.setStrength(Collator.TERTIARY);
    return Comparator.comparing(AbstractLookupRowDo::getText, Comparator.nullsFirst(collator));
  }

  /**
   * {@link Predicate} using the given {@code restriction} and the provided accessors {@code idAccessor} and
   * {@code textAccessor}. The predicate reflects the restrictions provided with
   * {@link AbstractLookupRestrictionDo#ids()} and {@link AbstractLookupRestrictionDo#text()}.
   * <p>
   * If {@code restriction} is {@code null}, {@link #truePredicate()} is returned.
   */
  public <ID, RESTRICTION extends AbstractLookupRestrictionDo<ID>, DATA> Predicate<DATA> restrictionPredicate(RESTRICTION restriction,
      Function<DATA, ID> idAccessor,
      Function<DATA, String> textAccessor,
      Function<DATA, Boolean> activeAccessor) {
    if (restriction == null) {
      return truePredicate();
    }
    List<ID> ids = restriction.getIds();
    Predicate<DATA> predicate = textPatternPredicate(restriction.getText(), textAccessor);
    if (idAccessor != null && !ids.isEmpty()) {
      predicate = predicate.and(data -> ids.contains(idAccessor.apply(data)));
    }
    if (activeAccessor != null) {
      predicate = predicate.and(activePredicate(restriction.getActive(), activeAccessor));
    }
    return predicate;
  }

  /**
   * {@link Predicate} using the given {@code textPattern} converted into a {@link Pattern} and the given
   * {@code textAccessor} applied on the object the predicate is working on. If {@code textPattern} is {@code null},
   * {@link #truePredicate()} is returned.
   */
  public <DATA> Predicate<DATA> textPatternPredicate(String textPattern, Function<DATA, String> textAccessor) {
    if (textPattern == null) {
      return truePredicate();
    }
    Assertions.assertNotNull(textAccessor, "textAccessor is required");
    Pattern pattern = createTextSearchPattern(textPattern);
    return data -> {
      if (data == null) {
        return false;
      }
      String text = textAccessor.apply(data);
      return text != null && pattern.matcher(text).matches();
    };
  }

  /**
   * {@link Predicate} using the given {@code active} value to filter.
   * <p>
   * If {@code active} is {@code null}, {@link #truePredicate()} is returned.
   */
  public <DATA> Predicate<DATA> activePredicate(Boolean active, Function<DATA, Boolean> activeAccessor) {
    if (active == null) {
      return truePredicate();
    }
    Assertions.assertNotNull(activeAccessor, "activeAccessor is required");
    boolean activeBoolean = active.booleanValue();
    return data -> {
      if (data == null) {
        return false;
      }
      Boolean dataActive = activeAccessor.apply(data);
      return dataActive != null && dataActive.booleanValue() == activeBoolean;
    };
  }

  /**
   * Text lookup pattern
   */
  protected Pattern createTextSearchPattern(String text) {
    if (text == null) {
      text = "";
    }
    text = text.replace(WILDCARD, WILDCARD_REPLACE);
    text = StringUtility.escapeRegexMetachars(text);
    text = text.replace(WILDCARD_REPLACE, MATCH_ALL_REGEX);
    if (!text.contains(MATCH_ALL_REGEX)) {
      text = text + MATCH_ALL_REGEX;
    }
    if (!text.startsWith(MATCH_ALL_REGEX)) {
      text = MATCH_ALL_REGEX + text;
    }
    return Pattern.compile(text, Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  }

  /**
   * Extracts ids form the given values and resolves them.
   */
  public <V, ID> Map<ID, String> resolve(
      Stream<V> values,
      Function<V, ID> idExtractor,
      Function<Set<ID>, List<? extends AbstractLookupRowDo<ID>>> textResolver) {
    Set<ID> ids = values
        .map(idExtractor)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    return textResolver.apply(ids)
        .stream()
        .collect(StreamUtility.toMap(AbstractLookupRowDo::getId, AbstractLookupRowDo::getText));
  }
}
