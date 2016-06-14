/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

/**
 * Row representing a result of a lookup.
 *
 * @param <ID_TYPE>
 *          type of the lookup key
 * @since Scout 4.0.0
 */
public interface ILookupRow<KEY_TYPE> extends Serializable {

  /**
   * @return key
   */
  KEY_TYPE getKey();

  /**
   * @return text
   */
  String getText();

  /**
   * @param text
   */
  ILookupRow<KEY_TYPE> withText(String text);

  /**
   * @return iconId
   */
  String getIconId();

  /**
   * @return {@link ILookupRow} with given icon id
   */
  ILookupRow<KEY_TYPE> withIconId(String iconId);

  /**
   * @return tooltipText
   */
  String getTooltipText();

  /**
   * @return {@link ILookupRow} with given tooltipText
   */
  ILookupRow<KEY_TYPE> withTooltipText(String tooltipText);

  /**
   * @return foregroundColor
   */
  String getForegroundColor();

  /**
   * @return {@link ILookupRow} with given foregroundColor
   */
  ILookupRow<KEY_TYPE> withForegroundColor(String foregroundColor);

  /**
   * @return backgroundColor
   */
  String getBackgroundColor();

  /**
   * @return {@link ILookupRow} with given backgroundColor
   */
  ILookupRow<KEY_TYPE> withBackgroundColor(String backgroundColor);

  /**
   * @return font
   */
  FontSpec getFont();

  /**
   * @return {@link ILookupRow} with given font
   */
  ILookupRow<KEY_TYPE> withFont(FontSpec font);

  /**
   * @return css class
   */
  String getCssClass();

  /**
   * @return {@link ILookupRow} with given css class
   */
  ILookupRow<KEY_TYPE> withCssClass(String cssClass);

  /**
   * @return active
   */
  boolean isActive();

  /**
   * @return {@link ILookupRow} with active set to the given value.
   */
  ILookupRow<KEY_TYPE> withActive(boolean active);

  /**
   * @return enabled
   */
  boolean isEnabled();

  /**
   * @return {@link ILookupRow} with enabled set to the given value.
   */
  ILookupRow<KEY_TYPE> withEnabled(boolean enabled);

  /**
   * @return parentKey
   */
  KEY_TYPE getParentKey();

  /**
   * @return {@link ILookupRow} with the given parent key.
   */
  ILookupRow<KEY_TYPE> withParentKey(KEY_TYPE parentKey);

  /**
   * Some additional data associated with this row.
   */
  AbstractTableRowData getAdditionalTableRowData();

  /**
   * Lookup row with some additional data associated with it.
   */
  ILookupRow<KEY_TYPE> withAdditionalTableRowData(AbstractTableRowData bean);

  /**
   * @param key
   * @return This {@link ILookupRow} with given key set.
   */
  ILookupRow<KEY_TYPE> withKey(KEY_TYPE key);

}
