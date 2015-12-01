/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.lookup;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.MemoryOptimizedObject;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Row representing a result of a lookup.
 *
 * @param <ID_TYPE>
 *          type of the lookup key
 */
public class LookupRow<ID_TYPE> extends MemoryOptimizedObject implements ILookupRow<ID_TYPE> {
  private static final long serialVersionUID = 0L;
  private static final Logger LOG = LoggerFactory.getLogger(LookupRow.class);

  public static final LookupRow<?>[] EMPTY_ARRAY = new LookupRow[0];

  public static final int KEY_BIT = 1;
  public static final int TEXT_BIT = 2;
  public static final int ICON_ID_BIT = 3;
  public static final int TOOLTIP_BIT = 4;
  public static final int BACKGROUD_COLOR_BIT = 5;
  public static final int FOREGROUD_COLOR_BIT = 6;
  public static final int FONT_BIT = 7;
  public static final int ENABLED_BIT = 8;
  public static final int PARENT_KEY_BIT = 9;
  public static final int ACTIVE_BIT = 10;
  public static final int ADDITIONAL_TABLE_ROW_DATA = 11;
  public static final int CSS_CLASS_BIT = 12;

  public LookupRow(ID_TYPE key, String text) {
    withKey(key).withText(text);
  }

  /**
   * @deprecated use {@link #withIconId(String)}. will be removed in version 6.1.
   */
  @Deprecated
  public LookupRow(ID_TYPE key, String text, String iconId) {
    this(key, text, iconId, null);
  }

  /**
   * @deprecated use {@link #withIconId(String)},{@link #withTooltip(String)} . will be removed in version 6.1.
   */
  @Deprecated
  public LookupRow(ID_TYPE key, String text, String iconId, String tooltip) {
    this(key, text, iconId, tooltip, null);
  }

  /**
   * @deprecated use {@link #withIconId(String)},{@link #withTooltip(String)}, {@link #withBackgroundColor(String)}.
   *             will be removed in version 6.1.
   */
  @Deprecated
  public LookupRow(ID_TYPE key, String text, String iconId, String tooltip, String backgroundColor) {
    this(key, text, iconId, tooltip, backgroundColor, null);
  }

  /**
   * @deprecated use {@link #withIconId(String)},{@link #withTooltip(String)}, {@link #withBackgroundColor(String)},
   *             {@link #withForegroundColor(String)}. will be removed in version 6.1.
   */
  @Deprecated
  public LookupRow(ID_TYPE key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor) {
    this(key, text, iconId, tooltip, backgroundColor, foregroundColor, null);
  }

  /**
   * @deprecated use {@link #withIconId(String)},{@link #withTooltip(String)}, {@link #withBackgroundColor(String)},
   *             {@link #withForegroundColor(String)}, {@link #withFont(FontSpec)(String)}. will be removed in version
   *             6.1.
   */
  @Deprecated
  public LookupRow(ID_TYPE key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor, FontSpec font) {
    this(key, text, iconId, tooltip, backgroundColor, foregroundColor, font, true);
  }

  /**
   * @deprecated use {@link #withIconId(String)},{@link #withTooltip(String)}, {@link #withBackgroundColor(String)},
   *             {@link #withForegroundColor(String)}, {@link #withFont(FontSpec)}, {@link #withEnabled(boolean)}, will
   *             be removed in version 6.1.
   */
  @Deprecated
  public LookupRow(ID_TYPE key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor, FontSpec font, boolean enabled) {
    this(key, text, iconId, tooltip, backgroundColor, foregroundColor, font, enabled, null);
  }

  /**
   * @deprecated use {@link #withIconId(String)},{@link #withTooltip(String)}, {@link #withBackgroundColor(String)},
   *             {@link #withForegroundColor(String)}, {@link #withFont(FontSpec)}, {@link #withEnabled(boolean)},
   *             {@link #withParentKey(Object)} will be removed in version 6.1.
   */
  @Deprecated
  public LookupRow(ID_TYPE key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor, FontSpec font, boolean enabled, ID_TYPE parentKey) {
    this(key, text, iconId, tooltip, backgroundColor, foregroundColor, font, enabled, parentKey, true);
  }

