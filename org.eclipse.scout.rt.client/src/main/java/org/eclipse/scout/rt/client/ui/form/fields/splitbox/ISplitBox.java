/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.splitbox;

import org.eclipse.scout.rt.client.ui.IPreferenceField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * @since 1.0.9 16.07.2008
 */
public interface ISplitBox extends ICompositeField, IPreferenceField {

  String PROP_SPLITTER_ENABLED = "splitterEnabled";
  String PROP_SPLIT_HORIZONTAL = "splitHorizontal";
  String PROP_SPLITTER_POSITION = "splitterPosition";
  String PROP_MIN_SPLITTER_POSITION = "minSplitterPosition";
  String PROP_SPLITTER_POSITION_TYPE = "splitterPositionType";
  String PROP_COLLAPSIBLE_FIELD = "collapsibleField";
  String PROP_FIELD_COLLAPSED = "fieldCollapsed";
  String PROP_TOGGLE_COLLAPSE_KEY_STROKE = "toggleCollapseKeyStroke";
  String PROP_FIRST_COLLAPSE_KEY_STROKE = "firstCollapseKeyStroke";
  String PROP_SECOND_COLLAPSE_KEY_STROKE = "secondCollapseKeyStroke";
  String PROP_FIELD_MINIMIZED = "fieldMinimized";
  String PROP_MINIMIZE_ENABLED = "minimizeEnabled";

  /**
   * The splitter position is the size of the <b>first</b> inner box relative to full size of the split box, i.e. it is
   * a percentage value in the range 0..1. This is the default splitter position type.
   * <p>
   * Example: 0.3 means the first box uses 30% of the available space, the second box uses 70%.
   */
  String SPLITTER_POSITION_TYPE_RELATIVE_FIRST = "relativeFirst";

  /**
   * The splitter position is the size of the <b>second</b> inner box relative to full size of the split box, i.e. it is
   * a percentage value in the range 0..1. This is the default splitter position type.
   * <p>
   * Example: 0.3 means the second box uses 30% of the available space, the first box uses 70%.
   */
  String SPLITTER_POSITION_TYPE_RELATIVE_SECOND = "relativeSecond";

  /**
   * The splitter position is the absolute size of the <b>first</b> inner box. The second box automatically uses the
   * rest of the available space (<code>1 - splitterPosition</code>).
   */
  String SPLITTER_POSITION_TYPE_ABSOLUTE_FIRST = "absoluteFirst";

  /**
   * The splitter position is the absolute size of the <b>second</b> inner box. The first box automatically uses the
   * rest of the available space (<code>1 - splitterPosition</code>).
   */
  String SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND = "absoluteSecond";

  boolean isSplitHorizontal();

  void setSplitHorizontal(boolean horizontal);

  boolean isSplitterEnabled();

  void setSplitterEnabled(boolean enabled);

  /**
   * Splitter position (absolute pixel value or relative value in the range 0..1, depending on the splitter position
   * type, see {@link #getSplitterPositionType()}).
   */
  double getSplitterPosition();

  /**
   * Splitter position (absolute pixel value or relative value in the range 0..1, depending on the splitter position
   * type, see {@link #getSplitterPositionType()}).
   */
  void setSplitterPosition(double position);

  /**
   * Minimal splitter position (absolute pixel value or relative value in the range 0..1, depending on the splitter
   * position type, see {@link #getSplitterPositionType()}). The minimal splitter position is ignored, if set to
   * {@code null}.
   */
  Double getMinSplitterPosition();

  /**
   * Minimal splitter position (absolute pixel value or relative value in the range 0..1, depending on the splitter
   * position type, see {@link #getSplitterPositionType()}). Set to {@code null} to disable the minimal splitter
   * position.
   */
  void setMinSplitterPosition(Double minPosition);

  /**
   * @return {@code true} if collapsible field (@see {@link #getCollapsibleField()}) is reduced to its minimal splitter
   *         position {@link #getMinSplitterPosition()}. If no minimal splitter position is configured, or
   *         {@link #isMinimizeEnabled()} is set to {@code false}, the minimized field state is ignored.
   */
  boolean isFieldMinimized();

