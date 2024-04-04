/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.shared.data.basic.chart;

import static org.eclipse.scout.rt.platform.util.StringUtility.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.shared.data.colorscheme.IColorScheme;

public class ChartConfig implements IChartConfig {

  private static final long serialVersionUID = 1L;

  protected static final String DELIMITER = ".";
  protected static final String ESCAPED_DELIMITER = escapeRegexMetachars(DELIMITER);
  protected static final String ARRAY_INDEX = arrayIndex("\\d+", true);
  protected static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile(ARRAY_INDEX);
  protected static final Pattern LIST_EXPRESSION_PATTERN = Pattern.compile("\\w+" + ARRAY_INDEX);
  protected static final Pattern PROPERTY_PATTERN = Pattern.compile("\\w+(" + ARRAY_INDEX + ")?(" + ESCAPED_DELIMITER + "\\w+(" + ARRAY_INDEX + ")?)*");

  protected static final String TYPE = "type";
  protected static final String OPTIONS = "options";
  protected static final String PLUGINS = combine(OPTIONS, "plugins");
  protected static final String AUTO_COLOR = combine(OPTIONS, "autoColor");
  protected static final String COLOR_MODE = combine(OPTIONS, "colorMode");
  protected static final String COLOR_SCHEME = combine(OPTIONS, "colorScheme");
  protected static final String TRANSPARENT = combine(OPTIONS, "transparent");
  protected static final String MAX_SEGMENTS = combine(OPTIONS, "maxSegments");
  protected static final String CLICKABLE = combine(OPTIONS, "clickable");
  protected static final String CHECKABLE = combine(OPTIONS, "checkable");
  protected static final String ANIMATION = combine(OPTIONS, "animation");
  protected static final String ANIMATION_DURATION = combine(ANIMATION, "duration");
  protected static final String TOOLTIP = combine(PLUGINS, "tooltip");
  protected static final String TOOLTIP_ENABLED = combine(TOOLTIP, "enabled");
  protected static final String LEGEND = combine(PLUGINS, "legend");
  protected static final String LEGEND_DISPLAY = combine(LEGEND, "display");
  protected static final String LEGEND_CLICKABLE = combine(LEGEND, "clickable");
  protected static final String LEGEND_POSITION = combine(LEGEND, "position");
  protected static final String ELEMENTS = combine(OPTIONS, "elements");
  protected static final String LINE = combine(ELEMENTS, "line");
  protected static final String LINE_TENSION = combine(LINE, "tension");
  protected static final String LINE_FILL = combine(LINE, "fill");
  protected static final String SCALES = combine(OPTIONS, "scales");
  protected static final String SCALE_LABEL_BY_TYPE_MAP = combine(OPTIONS, "scaleLabelByTypeMap");
  protected static final String X_LABEL_MAP = combine(OPTIONS, "xLabelMap");
  protected static final String Y_LABEL_MAP = combine(OPTIONS, "yLabelMap");
  protected static final String X = combine(SCALES, "x");
  protected static final String Y = combine(SCALES, "y");
  protected static final String STACKED = "stacked";
  protected static final String SCALE_TITLE = "title";
  protected static final String SCALE_TITLE_DISPLAY = combine(SCALE_TITLE, "display");
  protected static final String SCALE_TITLE_TEXT = combine(SCALE_TITLE, "text");
  protected static final String DATALABELS = combine(PLUGINS, "datalabels");
  protected static final String DATALABELS_DISPLAY = combine(DATALABELS, "display");

  private final Map<String, Object> m_properties = new HashMap<>();

  protected static String combine(String... elements) {
    return join(DELIMITER, elements);
  }

  protected static String arrayIndex(int index) {
    return arrayIndex(String.valueOf(index));
  }

  protected static String arrayIndex(String index) {
    return arrayIndex(index, false);
  }

  protected static String arrayIndex(String index, boolean regex) {
    return box(regex ? escapeRegexMetachars("[") : "[", index, regex ? escapeRegexMetachars("]") : "]");
  }

  @Override
  public ChartConfig copy() {
    ChartConfig chartConfig = BEANS.get(ChartConfig.class);
    chartConfig.m_properties.putAll(m_properties);
    return chartConfig;
  }

