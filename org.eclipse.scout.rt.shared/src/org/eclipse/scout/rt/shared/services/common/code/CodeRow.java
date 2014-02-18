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
package org.eclipse.scout.rt.shared.services.common.code;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public class CodeRow<ID_TYPE> implements ICodeRow<ID_TYPE> {
  private static final long serialVersionUID = 0L;

  private ID_TYPE m_key;
  private String m_text;
  private String m_iconId;
  private String m_tooltip;
  private String m_backgroundColor;
  private String m_foregroundColor;
  private FontSpec m_font;
  private boolean m_active = true;
  private boolean m_enabled = true;
  private transient ID_TYPE m_parentKey;
  private String m_extKey;
  private Number m_value;
  private long m_partitionId;

  public CodeRow(Object[] cells) {
    this(cells, cells.length - 1);
  }

  /**
   * Cell array containing the following values
   * <p>
   * [0] Long key <br>
   * [1] String text <br>
   * [2] String iconId <br>
   * [3] String tooltipText <br>
   * [4] String backgroundColor <br>
   * [5] String foregroundColor <br>
   * [6] String font <br>
   * [7] Long active (0 or 1) <br>
   * [8] Long parentKey <br>
   * [9] String extKey <br>
   * [10] Number value <br>
   * [11] Long enabled (0 or 1) <br>
   * [12] Long partitionId
   */
  @SuppressWarnings("unchecked")
  public CodeRow(Object[] cells, int maxColumnIndex) {
    if (cells != null) {
      for (int index = 0; index < cells.length && index <= maxColumnIndex; index++) {
        if (cells[index] != null) {
          switch (index) {
            case 0: {
              m_key = (ID_TYPE) TypeCastUtility.castValue(cells[index], TypeCastUtility.getGenericsParameterClass(this.getClass(), CodeRow.class));
              break;
            }
            case 1: {
              m_text = cells[index].toString();
              break;
            }
            case 2: {
              m_iconId = cells[index].toString();
              break;
            }
            case 3: {
              m_tooltip = cells[index].toString();
              break;
            }
            case 4: {
              m_backgroundColor = cells[index].toString();
              break;
            }
            case 5: {
              m_foregroundColor = cells[index].toString();
              break;
            }
            case 6: {
              m_font = FontSpec.parse(cells[index].toString());
              break;
            }
            case 7: {
              Boolean b = TypeCastUtility.castValue(cells[index], Boolean.class);
              m_active = b.booleanValue();
              break;
            }
            case 8: {
              ID_TYPE o = (ID_TYPE) TypeCastUtility.castValue(cells[index], TypeCastUtility.getGenericsParameterClass(this.getClass(), CodeRow.class));
              if ((o instanceof Number) && ((Number) o).longValue() == 0) {
                o = null;
              }
              m_parentKey = o;
              break;
            }
            case 9: {
              m_extKey = cells[index].toString();
              break;
            }
            case 10: {
              Object o = cells[index];
              if (o instanceof Number) {
                m_value = (Number) o;
              }
              break;
            }
            case 11: {
              Boolean b = TypeCastUtility.castValue(cells[index], Boolean.class);
              m_enabled = b.booleanValue();
              break;
            }
            case 12: {
              m_partitionId = ((Number) cells[index]).longValue();
              break;
            }
          }
        }
      }
    }
  }

  public CodeRow(ICodeRow<ID_TYPE> t) {
    this(
        t.getKey(),
        t.getText(),
        t.getIconId(),
        t.getTooltipText(),
        t.getBackgroundColor(),
        t.getForegroundColor(),
        t.getFont(),
        t.isEnabled(),
        t.getParentKey(),
        t.isActive(),
        t.getExtKey(),
        t.getValue(),
        t.getPartitionId());
  }

  public CodeRow(ID_TYPE key, String text) {
    m_key = key;
    m_text = text;
  }

  public CodeRow(ID_TYPE key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor, FontSpec font, boolean enabled, ID_TYPE parentKey, boolean active, String extKey, Number value, long partitionId) {
    m_key = key;
    m_text = text;
    m_iconId = iconId;
    m_tooltip = tooltip;
    m_backgroundColor = backgroundColor;
    m_foregroundColor = foregroundColor;
    m_font = font;
    m_active = active;
    m_enabled = enabled;
    m_parentKey = parentKey;
    m_extKey = extKey;
    m_value = value;
    m_partitionId = partitionId;
  }

  /*
   * typed member access
   */
  @Override
  public ID_TYPE getKey() {
    return m_key;
  }

  public void setKey(ID_TYPE key) {
    m_key = key;
  }

  @Override
  public String getText() {
    return m_text;
  }

  @Override
  public void setText(String text) {
    m_text = text;
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  @Override
  public void setIconId(String iconId) {
    m_iconId = iconId;
  }

  @Override
  public String getTooltipText() {
    return getTooltip();
  }

  /**
   * @return
   * @deprecated use {@link #getTooltipText()}
   */
  @Deprecated
  public String getTooltip() {
    return m_tooltip;
  }

  public void setTooltip(String tooltip) {
    m_tooltip = tooltip;
  }

  @Override
  public String getForegroundColor() {
    return m_foregroundColor;
  }

  public void setForegroundColor(String foregroundColor) {
    m_foregroundColor = foregroundColor;
  }

  @Override
  public String getBackgroundColor() {
    return m_backgroundColor;
  }

  public void setBackgroundColor(String backgroundColor) {
    m_backgroundColor = backgroundColor;
  }

  @Override
  public FontSpec getFont() {
    return m_font;
  }

  public void setFont(FontSpec font) {
    m_font = font;
  }

  @Override
  public boolean isActive() {
    return m_active;
  }

  @Override
  public void setActive(boolean b) {
    m_active = b;
  }

  @Override
  public boolean isEnabled() {
    return m_enabled;
  }

  @Override
  public void setEnabled(boolean b) {
    m_enabled = b;
  }

  @Override
  public ID_TYPE getParentKey() {
    return m_parentKey;
  }

  @Override
  public void setParentKey(ID_TYPE parentKey) {
    m_parentKey = parentKey;
  }

  @Override
  public String getExtKey() {
    return m_extKey;
  }

  @Override
  public void setExtKey(String extKey) {
    m_extKey = extKey;
  }

  @Override
  public Number getValue() {
    return m_value;
  }

  @Override
  public void setValue(Number value) {
    m_value = value;
  }

  @Override
  public long getPartitionId() {
    return m_partitionId;
  }

  public void setPartitionId(long partitionId) {
    m_partitionId = partitionId;
  }
}
