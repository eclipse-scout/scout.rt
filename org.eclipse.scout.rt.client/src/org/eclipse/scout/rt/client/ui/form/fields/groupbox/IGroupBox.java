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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox;

import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

public interface IGroupBox extends ICompositeField {

  /**
   * {@link Boolean}
   */
  String PROP_BORDER_VISIBLE = "borderVisible";
  /**
   * {@link Boolean}
   */
  String PROP_EXPANDABLE = "expandable";
  /**
   * {@link Boolean}
   */
  String PROP_EXPANDED = "expanded";
  /**
   * {@link String}
   */
  String PROP_BORDER_DECORATION = "borderDecoration";
  /**
   * {@link String}
   */
  String PROP_BACKGROUND_IMAGE_NAME = "backgroundImageName";
  /**
   * {@link Integer}
   */
  String PROP_BACKGROUND_IMAGE_HORIZONTAL_ALIGNMENT = "backgroundImageHorizontalAlignment";
  /**
   * {@link Integer}
   */
  String PROP_BACKGROUND_IMAGE_VERTICAL_ALIGNMENT = "backgroundImageVerticalAlignment";

  String BORDER_DECORATION_EMPTY = "empty";
  String BORDER_DECORATION_LINE = "line";
  String BORDER_DECORATION_SECTION = "section";
  /**
   * automatic border decoration
   * <ul>
   * <li>expandable=true uses section border</li>
   * <li>MainBox (IGroupBox#isMainBox()) has no border</li>
   * <li>groupbog inside tab box (IGroupBox#isMainBox()) has no border</li>
   * <li>all others use line border</li>
   * </ul>
   */
  String BORDER_DECORATION_AUTO = "auto";

  /*
   * Runtime
   */

  /**
   * The index of the groupbox in the parent box
   */
  int getGroupBoxIndex(IGroupBox groupBox);

  int getGroupBoxCount();

  int getCustomProcessButtonCount();

  int getSystemProcessButtonCount();

  /**
   * fields excluding process buttons
   */
  IFormField[] getControlFields();

  IGroupBox[] getGroupBoxes();

  /**
   * buttons with processButton=true and systemType=none
   */
  IButton[] getCustomProcessButtons();

  /**
   * buttons with processButton=true and systemType<>none
   */
  IButton[] getSystemProcessButtons();

  boolean isMainBox();

  void setMainBox(boolean b);

  boolean isBorderVisible();

  void setBorderVisible(boolean b);

  /**
   * When borderEnabled=true: borderDecoration="line" shows a line decoration around box, borderDecoration="none" or
   * null shows just insets.<br>
   * borderDecoration="section" shows a section header<br>
   * Other custom borderDecorations are possible and must be handled in the appropriate GUI factory.
   * <p>
   * Note that this is just the style of the border. To define a section group with expand/collapse, use
   * {@link #getConfiguredExpandable()}, {@link #isExpandable()} and {@link #isExpanded()}<br>
   */
  String getBorderDecoration();

  void setBorderDecoration(String s);

  String getBackgroundImageName();

  void setBackgroundImageName(String imageName);

  int getBackgroundImageVerticalAlignment();

  void setBackgroundImageVerticalAlignment(int a);

  int getBackgroundImageHorizontalAlignment();

  void setBackgroundImageHorizontalAlignment(int a);

  /**
   * column count for this composite box<br>
   * see {@value #GRID_COLUMN_COUNT_INHERITED} for inherited column count
   */
  int getGridColumnCountHint();

  /**
   * @param c
   *          column count for this composite box<br>
   *          see {@value #GRID_COLUMN_COUNT_INHERITED} for inherited column
   *          count
   */
  void setGridColumnCountHint(int c);

  boolean isScrollable();

  void setScrollable(boolean scrollable);

  /**
   * @return true if the group <i>can</i> be collapsed
   *         see {@link #isExpanded()} to see if border is effectively expanded or collapsed
   */
  boolean isExpandable();

  void setExpandable(boolean b);

  /**
   * @return true if the group is expanded
   */
  boolean isExpanded();

  void setExpanded(boolean b);

  IGroupBoxUIFacade getUIFacade();
}
