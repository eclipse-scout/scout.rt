/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public class CodeRow<ID_TYPE> extends LookupRow<ID_TYPE> implements ICodeRow<ID_TYPE> {
  private static final long serialVersionUID = 0L;

  private String m_extKey;
  private Number m_value;
  private long m_partitionId;
  private double m_order;

  /**
   * This constructor only works if you are using a subclass of CodeRow.
   *
   * @param cells
   *          array containing the values
   */
  protected CodeRow(Object[] cells) {
    this(cells, cells.length - 1);
  }

  /**
   * This constructor only works if you are using a subclass of CodeRow.
   *
   * @param cells
   *          array containing the values
   * @param maxColumnIndex
   *          upper bound for column to be used
   */
  protected CodeRow(Object[] cells, int maxColumnIndex) {
    this(cells, maxColumnIndex, null);
  }

  /**
   * @param cells
   *          array containing the values
   * @param clazz
   *          ID_TYPE of the Key
   */
  public CodeRow(Object[] cells, Class<? extends ID_TYPE> clazz) {
    this(cells, cells.length - 1, clazz);
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
   * [12] Long partitionId<br>
   * [13] Double order
   * </p>
   *
   * @param cells
   *          array containing the values
   * @param maxColumnIndex
   *          upper bound for column to be used
   * @param clazz
   *          ID_TYPE of the Key
   */
  @SuppressWarnings("unchecked")
  public CodeRow(Object[] cells, int maxColumnIndex, Class<? extends ID_TYPE> clazz) {
    super(null, (String) null);
    Class<? extends ID_TYPE> aClazz = clazz;
    if (aClazz == null) {
      aClazz = TypeCastUtility.getGenericsParameterClass(this.getClass(), CodeRow.class);
    }
    if (cells != null) {
      for (int index = 0; index < cells.length && index <= maxColumnIndex; index++) {
        if (cells[index] != null) {
          switch (index) {
            case 0: {
              ID_TYPE key = TypeCastUtility.castValue(cells[index], aClazz);
              withKey(key);
              break;
            }
            case 1: {
              withText(cells[index].toString());
              break;
            }
            case 2: {
              withIconId(cells[index].toString());
              break;
            }
            case 3: {
              withTooltipText(cells[index].toString());
              break;
            }
            case 4: {
              withBackgroundColor(cells[index].toString());
              break;
            }
            case 5: {
              withForegroundColor(cells[index].toString());
              break;
            }
            case 6: {
              withFont(FontSpec.parse(cells[index].toString()));
              break;
            }
            case 7: {
              Boolean b = TypeCastUtility.castValue(cells[index], Boolean.class);
              withActive(b.booleanValue());
              break;
            }
            case 8: {
              ID_TYPE o = TypeCastUtility.castValue(cells[index], aClazz);
              if ((o instanceof Number) && ((Number) o).longValue() == 0) {
                o = null;
              }
              withParentKey(o);
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
              withEnabled(b.booleanValue());
              break;
            }
            case 12: {
              m_partitionId = ((Number) cells[index]).longValue();
              break;
            }
            case 13:
              Double val = TypeCastUtility.castValue(cells[index], Double.class);
              if (val != null) {
                withOrder(val.doubleValue());
              }
              else {
                withOrder(IOrdered.DEFAULT_ORDER);
              }
              break;
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
        t.getCssClass(),
        t.isEnabled(),
        t.getParentKey(),
        t.isActive(),
        t.getExtKey(),
        t.getValue(),
        t.getPartitionId(),
        t.getOrder());
  }

  public CodeRow(ID_TYPE key, String text) {
    super(key, text);
    m_order = Double.MAX_VALUE;
  }

  @SuppressWarnings("squid:S00107")
  public CodeRow(ID_TYPE key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor, FontSpec font, String cssClass, boolean enabled, ID_TYPE parentKey, boolean active, String extKey, Number value,
      long partitionId) {
    this(key, text, iconId, tooltip, backgroundColor, foregroundColor, font, cssClass, enabled, parentKey, active, extKey, value, partitionId, IOrdered.DEFAULT_ORDER);
  }

  @SuppressWarnings("squid:S00107")
  public CodeRow(ID_TYPE key, String text, String iconId, String tooltip, String backgroundColor, String foregroundColor, FontSpec font, String cssClass, boolean enabled, ID_TYPE parentKey, boolean active, String extKey, Number value,
      long partitionId, double order) {
    super(key, text);
    withIconId(iconId);
    withTooltipText(tooltip);
    withBackgroundColor(backgroundColor);
    withForegroundColor(foregroundColor);
    withFont(font);
    withCssClass(cssClass);
    withActive(active);
    withEnabled(enabled);
    withParentKey(parentKey);
    m_extKey = extKey;
    m_value = value;
    m_partitionId = partitionId;
    m_order = order;
  }

  /*
   * typed member access
   */

  @Override
  public CodeRow<ID_TYPE> withKey(ID_TYPE key) {
    return (CodeRow<ID_TYPE>) super.withKey(key);
  }

  @Override
  public CodeRow<ID_TYPE> withText(String text) {
    return (CodeRow<ID_TYPE>) super.withText(text);
  }

  @Override
  public CodeRow<ID_TYPE> withIconId(String iconId) {
    return (CodeRow<ID_TYPE>) super.withIconId(iconId);
  }

  @Override
  public CodeRow<ID_TYPE> withTooltipText(String tooltipText) {
    return (CodeRow<ID_TYPE>) super.withTooltipText(tooltipText);
  }

  @Override
  public CodeRow<ID_TYPE> withForegroundColor(String foregroundColor) {
    return (CodeRow<ID_TYPE>) super.withForegroundColor(foregroundColor);
  }

  @Override
  public CodeRow<ID_TYPE> withBackgroundColor(String backgroundColor) {
    return (CodeRow<ID_TYPE>) super.withBackgroundColor(backgroundColor);
  }

  @Override
  public CodeRow<ID_TYPE> withFont(FontSpec font) {
    return (CodeRow<ID_TYPE>) super.withFont(font);
  }

  @Override
  public CodeRow<ID_TYPE> withActive(boolean active) {
    return (CodeRow<ID_TYPE>) super.withActive(active);
  }

  @Override
  public CodeRow<ID_TYPE> withEnabled(boolean enabled) {
    return (CodeRow<ID_TYPE>) super.withEnabled(enabled);
  }

  @Override
  public CodeRow<ID_TYPE> withParentKey(ID_TYPE parentKey) {
    return (CodeRow<ID_TYPE>) super.withParentKey(parentKey);
  }

  @Override
  public CodeRow<ID_TYPE> withAdditionalTableRowData(AbstractTableRowData bean) {
    return (CodeRow<ID_TYPE>) super.withAdditionalTableRowData(bean);
  }

  @Override
  public CodeRow<ID_TYPE> withCssClass(String cssClass) {
    return (CodeRow<ID_TYPE>) super.withCssClass(cssClass);
  }

  @Override
  public String getExtKey() {
    return m_extKey;
  }

  @Override
  public CodeRow<ID_TYPE> withExtKey(String extKey) {
    m_extKey = extKey;
    return this;
  }

  @Override
  public Number getValue() {
    return m_value;
  }

  @Override
  public CodeRow<ID_TYPE> withValue(Number value) {
    m_value = value;
    return this;
  }

  @Override
  public long getPartitionId() {
    return m_partitionId;
  }

  @Override
  public CodeRow<ID_TYPE> withPartitionId(long partitionId) {
    m_partitionId = partitionId;
    return this;
  }

  @Override
  public double getOrder() {
    return m_order;
  }

  @Override
  public CodeRow<ID_TYPE> withOrder(double order) {
    m_order = order;
    return this;
  }
}