  /**
   * Support for different modifying operations of the {@link #m_properties} map. The different {@link ModifyMode}s
   * support validation of a given name-value-pair and performing their specific operation.
   */
  protected enum ModifyMode {
    PUT {
      @Override
      protected boolean validate(String name, Object value) {
        return super.validate(name, value) && value != null;
      }

      @Override
      protected boolean isCreateMissing() {
        return true;
      }

      @Override
      protected Object modify(Map<String, Object> map, String name, Object value) {
        if (map == null) {
          throw new IllegalArgumentException("The given Map is null.");
        }
        return map.put(name, value);
      }
    },
    REMOVE {
      @Override
      protected Object modify(Map<String, Object> map, String name, Object value) {
        if (CollectionUtility.isEmpty(map)) {
          return null;
        }
        Pair<String, Integer> nameIndexPair = ChartConfig.splitListExpression(name);
        if (nameIndexPair != null) {
          Object obj = map.get(nameIndexPair.getLeft());
          if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> l = (List<Map<String, Object>>) obj;
            if (nameIndexPair.getRight() >= 0 && nameIndexPair.getRight() < l.size()) {
              Map<String, Object> element = CollectionUtility.getElement(l, nameIndexPair.getRight());
              l.add(nameIndexPair.getRight(), new HashMap<>());
              return element;
            }
          }
        }
        else {
          return map.remove(name);
        }
        return null;
      }
    },
    GET {
      @Override
      protected Object modify(Map<String, Object> map, String name, Object value) {
        if (CollectionUtility.isEmpty(map)) {
          return null;
        }
        Pair<String, Integer> nameIndexPair = ChartConfig.splitListExpression(name);
        if (nameIndexPair != null) {
          Object obj = map.get(nameIndexPair.getLeft());
          if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> l = (List<Map<String, Object>>) obj;
            return CollectionUtility.getElement(l, nameIndexPair.getRight());
          }
        }
        else {
          return map.get(name);
        }
        return null;
      }
    };

    protected boolean validate(String name, Object value) {
      return PROPERTY_PATTERN.matcher(name).matches();
    }

    protected boolean isCreateMissing() {
      return false;
    }

    protected abstract Object modify(Map<String, Object> map, String name, Object value);
  }

  @Override
  public IChartConfig withProperty(String name, Object value) {
    modifyProperties(ModifyMode.PUT, name, value);
    return this;
  }

  @Override
  public IChartConfig removeProperty(String name) {
    modifyProperties(ModifyMode.REMOVE, name, null);
    return this;
  }

  @Override
  public Object getProperty(String name) {
    return modifyProperties(ModifyMode.GET, name, null);
  }

  protected IChartConfig withArrayProperty(String array, int index, String name, Object value) {
    return withProperty(combine(array + arrayIndex(index), name), value);
  }

  protected IChartConfig removeArrayProperty(String array, int index, String name) {
    return removeProperty(combine(array + arrayIndex(index), name));
  }

  protected Object getArrayProperty(String array, int index, String name) {
    return getProperty(combine(array + arrayIndex(index), name));
  }

  /**
   * Modifies the {@link #m_properties} map depending on the given {@link ModifyMode}.
   *
   * @param modifyMode
   *          Specific {@link ModifyMode} like put or remove.
   * @param name
   *          The name of the property. Recursive properties have to be separated by "." and indices of arrays have to
   *          be specified in the form "[i]" (e.g. "options.legend.position", "options.scales.xAxes[0].scaleLabel").
   * @param value
   *          The value of the property or null for {@link ModifyMode}s that don't need a value.
   * @return The Object returned by the specific operation.
   */
  protected Object modifyProperties(ModifyMode modifyMode, String name, Object value) {
    if (modifyMode == null || !modifyMode.validate(name, value)) {
      throw new IllegalArgumentException("The given ModifyMode is null or validating it failed.");
    }

    List<String> namesRec = CollectionUtility.arrayList(name.split(ESCAPED_DELIMITER));
    String propertyName = CollectionUtility.lastElement(namesRec);
    Map<String, Object> map = m_properties;
    if (namesRec.size() > 1) {
      AtomicReference<Map<String, Object>> atomicMap = new AtomicReference<>(map);
      //noinspection ResultOfMethodCallIgnored
      CollectionUtility.slice(namesRec, 0, -2).stream().filter(nameRec -> {
        Pair<String, Integer> nameIndexPair = splitListExpression(nameRec);
        if (nameIndexPair != null) {
          nameRec = nameIndexPair.getLeft();
        }
        Object obj = atomicMap.get().get(nameRec);
        if (obj == null && modifyMode.isCreateMissing()) {
          Map<String, Object> m;
          if (nameIndexPair != null) {
            List<Map<String, Object>> l = Stream.generate(HashMap<String, Object>::new)
                .limit(nameIndexPair.getRight() + 1)
                .collect(Collectors.toList());
            atomicMap.get().put(nameRec, l);
            m = l.get(nameIndexPair.getRight());
          }
          else {
            m = new HashMap<>();
            atomicMap.get().put(nameRec, m);
          }
          atomicMap.set(m);
        }
        else if (nameIndexPair == null && obj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> m = (Map<String, Object>) obj;
          atomicMap.set(m);
        }
        else if (nameIndexPair != null && obj instanceof List) {
          @SuppressWarnings("unchecked")
          List<Map<String, Object>> l = (List<Map<String, Object>>) obj;
          if (l.size() < nameIndexPair.getRight() + 1) {
            l.addAll(Stream.generate(HashMap<String, Object>::new)
                .limit(nameIndexPair.getRight() + 1 - l.size())
                .collect(Collectors.toList()));
          }
          atomicMap.set(l.get(nameIndexPair.getRight()));
        }
        else {
          atomicMap.set(null);
          return true;
        }
        return false;
      }).findFirst();
      map = atomicMap.get();
    }
    return modifyMode.modify(map, propertyName, value);
  }

  /**
   * Splits a given list expression into the name of the list and the index (e.g. "list[3]" -> "list", 3).
   *
   * @param listExpression
   *          The list expression in the form name of the list followed by the index in square brackets.
   * @return A {@link Pair} containing the name of the list as left and the index as right value or {@code null} if the
   *         given listExpression does not match the required form.
   */
  protected static Pair<String, Integer> splitListExpression(String listExpression) {
    Matcher arrayIndexMatcher = ARRAY_INDEX_PATTERN.matcher(listExpression);
    if (LIST_EXPRESSION_PATTERN.matcher(listExpression).matches() && arrayIndexMatcher.find()) {
      int start = arrayIndexMatcher.start();
      String indexString = listExpression.substring(start).replaceAll("\\D", "");
      return new ImmutablePair<>(listExpression.substring(0, start), TypeCastUtility.castValue(indexString, Integer.class));
    }
    return null;
  }

  @Override
  public Map<String, Object> getProperties() {
    return m_properties;
  }

  @Override
  public Map<String, Object> getPropertiesFlat() {
    return getPropertiesFlatRec(m_properties);
  }

  protected Map<String, Object> getPropertiesFlatRec(Map<String, Object> properties) {
    return getPropertiesFlatRec(properties, null);
  }

  protected Map<String, Object> getPropertiesFlatRec(Map<String, Object> properties, String parentProperty) {
    Map<String, Object> result = new HashMap<>();
    if (properties == null) {
      return result;
    }

    properties.forEach((key, value) -> {
      String property = join(DELIMITER, parentProperty, key);
      if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;
        result.putAll(getPropertiesFlatRec(map, property));
      }
      else if (value instanceof List) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) value;
        for (int i = 0; i < list.size(); i++) {
          result.putAll(getPropertiesFlatRec(list.get(i), property + "[" + i + "]"));
        }
      }
      else if (value != null) {
        result.put(property, value);
      }
    });

    return result;
  }

  @Override
  public IChartConfig addProperties(IChartConfig config, boolean override) {
    if (config != null && !CollectionUtility.isEmpty(config.getProperties())) {
      Map<String, Object> source = override ? config.getProperties() : m_properties;
      Map<String, Object> target = override ? m_properties : config.getProperties();
      Map<String, Object> propertiesNew = putAllRec(source, target);
      m_properties.clear();
      m_properties.putAll(propertiesNew);
    }
    return this;
  }

  protected Map<String, Object> putAllRec(Map<String, Object> source, Map<String, Object> target) {
    if (CollectionUtility.isEmpty(source) && CollectionUtility.isEmpty(target)) {
      return new HashMap<>();
    }
    else if (CollectionUtility.isEmpty(source)) {
      return new HashMap<>(target);
    }
    else if (CollectionUtility.isEmpty(target)) {
      return new HashMap<>(source);
    }

    Map<String, Object> result = new HashMap<>();

    Set<String> properties = new HashSet<>();
    properties.addAll(source.keySet());
    properties.addAll(target.keySet());

    properties.forEach(property -> {
      Object sourceObj = source.get(property);
      Object targetObj = target.get(property);
      if (sourceObj == null) {
        result.put(property, targetObj);
      }
      else if (sourceObj instanceof Map && targetObj instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> sourceMap = (Map<String, Object>) sourceObj;
        @SuppressWarnings("unchecked")
        Map<String, Object> targetMap = (Map<String, Object>) targetObj;
        result.put(property, putAllRec(sourceMap, targetMap));
      }
      else if (sourceObj instanceof List && targetObj instanceof List) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sourceList = (List<Map<String, Object>>) sourceObj;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> targetList = (List<Map<String, Object>>) targetObj;

        for (int i = 0; i < Math.max(sourceList.size(), targetList.size()); i++) {
          resultList.add(putAllRec(i < sourceList.size() ? sourceList.get(i) : null, i < targetList.size() ? targetList.get(i) : null));
        }

        result.put(property, resultList);
      }
      else {
        result.put(property, sourceObj);
      }
    });

    return result;
  }

  @Override
  public IChartConfig withType(String type) {
    return withProperty(TYPE, type);
  }

  @Override
  public IChartConfig removeType() {
    return removeProperty(TYPE);
  }

  @Override
  public String getType() {
    return (String) getProperty(TYPE);
  }

  @Override
  public IChartConfig withAutoColor(boolean autoColor) {
    return withProperty(AUTO_COLOR, autoColor);
  }

  @Override
  public IChartConfig removeAutoColor() {
    return removeProperty(AUTO_COLOR);
  }

  @Override
  public boolean isAutoColor() {
    return BooleanUtility.nvl((Boolean) getProperty(AUTO_COLOR));
  }

  @Override
  public IChartConfig withColorMode(ColorMode colorMode) {
    return withProperty(COLOR_MODE, colorMode != null ? colorMode.getValue() : null);
  }

  @Override
  public IChartConfig removeColorMode() {
    return removeProperty(COLOR_MODE);
  }

  @Override
  public ColorMode getColorMode() {
    return ColorMode.parse((String) getProperty(COLOR_MODE));
  }

  @Override
  public IChartConfig withColorScheme(IColorScheme colorScheme) {
    return withProperty(COLOR_SCHEME, colorScheme != null ? colorScheme.getIdentifier() : null);
  }

  @Override
  public IChartConfig removeColorScheme() {
    return removeProperty(COLOR_SCHEME);
  }

  @Override
  public IColorScheme getColorScheme() {
    return IColorScheme.parse((String) getProperty(COLOR_SCHEME));
  }

  @Override
  public IChartConfig withTransparent(boolean transparent) {
    return withProperty(TRANSPARENT, transparent);
  }

  @Override
  public IChartConfig removeTransparent() {
    return removeProperty(TRANSPARENT);
  }

  @Override
  public boolean isTransparent() {
    return BooleanUtility.nvl((Boolean) getProperty(TRANSPARENT));
  }

  @Override
  public IChartConfig withMaxSegments(int maxSegments) {
    return withProperty(MAX_SEGMENTS, maxSegments);
  }

  @Override
  public IChartConfig removeMaxSegments() {
    return removeProperty(MAX_SEGMENTS);
  }

  @Override
  public int getMaxSegments() {
    return (int) getProperty(MAX_SEGMENTS);
  }

  @Override
  public IChartConfig withClickable(boolean clickable) {
    return withProperty(CLICKABLE, clickable);
  }

  @Override
  public IChartConfig removeClickable() {
    return removeProperty(CLICKABLE);
  }

  @Override
  public boolean isClickable() {
    return BooleanUtility.nvl((Boolean) getProperty(CLICKABLE));
  }

  @Override
  public IChartConfig withCheckable(boolean checkable) {
    return withProperty(CHECKABLE, checkable);
  }

  @Override
  public IChartConfig removeCheckable() {
    return removeProperty(CHECKABLE);
  }

  @Override
  public boolean isCheckable() {
    return BooleanUtility.nvl((Boolean) getProperty(CHECKABLE));
  }

  @Override
  public IChartConfig withAnimationDuration(int duration) {
    return withProperty(ANIMATION_DURATION, duration);
  }

  @Override
  public IChartConfig removeAnimationDuration() {
    return removeProperty(ANIMATION_DURATION);
  }

  @Override
  public int getAnimationDuration() {
    return (int) getProperty(ANIMATION_DURATION);
  }

  protected int getDefaultAnimationDuration() {
    return 600;
  }

  @Override
  public IChartConfig withAnimated(boolean animated) {
    return withAnimationDuration(animated ? getDefaultAnimationDuration() : 0);
  }

  @Override
  public IChartConfig removeAnimated() {
    return removeAnimationDuration();
  }

  @Override
  public boolean isAnimated() {
    return getAnimationDuration() > 0;
  }

  @Override
  public IChartConfig withTooltipsEnabled(boolean tooltipsEnabled) {
    return withProperty(TOOLTIP_ENABLED, tooltipsEnabled);
  }

  @Override
  public IChartConfig removeTooltipsEnabled() {
    return removeProperty(TOOLTIP_ENABLED);
  }

  @Override
  public boolean isTooltipsEnabled() {
    return BooleanUtility.nvl((Boolean) getProperty(TOOLTIP_ENABLED));
  }

  @Override
  public IChartConfig withLegendDisplay(boolean legendDisplay) {
    return withProperty(LEGEND_DISPLAY, legendDisplay);
  }

  @Override
  public IChartConfig removeLegendDisplay() {
    return removeProperty(LEGEND_DISPLAY);
  }

  @Override
  public boolean isLegendDisplay() {
    return BooleanUtility.nvl((Boolean) getProperty(LEGEND_DISPLAY));
  }

  @Override
  public IChartConfig withLegendClickable(boolean legendClickable) {
    return withProperty(LEGEND_CLICKABLE, legendClickable);
  }

  @Override
  public IChartConfig removeLegendClickable() {
    return removeProperty(LEGEND_CLICKABLE);
  }

  @Override
  public boolean isLegendClickable() {
    return BooleanUtility.nvl((Boolean) getProperty(LEGEND_CLICKABLE));
  }

  @Override
  public IChartConfig withLegendPosition(String legendPosition) {
    return withProperty(LEGEND_POSITION, legendPosition);
  }

  @Override
  public IChartConfig removeLegendPosition() {
    return removeProperty(LEGEND_POSITION);
  }

  @Override
  public String getLegendPosition() {
    return (String) getProperty(LEGEND_POSITION);
  }

  @Override
  public IChartConfig withLegendPositionTop() {
    return withLegendPosition(TOP);
  }

  @Override
  public IChartConfig withLegendPositionBottom() {
    return withLegendPosition(BOTTOM);
  }

  @Override
  public IChartConfig withLegendPositionLeft() {
    return withLegendPosition(LEFT);
  }

  @Override
  public IChartConfig withLegendPositionRight() {
    return withLegendPosition(RIGHT);
  }

  @Override
  public IChartConfig withLineTension(BigDecimal tension) {
    return withProperty(LINE_TENSION, tension);
  }

  @Override
  public IChartConfig removeLineTension() {
    return removeProperty(LINE_TENSION);
  }

  @Override
  public BigDecimal getLineTension() {
    return (BigDecimal) getProperty(LINE_TENSION);
  }

  @Override
  public IChartConfig withLineFill(boolean fill) {
    return withProperty(LINE_FILL, fill);
  }

  @Override
  public IChartConfig removeLineFill() {
    return removeProperty(LINE_FILL);
  }

  @Override
  public boolean isLineFill() {
    return BooleanUtility.nvl((Boolean) getProperty(LINE_FILL));
  }

  @Override
  public IChartConfig removeScales() {
    return removeProperty(SCALES);
  }

  @Override
  public IChartConfig withScaleLabelByTypeMap(Map<String, String> scaleLabelByTypeMap) {
    return withProperty(SCALE_LABEL_BY_TYPE_MAP, scaleLabelByTypeMap);
  }

  @Override
  public IChartConfig removeScaleLabelByTypeMap() {
    return removeProperty(SCALE_LABEL_BY_TYPE_MAP);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, String> getScaleLabelByTypeMap() {
    return (Map<String, String>) getProperty(SCALE_LABEL_BY_TYPE_MAP);
  }

  @Override
  public IChartConfig withXLabelMap(Map<String, String> xLabelMap) {
    return withProperty(X_LABEL_MAP, xLabelMap);
  }

  @Override
  public IChartConfig removeXLabelMap() {
    return removeProperty(X_LABEL_MAP);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, String> getXLabelMap() {
    return (Map<String, String>) getProperty(X_LABEL_MAP);
  }

  @Override
  public IChartConfig withXAxisStacked(boolean stacked) {
    return withProperty(combine(X, STACKED), stacked);
  }

  @Override
  public IChartConfig removeXAxisStacked() {
    return removeProperty(combine(X, STACKED));
  }

  @Override
  public boolean isXAxisStacked() {
    return BooleanUtility.nvl((Boolean) getProperty(combine(X, STACKED)));
  }

  @Override
  public IChartConfig withXAxisLabelDisplay(boolean display) {
    return withProperty(combine(X, SCALE_TITLE_DISPLAY), display);
  }

  @Override
  public IChartConfig removeXAxisLabelDisplay() {
    return removeProperty(combine(X, SCALE_TITLE_DISPLAY));
  }

  @Override
  public boolean isXAxisLabelDisplay() {
    return BooleanUtility.nvl((Boolean) getProperty(combine(X, SCALE_TITLE_DISPLAY)));
  }

  @Override
  public IChartConfig withXAxisLabel(String label) {
    return withProperty(combine(X, SCALE_TITLE_TEXT), label);
  }

  @Override
  public IChartConfig removeXAxisLabel() {
    return removeProperty(combine(X, SCALE_TITLE_TEXT));
  }

  @Override
  public String getXAxisLabel() {
    return (String) getProperty(combine(X, SCALE_TITLE_TEXT));
  }

  @Override
  public IChartConfig withYLabelMap(Map<String, String> yLabelMap) {
    return withProperty(Y_LABEL_MAP, yLabelMap);
  }

  @Override
  public IChartConfig removeYLabelMap() {
    return removeProperty(Y_LABEL_MAP);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, String> getYLabelMap() {
    return (Map<String, String>) getProperty(Y_LABEL_MAP);
  }

  @Override
  public IChartConfig withYAxisStacked(boolean stacked) {
    return withProperty(combine(Y, STACKED), stacked);
  }

  @Override
  public IChartConfig removeYAxisStacked() {
    return removeProperty(combine(Y, STACKED));
  }

  @Override
  public boolean isYAxisStacked() {
    return BooleanUtility.nvl((Boolean) getProperty(combine(Y, STACKED)));
  }

  @Override
  public IChartConfig withYAxisLabelDisplay(boolean display) {
    return withProperty(combine(Y, SCALE_TITLE_DISPLAY), display);
  }

  @Override
  public IChartConfig removeYAxisLabelDisplay() {
    return removeProperty(combine(Y, SCALE_TITLE_DISPLAY));
  }

  @Override
  public boolean isYAxisLabelDisplay() {
    return BooleanUtility.nvl((Boolean) getProperty(combine(Y, SCALE_TITLE_DISPLAY)));
  }

  @Override
  public IChartConfig withYAxisLabel(String label) {
    return withProperty(combine(Y, SCALE_TITLE_TEXT), label);
  }

  @Override
  public IChartConfig removeYAxisLabel() {
    return removeProperty(combine(Y, SCALE_TITLE_TEXT));
  }

  @Override
  public String getYAxisLabel() {
    return (String) getProperty(combine(Y, SCALE_TITLE_TEXT));
  }

  @Override
  public IChartConfig withDatalabelsDisplay(boolean display) {
    return withProperty(DATALABELS_DISPLAY, display);
  }

  @Override
  public IChartConfig removeDatalabelsDisplay() {
    return removeProperty(DATALABELS_DISPLAY);
  }

  @Override
  public boolean isDatalabelsDisplay() {
    return BooleanUtility.nvl((Boolean) getProperty(DATALABELS_DISPLAY));
  }
}