  /**
   * @deprecated use {@link #withIconId(String)},{@link #withTooltip(String)}, {@link #withBackgroundColor(String)},
   *             {@link #withForegroundColor(String)}, {@link #withFont(FontSpec)}, {@link #withEnabled(boolean)},
   *             {@link #withParentKey(Object)}, {@link #withActive(boolean)} will be removed in version 6.1.
   */
  @Deprecated
  public LookupRow(ID_TYPE key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor, FontSpec font, boolean enabled, ID_TYPE parentKey, boolean active) {
    setKey(key);
    setText(text);
    setIconId(iconId);
    setTooltipText(tooltip);
    setBackgroundColor(backgroundColor);
    setForegroundColor(foregroundColor);
    setFont(font);
    setEnabled(enabled);
    setParentKey(parentKey);
    setActive(active);
  }

  public LookupRow(Object[] cells, Class<?> keyClass) {
    this(cells, (cells == null ? -1 : cells.length - 1), keyClass);
  }

  /**
   * Creates a new lookup row with the given cells as data.
   *
   * @param cells
   *          array containing the following values:<br>
   *          Object key (use keyClass to specify the type of the key) <br>
   *          String text <br>
   *          String iconId <br>
   *          String tooltip <br>
   *          String background color <br>
   *          String foreground color <br>
   *          String font <br>
   *          Boolean enabled <br>
   *          Object parentKey used in hierarchical structures to point to the parents primary key <br>
   *          Boolean active (0,1) see {@link TriState#parse(Object)}
   * @param maxColumnIndex
   *          index describing the last column in cells that should be evaluated
   * @param keyClass
   *          Describes the class of the key column (first column) in the given cells. This must correspond to the
   *          generic type of this lookup row.
   */
  @SuppressWarnings("unchecked")
  public LookupRow(Object[] cells, int maxColumnIndex, Class<?> keyClass) {
    if (cells != null) {

      if (maxColumnIndex > cells.length - 1) {
        LOG.warn(String.format("Max column index (%d) greater than provided data (%d).", maxColumnIndex, cells.length - 1));
        maxColumnIndex = cells.length - 1;
      }

      int keyIndex = 0, textIndex = 1, iconIndex = 2, ttIndex = 3, bgIndex = 4, fgIndex = 5, fontIndex = 6, enabledIndex = 7, parentKeyIndex = 8, activeIndex = 9;
      //
      if (keyIndex <= maxColumnIndex && cells[keyIndex] != null) {
        Object key = cells[keyIndex];
        assertAssignableFrom(key, keyClass);
        withKey((ID_TYPE) key);
      }
      if (textIndex <= maxColumnIndex && cells[textIndex] != null) {
        withText(cells[textIndex].toString());
      }
      if (iconIndex <= maxColumnIndex && cells[iconIndex] != null) {
        withIconId(cells[iconIndex].toString());
      }
      if (ttIndex <= maxColumnIndex && cells[ttIndex] != null) {
        withTooltipText(cells[ttIndex].toString());
      }
      if (bgIndex <= maxColumnIndex && cells[bgIndex] != null) {
        withBackgroundColor(cells[bgIndex].toString());
      }
      if (fgIndex <= maxColumnIndex && cells[fgIndex] != null) {
        withForegroundColor(cells[fgIndex].toString());
      }
      if (fontIndex <= maxColumnIndex && cells[fontIndex] != null) {
        withFont(cells[fontIndex].toString());
      }
      if (enabledIndex <= maxColumnIndex && cells[enabledIndex] != null) {
        if (cells[enabledIndex] instanceof Boolean) {
          withEnabled((Boolean) cells[enabledIndex]);
        }
        else if (cells[enabledIndex] instanceof Number) {
          withEnabled(Boolean.valueOf(((Number) cells[enabledIndex]).intValue() != 0));
        }
        else {
          LOG.error(String.format("Ignoring invalid Boolean value", cells[enabledIndex]));
        }
      }
      if (parentKeyIndex <= maxColumnIndex && cells[parentKeyIndex] != null) {
        Object parentKey = cells[parentKeyIndex];
        assertAssignableFrom(parentKey, keyClass);
        withParentKey((ID_TYPE) parentKey);
      }
      if (activeIndex <= maxColumnIndex && cells[activeIndex] != null) {
        if (cells[activeIndex] instanceof Boolean) {
          withActive((Boolean) cells[activeIndex]);
        }
        else if (cells[activeIndex] instanceof Number) {
          withActive(Boolean.valueOf(((Number) cells[activeIndex]).intValue() != 0));
        }
        else {
          LOG.error(String.format("Ignoring invalid Boolean value", cells[activeIndex]));
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T assertAssignableFrom(Object object, Class<T> clazz) {
    if (clazz != null && !clazz.isAssignableFrom(object.getClass())) {
      throw new IllegalArgumentException("Invalid key type for LookupRow. Expected: '" + clazz.getName() + "', actual: '" + object.getClass().getName() + "'.");
    }
    return (T) object;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ID_TYPE getKey() {
    return (ID_TYPE) getValueInternal(KEY_BIT);
  }

  /**
   * @deprecated use {@link #withKey(Object)}. will be removed in version 6.1.
   */
  @Deprecated
  public void setKey(ID_TYPE key) {
    setValueInternal(KEY_BIT, key);
  }

  public ILookupRow<ID_TYPE> withKey(ID_TYPE key) {
    setValueInternal(KEY_BIT, key);
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ID_TYPE getParentKey() {
    return (ID_TYPE) getValueInternal(PARENT_KEY_BIT);
  }

  @Override
  public ILookupRow<ID_TYPE> withParentKey(ID_TYPE parentKey) {
    setValueInternal(PARENT_KEY_BIT, parentKey);
    return this;
  }

  /**
   * @deprecated use {@link #withParentKey(Object)}. will be removed in version 6.1.
   */
  @Deprecated
  @Override
  @SuppressWarnings("deprecation")
  public void setParentKey(ID_TYPE parentKey) {
    withParentKey(parentKey);
  }

  @Override
  public String getText() {
    char[] c = (char[]) getValueInternal(TEXT_BIT);
    return c != null ? new String(c) : null;
  }

  @Override
  public ILookupRow<ID_TYPE> withText(String text) {
    setValueInternal(TEXT_BIT, text != null ? text.toCharArray() : null);
    return this;
  }

  /**
   * @deprecated use {@link #withText(String)}. will be removed in version 6.1.
   */
  @Deprecated
  @Override
  @SuppressWarnings("deprecation")
  public void setText(String text) {
    withText(text);
  }

  @Override
  public String getIconId() {
    return (String) getValueInternal(ICON_ID_BIT);
  }

  @Override
  public ILookupRow<ID_TYPE> withIconId(String iconId) {
    setValueInternal(ICON_ID_BIT, StringUtility.intern(iconId));
    return this;
  }

  /**
   * @deprecated use {@link #withIconId(String)}. will be removed in version 6.1.
   */
  @Deprecated
  @Override
  @SuppressWarnings("deprecation")
  public void setIconId(String iconId) {
    withIconId(iconId);
  }

  @Override
  public String getTooltipText() {
    char[] c = (char[]) getValueInternal(TOOLTIP_BIT);
    return c != null ? new String(c) : null;
  }

  @Override
  public ILookupRow<ID_TYPE> withTooltipText(String tooltipText) {
    setValueInternal(TOOLTIP_BIT, tooltipText != null ? tooltipText.toCharArray() : null);
    return this;
  }

  /**
   * @deprecated use {@link #withTooltipText(String)}. will be removed in version 6.1.
   */
  @Deprecated
  @Override
  @SuppressWarnings("deprecation")
  public void setTooltipText(String tooltipText) {
    withTooltipText(tooltipText);
  }

  @Override
  public String getForegroundColor() {
    return (String) getValueInternal(FOREGROUD_COLOR_BIT);
  }

  @Override
  public ILookupRow<ID_TYPE> withForegroundColor(String foregroundColor) {
    setValueInternal(FOREGROUD_COLOR_BIT, StringUtility.intern(foregroundColor));
    return this;
  }

  /**
   * @deprecated use {@link #withForegroundColor(String)}; will be removed in version 6.1.
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setForegroundColor(String foregroundColor) {
    withForegroundColor(foregroundColor);
  }

  @Override
  public String getCssClass() {
    return (String) getValueInternal(CSS_CLASS_BIT);
  }

  @Override
  public ILookupRow<ID_TYPE> withCssClass(String cssClass) {
    setValueInternal(CSS_CLASS_BIT, StringUtility.intern(cssClass));
    return this;
  }

  @Override
  public String getBackgroundColor() {
    return (String) getValueInternal(BACKGROUD_COLOR_BIT);
  }

  @Override
  public ILookupRow<ID_TYPE> withBackgroundColor(String backgroundColor) {
    setValueInternal(BACKGROUD_COLOR_BIT, StringUtility.intern(backgroundColor));
    return this;
  }

  /**
   * @deprecated use {@link #withBackgroundColor(String)}; will be removed in version 6.1.
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setBackgroundColor(String backgroundColor) {
    withBackgroundColor(backgroundColor);
  }

  @Override
  public FontSpec getFont() {
    String s = (String) getValueInternal(FONT_BIT);
    return s != null ? FontSpec.parse(s) : null;
  }

  public ILookupRow<ID_TYPE> withFont(String font) {
    return withFont(FontSpec.parse(font));
  }

  @Override
  public ILookupRow<ID_TYPE> withFont(FontSpec font) {
    setValueInternal(FONT_BIT, font != null ? StringUtility.intern(font.toPattern()) : null);
    return this;
  }

  /**
   * @deprecated use {@link #withFont(FontSpec)}. will be removed in version 6.1.
   */
  @Deprecated
  @Override
  @SuppressWarnings("deprecation")
  public void setFont(FontSpec font) {
    withFont(font);
  }

  @Override
  public boolean isEnabled() {
    return getOrElse(ENABLED_BIT, Boolean.TRUE);
  }

  @Override
  public ILookupRow<ID_TYPE> withEnabled(boolean enabled) {
    setIfNotDefault(ENABLED_BIT, enabled, Boolean.TRUE);
    return this;
  }

  /**
   * @deprecated use {@link #withEnabled(boolean)}. will be removed in version 6.1.
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setEnabled(boolean enabled) {
    withEnabled(enabled);
  }

  @Override
  public boolean isActive() {
    return getOrElse(ACTIVE_BIT, Boolean.TRUE);
  }

  @Override
  public ILookupRow<ID_TYPE> withActive(boolean active) {
    setIfNotDefault(ACTIVE_BIT, active, Boolean.TRUE);
    return this;
  }

  /**
   * @deprecated use {@link #setActive(boolean)}. will be removed in version 6.1.
   */
  @Deprecated
  @Override
  @SuppressWarnings("deprecation")
  public void setActive(boolean active) {
    withActive(active);
  }

  @Override
  public AbstractTableRowData getAdditionalTableRowData() {
    return (AbstractTableRowData) getValueInternal(ADDITIONAL_TABLE_ROW_DATA);
  }

  @Override
  public ILookupRow<ID_TYPE> withAdditionalTableRowData(AbstractTableRowData bean) {
    setValueInternal(ADDITIONAL_TABLE_ROW_DATA, bean);
    return this;
  }

  /**
   * @deprecated use {@link #withAdditionalTableRowData(AbstractTableRowData)}. will be removed in version 6.1.
   */
  @Deprecated
  @Override
  @SuppressWarnings("deprecation")
  public void setAdditionalTableRowData(AbstractTableRowData bean) {
    withAdditionalTableRowData(bean);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getKey() + ", " + getText() + "]";
  }
}
