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
package org.eclipse.scout.rt.client.ui.form.fields.splitbox;

import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * <h3>ISplitBox</h3> ...
 *
 * @since 1.0.9 16.07.2008
 */
// TODO [7.0] BSH, AWE: Support caching of splitter position or remove from model
public interface ISplitBox extends ICompositeField {

  String PROP_SPLITTER_ENABLED = "splitterEnabled";
  String PROP_SPLIT_HORIZONTAL = "splitHorizontal";
  String PROP_SPLITTER_POSITION = "splitterPosition";
  String PROP_MIN_SPLITTER_POSITION = "minSplitterPosition";
  String PROP_SPLITTER_POSITION_TYPE = "splitterPositionType";
  String PROP_COLLAPSIBLE_FIELD = "collapsibleField";
  String PROP_FIELD_COLLAPSED = "fieldCollapsed";
  String PROP_COLLAPSE_KEY_STROKE = "collapseKeyStroke";
  String PROP_FIELD_MINIMIZED = "fieldMinimized";

  /**
   * The splitter position is the size of the <b>first</b> inner box relative to full size of the split box, i.e. it is
   * a percentage value in the range 0..1. This is the default splitter position type.
   * <p>
   * Example: 0.3 means the first box uses 30% of the available space, the second box uses 70%.
   */
  String SPLITTER_POSITION_TYPE_RELATIVE_FIRST = "relativeFirst";

  /**
   * The splitter position is the size of the <b>first</b> inner box relative to full size of the split box, i.e. it is
   * a percentage value in the range 0..1. This is the default splitter position type.
   * <p>
   * Example: 0.3 means the first box uses 30% of the available space, the second box uses 70%.
   *
   * @deprecated Use {@value #SPLITTER_POSITION_TYPE_RELATIVE_FIRST} instead.
   *             <p>
   *             TODO [8.0] pbz: Remove deprecated constant
   */
  @Deprecated
  String SPLITTER_POSITION_TYPE_RELATIVE = SPLITTER_POSITION_TYPE_RELATIVE_FIRST;

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
   *         position {@link #getMinSplitterPosition()}. If no minimal splitter position is configured, the minimized
   *         field state is ignored.
   */
  boolean isFieldMinimized();

  /**
   * Sets the state of the collapsible field (@see {@link #getCollapsibleField()}). If the collapsible field is set to
   * minimized {@code true}, the splitter position is reduced to {@link #getMinSplitterPosition()}. If no minimal
   * splitter position is configured, the minimized field state is ignored.
   */
  void setFieldMinimized(boolean minimized);

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
   * Sets the key-stroke used to trigger the collapse button.
   *
   * @since 6.0
   */
  void setCollapseKeyStroke(String keyStroke);

  /**
   * Returns the key-stroke used to trigger the collapse button.
   *
   * @since 6.0
   */
  String getCollapseKeyStroke();

  ISplitBoxUIFacade getUIFacade();
}
