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

import java.io.Serializable;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.MemoryOptimizedObject;

public class LookupRow extends MemoryOptimizedObject implements Serializable {
  private static final long serialVersionUID = 0L;

  public static final LookupRow[] EMPTY_ARRAY = new LookupRow[0];

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

  public LookupRow(Object key, String text) {
    this(key, text, null);
  }

  public LookupRow(Object key, String text, String iconId) {
    this(key, text, iconId, null);
  }

  public LookupRow(Object key, String text, String iconId, String tooltip) {
    this(key, text, iconId, tooltip, null);
  }

  public LookupRow(Object key, String text, String iconId, String tooltip, String backgroundColor) {
    this(key, text, iconId, tooltip, backgroundColor, null);
  }

  public LookupRow(Object key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor) {
    this(key, text, iconId, tooltip, backgroundColor, foregroundColor, null);
  }

  public LookupRow(Object key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor, FontSpec font) {
    this(key, text, iconId, tooltip, backgroundColor, foregroundColor, font, true);
  }

  public LookupRow(Object key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor, FontSpec font, boolean enabled) {
    this(key, text, iconId, tooltip, backgroundColor, foregroundColor, font, enabled, null);
  }

  public LookupRow(Object key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor, FontSpec font, boolean enabled, Object parentKey) {
    this(key, text, iconId, tooltip, backgroundColor, foregroundColor, font, enabled, parentKey, true);
  }

