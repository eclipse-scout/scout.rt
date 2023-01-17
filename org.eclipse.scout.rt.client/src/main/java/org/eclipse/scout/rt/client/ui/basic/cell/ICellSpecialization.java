/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.cell;

import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

/**
 * A cell specialization carries rarely used cell properties as well as style attributes.
 */
public interface ICellSpecialization extends IStyleable {

  /**
   * Creates a copy of this object. Subclasses may copy only values shared with other instances of this interface.
   *
   * @return returns a copy of this object.
   */
  ICellSpecialization copy();

  /**
   * Reconciles the given cell style with this instance and returns the reconciled result. In general, the resulting
   * object is not the same as the one the method is called on.
   *
   * @param cellStyle
   *          the cell style to reconcile.
   * @return the potentially new object with the reconciled cell style.
   */
  ICellSpecialization reconcile(CellStyle cellStyle);

  CellStyle getCellStyle();

  String getTooltipText();

  void setTooltipText(String tooltip);

  boolean isEditable();

  void setEditable(boolean editable);

  String getIconId();

  void setIconId(String iconId);

  String getBackgroundColor();

  void setBackgroundColor(String backgroundColor);

  String getForegroundColor();

  void setForegroundColor(String foregroundColor);

  FontSpec getFont();

  void setFont(FontSpec font);

  int getHorizontalAlignment();

  void setHorizontalAlignment(int horizontalAlignment);

  void setHtmlEnabled(boolean enabled);

  boolean isHtmlEnabled();

  boolean isMandatory();

  void setMandatory(boolean b);
}
