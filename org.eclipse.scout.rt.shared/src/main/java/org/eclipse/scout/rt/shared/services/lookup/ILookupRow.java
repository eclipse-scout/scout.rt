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

import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

/**
 * @since Scout 4.0.0
 */
public interface ILookupRow<KEY_TYPE> extends Serializable {

  AbstractTableRowData getAdditionalTableRowData();

  void setAdditionalTableRowData(AbstractTableRowData bean);

  /**
   * @return
   */
  KEY_TYPE getKey();

  /**
   * @return text
   */
  String getText();

  /**
   * @param text
   */
  void setText(String text);

  /**
   * @return iconId
   */
  String getIconId();

  /**
   * @param iconId
   */
  void setIconId(String iconId);

  /**
   * @return tooltipText
   */
  String getTooltipText();

  /**
   * @param tooltipText
   */
  void setTooltipText(String tooltipText);

  /**
   * @return foregroundColor
   */
  String getForegroundColor();

  /**
   * @return foregroundColor
   */
  void setForegroundColor(String foregroundColor);

  /**
   * @return backgroundColor
   */
  String getBackgroundColor();

  /**
   * @param backgroundColor
   */
  void setBackgroundColor(String backgroundColor);

  /**
   * @return font
   */
  FontSpec getFont();

  /**
   * @param font
   */
  void setFont(FontSpec font);

  /**
   * @return active
   */
  boolean isActive();

  /**
   * @param active
   */
  void setActive(boolean active);

  /**
   * @return enabled
   */
  boolean isEnabled();

  /**
   * @param enabled
   */
  void setEnabled(boolean enabled);

  /**
   * @return parentKey
   */
  KEY_TYPE getParentKey();

  /**
   * @param parentKey
   */
  void setParentKey(KEY_TYPE parentKey);

}
