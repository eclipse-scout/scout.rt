/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.LogicalGridLayoutConfig;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;

public interface IRadioButtonGroup<T> extends IValueField<T>, ICompositeField {

  /**
   * int property that stores the number of columns of the group.
   *
   * @see #DEFAULT_GRID_COLUMN_COUNT
   */
  String PROP_GRID_COLUMN_COUNT = "gridColumnCount";

  /**
   * Use columns as required to display all radio buttons in the height of this group.<br>
   * Can be used in {@link #setGridColumnCount(int)}.<br>
   * Instructs the radio button group to calculate the number of columns based on the number of radio buttons and the
   * configured height of the field.
   */
  int DEFAULT_GRID_COLUMN_COUNT = -1;

  String PROP_LAYOUT_CONFIG = "layoutConfig";

  /**
   * @return the buttons controlled by this radio button group
   */
  List<IRadioButton<T>> getButtons();

  /**
   * @return the button representing this value
   */
  IRadioButton<T> getButtonFor(T radioValue);

  /**
   * @return the selected radio button controlled by this radio button group
   */
  IRadioButton<T> getSelectedButton();

  /**
   * select a button controlled by this radio button group
   */
  void selectButton(IRadioButton<T> button);

  /**
   * @return the radio value of the selected button controlled by this radio button group
   */
  T getSelectedKey();

  /**
   * select the buttons controlled by this radio button group with this radio value
   */
  void selectKey(T key);

  /**
   * @return the number of columns this group uses to layout radio buttons. If the value is <=0, it is calculated based
   * on the height of {@link IRadioButtonGroup} and the number of visible radio buttons. In that case the value
   * might change on every layout (e.g. if buttons change visibility).
   */
  int getGridColumnCount();

  /**
   * Sets a new grid column count which is used to layout radio buttons.
   *
   * @param c
   *     the new number of columns. If a value <= 0 is passed (e.g. {@link #DEFAULT_GRID_COLUMN_COUNT}), the number
   *     of columns is calculated on every layout based on the height of this {@link IRadioButtonGroup} and the
   *     number of visible radio buttons.
   * @return {@code true} if the column count has been changed. This also triggers a rebuild of the grid for the current
   * group. {@code false} if it was already set to this value and nothing was updated.
   */
  boolean setGridColumnCount(int c);

  void setLayoutConfig(LogicalGridLayoutConfig config);

  LogicalGridLayoutConfig getLayoutConfig();
}