  public LookupRow(Object key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor, FontSpec font, boolean enabled, Object parentKey, boolean active) {
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

  /**
   * Cell array containing the following values <br>
   * Object key <br>
   * String text <br>
   * String iconId <br>
   * String tooltip <br>
   * String background color <br>
   * String foreground color <br>
   * String font <br>
   * Boolean enabled <br>
   * Object parentKey used in hierarchical structures to point to the parents
   * primary key <br>
   * Boolean active (0,1) see {@link TriState#parseTriState(Object)}
   */
  public LookupRow(Object[] cells) {
    this(cells, (cells == null ? 0 : cells.length));
  }

  public LookupRow(Object[] cells, int maxColumnIndex) {
    if (cells != null) {
      int keyIndex = 0, textIndex = 1, iconIndex = 2, ttIndex = 3, bgIndex = 4, fgIndex = 5, fontIndex = 6, enabledIndex = 7, parentKeyIndex = 8, activeIndex = 9;
      //
      if (cells.length > keyIndex && keyIndex <= maxColumnIndex && cells[keyIndex] != null) {
        setKey(cells[keyIndex]);
      }
      if (cells.length > textIndex && textIndex <= maxColumnIndex && cells[textIndex] != null) {
        if (cells[textIndex] != null) {
          setText(cells[textIndex].toString());
        }
        else {
          setText(null);
        }
      }
      if (cells.length > iconIndex && iconIndex <= maxColumnIndex && cells[iconIndex] != null) {
        if (cells[iconIndex] != null) {
          setIconId(cells[iconIndex].toString());
        }
        else {
          setIconId(null);
        }
      }
      if (cells.length > ttIndex && ttIndex <= maxColumnIndex && cells[ttIndex] != null) {
        if (cells[ttIndex] != null) {
          setTooltipText(cells[ttIndex].toString());
        }
        else {
          setTooltipText(null);
        }
      }
      if (cells.length > bgIndex && bgIndex <= maxColumnIndex && cells[bgIndex] != null) {
        if (cells.length > ttIndex && ttIndex <= maxColumnIndex && cells[ttIndex] != null) {
          if (cells[bgIndex] != null) {
            setBackgroundColor(cells[bgIndex].toString());
          }
          else {
            setBackgroundColor(null);
          }
        }
      }
      if (cells.length > fgIndex && fgIndex <= maxColumnIndex && cells[fgIndex] != null) {
        if (cells.length > ttIndex && ttIndex <= maxColumnIndex && cells[ttIndex] != null) {
          if (cells[fgIndex] != null) {
            setForegroundColor(cells[fgIndex].toString());
          }
          else {
            setForegroundColor(null);
          }
        }
      }
      if (cells.length > fontIndex && fontIndex <= maxColumnIndex && cells[fontIndex] != null) {
        if (cells[fontIndex] != null) {
          setFont(FontSpec.parse(cells[fontIndex].toString()));
        }
        else {
          setFont(null);
        }
      }
      if (cells.length > enabledIndex && enabledIndex <= maxColumnIndex && cells[enabledIndex] != null) {
        if (cells[enabledIndex] instanceof Boolean) {
          setEnabled((Boolean) cells[enabledIndex]);
        }
        else if (cells[enabledIndex] instanceof Number) {
          setEnabled(Boolean.valueOf(((Number) cells[enabledIndex]).intValue() != 0));
        }
      }
      if (cells.length > parentKeyIndex && parentKeyIndex <= maxColumnIndex && cells[parentKeyIndex] != null) {
        setParentKey(cells[parentKeyIndex]);
      }
      if (cells.length > activeIndex && activeIndex <= maxColumnIndex && cells[activeIndex] != null) {
        if (cells[activeIndex] instanceof Boolean) {
          setActive((Boolean) cells[activeIndex]);
        }
        else if (cells[activeIndex] instanceof Number) {
          setActive(Boolean.valueOf(((Number) cells[activeIndex]).intValue() != 0));
        }
      }
    }
  }

  public Object getKey() {
    return getValueInternal(KEY_BIT);
  }

  public void setKey(Object key) {
    setValueInternal(KEY_BIT, key);
  }

  public Object getParentKey() {
    return getValueInternal(PARENT_KEY_BIT);
  }

  public void setParentKey(Object parentKey) {
    setValueInternal(PARENT_KEY_BIT, parentKey);
  }

  public String getText() {
    char[] c = (char[]) getValueInternal(TEXT_BIT);
    return c != null ? new String(c) : null;
  }

  public void setText(String text) {
    setValueInternal(TEXT_BIT, text != null ? text.toCharArray() : null);
  }

  public String getIconId() {
    return (String) getValueInternal(ICON_ID_BIT);
  }

  public void setIconId(String iconId) {
    setValueInternal(ICON_ID_BIT, StringUtility.intern(iconId));
  }

  public String getTooltipText() {
    char[] c = (char[]) getValueInternal(TOOLTIP_BIT);
    return c != null ? new String(c) : null;
  }

  public void setTooltipText(String tooltip) {
    setValueInternal(TOOLTIP_BIT, tooltip != null ? tooltip.toCharArray() : null);
  }

  public String getForegroundColor() {
    return (String) getValueInternal(FOREGROUD_COLOR_BIT);
  }

  public void setForegroundColor(String foregroundColor) {
    setValueInternal(FOREGROUD_COLOR_BIT, StringUtility.intern(foregroundColor));
  }

  public String getBackgroundColor() {
    return (String) getValueInternal(BACKGROUD_COLOR_BIT);
  }

  public void setBackgroundColor(String backgroundColor) {
    setValueInternal(BACKGROUD_COLOR_BIT, StringUtility.intern(backgroundColor));
  }

  public FontSpec getFont() {
    String s = (String) getValueInternal(FONT_BIT);
    return s != null ? FontSpec.parse(s) : null;
  }

  public void setFont(FontSpec font) {
    setValueInternal(FONT_BIT, font != null ? StringUtility.intern(font.toPattern()) : null);
  }

  public boolean isEnabled() {
    if (getValueInternal(ENABLED_BIT) == null) {
      return true;
    }
    return (Boolean) getValueInternal(ENABLED_BIT);
  }

  public void setEnabled(boolean enabled) {
    setValueInternal(ENABLED_BIT, enabled ? null : Boolean.FALSE);
  }

  public boolean isActive() {
    if (getValueInternal(ACTIVE_BIT) == null) {
      return true;
    }
    return (Boolean) getValueInternal(ACTIVE_BIT);
  }

  public void setActive(boolean active) {
    setValueInternal(ACTIVE_BIT, active ? null : Boolean.FALSE);
  }

  /**
   * Convenience helper for transforming Object[][] data into CodeRow[] <br>
   * The Object[][] must contain rows with the elements in the following order: <br>
   * see {@link #LookupRow(Object[])}
   */
  public static LookupRow[] createLookupRowArray(Object[][] data) {
    if (data == null || data.length == 0) {
      return LookupRow.EMPTY_ARRAY;
    }
    else {
      LookupRow[] a = new LookupRow[data.length];
      for (int i = 0; i < data.length; i++) {
        a[i] = new LookupRow(data[i]);
      }
      return a;
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getKey() + ", " + getText() + "]";
  }
}