  /**
   * Sets the state of the collapsible field (@see {@link #getCollapsibleField()}). If the collapsible field is set to
   * minimized {@code true}, the splitter position is reduced to {@link #getMinSplitterPosition()}. If no minimal
   * splitter position is configured, or {@link #isMinimizeEnabled()} is set to {@code false}, the minimized field state
   * is ignored.
   */
  void setFieldMinimized(boolean minimized);

  /**
   * @return {@code true} if the collapsible field (@see {@link #getCollapsibleField()}) has a minimized field state.
   */
  boolean isMinimizeEnabled();

  /**
   * Sets if the collapsible field (@see {@link #getCollapsibleField()}) has a minimized field state. If set to
   * {@code false}, only the collapsed state will be available. In this case {@link #setFieldMinimized(boolean)} will
   * have not effect.
   */
  void setMinimizeEnabled(boolean enabled);

  /**
   * Value indicating how to interpret the value returned by {@link #getSplitterPosition()}. Should be one of the
   * following constants: {@link #SPLITTER_POSITION_TYPE_RELATIVE_FIRST},
   * {@link #SPLITTER_POSITION_TYPE_RELATIVE_SECOND}, {@link #SPLITTER_POSITION_TYPE_ABSOLUTE_FIRST},
   * {@link #SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND}.
   */
  String getSplitterPositionType();

  /**
   * Sets the splitter position type (see {@link #getSplitterPositionType()}).
   * <p>
   * Note that the value of the splitter position does not change when the splitter position is changed at runtime.
   * However, if the split box is already rendered, the UI may choose to adjust the splitter position value and write it
   * back to the model.
   */
  void setSplitterPositionType(String splitterPositionType);

  /**
   * @return true if the splitter position is to be cached by the ui
   */
  boolean isCacheSplitterPosition();

  void setCacheSplitterPosition(boolean b);

  /**
   * set the storeSplitterPositionPropertyName to store the splitter position UI side. all splitboxes with the same
   * positionPropertyName have the same position of the splitter.
   *
   * @return null to avoid storing the location a String to store the location under the given name.
   */
  String getCacheSplitterPositionPropertyName();

  void setCacheSplitterPositionPropertyName(String propName);

  /**
   * Marks the first or the second field of the split box as collapsible, which displays the according button. Only one
   * field of the split box can be collapsible.
   *
   * @since 6.0
   */
  void setCollapsibleField(IFormField field);

  /**
   * Returns the first or second form-field of the split box which is marked as collapsible or null when no part of the
   * split box is collapsible.
   *
   * @since 6.0
   */
  IFormField getCollapsibleField();

  /**
   * Sets the collapsed state of the collapsible field. When no field is collapsible, the method does nothing.
   *
   * @since 6.0
   */
  void setFieldCollapsed(boolean collapsed);

  /**
   * Returns the collapsed state of the collapsible field. When no field is collapsible, the method returns false.
   *
   * @since 6.0
   */
  boolean isFieldCollapsed();

  /**
   * Sets the key-stroke used to toggle the field collapsed mode.
   *
   * @since 7.0
   */
  void setToggleCollapseKeyStroke(String keyStroke);

  /**
   * Returns the key-stroke used to toggle the field collapsed mode
   *
   * @since 7.0
   */
  String getToggleCollapseKeyStroke();

  /**
   * Sets the key-stroke used to trigger the first collapse button (e.g. the left button for a vertical splitbox and the
   * top button for a horizontal splitbox).
   *
   * @since 7.0
   */
  void setFirstCollapseKeyStroke(String keyStroke);

  /**
   * Returns the key-stroke used to trigger the first collapse button (e.g. the left button for a vertical splitbox and
   * the top button for a horizontal splitbox).
   *
   * @since 7.0
   */
  String getFirstCollapseKeyStroke();

  /**
   * Sets the key-stroke used to trigger the second collapse button (e.g. the right button for vertical splitbox and the
   * bottom button for a horizontal splitbox).
   *
   * @since 7.0
   */
  void setSecondCollapseKeyStroke(String keyStroke);

  /**
   * Returns the key-stroke used to trigger the second collapse button (e.g. the right button for vertical splitbox and
   * the bottom button for a horizontal splitbox).
   *
   * @since 7.0
   */
  String getSecondCollapseKeyStroke();

  ISplitBoxUIFacade getUIFacade();
}
