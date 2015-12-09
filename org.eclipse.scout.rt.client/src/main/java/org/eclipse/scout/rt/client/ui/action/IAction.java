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
package org.eclipse.scout.rt.client.ui.action;

import java.security.Permission;

import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;

/**
 * Actions have a trigger scope that is a combination of the "locations" {@link #isSingleSelectionAction()},
 * {@link #isMultiSelectionAction()}, {@link #isNonSelectionAction()}, {@link #isEmptySpaceAction()} and the granting
 * {@link #isInheritAccessibility()}
 * <p>
 * Examples:<br>
 * A typical NEW menu on a table that is only visible on the empty space of the table and only when the table field is
 * enabled would have emptySpaceAction=false;
 */
public interface IAction extends IPropertyObserver, ITypeWithClassId, IOrdered {

  String PROP_CONTAINER = "container";
  String PROP_ICON_ID = "iconId";
  String PROP_TEXT = "text";
  String PROP_TEXT_WITH_MNEMONIC = "&text";
  String PROP_TOOLTIP_TEXT = "tooltipText";
  String PROP_ENABLED = "enabled";
  String PROP_SELECTED = "selected";
  String PROP_VISIBLE = "visible";
  String PROP_MNEMONIC = "mnemonic";
  String PROP_KEY_STROKE = "keyStroke";
  String PROP_VIEW_ORDER = "viewOrder"; // FIXME awe: evtl. nur order renamen (gab früher probleme in CRM) --> MVI fragen
  String PROP_HORIZONTAL_ALIGNMENT = "horizontalAlignment";
  int HORIZONTAL_ALIGNMENT_LEFT = -1;
  int HORIZONTAL_ALIGNMENT_RIGHT = 1;

  void initAction();

  /**
   * called to perform action
   */
  void doAction();

  Object getProperty(String name);

  /**
   * With this method it's possible to set (custom) properties.
   * <p>
   * <b>Important: </b> Although this method is intended to be used for custom properties, it's actually possible to
   * change main properties as well. Keep in mind that directly changing main properties may result in unexpected
   * behavior, so do it only if you really know what you are doing. Rather use the officially provided api instead. <br>
   * Example for an unexpected behavior: setVisible() does not only set the property PROP_VISIBLE but also executes
   * additional code. This code would NOT be executed by directly setting the property PROP_VISIBLE with setProperty().
   */
  void setProperty(String name, Object value);

  boolean hasProperty(String name);

  String getActionId();

  String getIconId();

  void setIconId(String iconId);

  String getText();

  void setText(String text);

  /**
   * @return returns the text with mnemonic, e.g. "&File";
   * @since 3.10.0-M3
   */
  String getTextWithMnemonic();

  /**
   * Key stroke with format lowercase [shift-] [control-] [alternate-] key
   * <p>
   * Examples:
   * <ul>
   * <li>"*"
   * <li>"space"
   * <li>"control-s"
   * <li>"shift-control-t"
   * </ul>
   */
  String getKeyStroke();

  void setKeyStroke(String text);

  String getTooltipText();

  void setTooltipText(String text);

  boolean isSeparator();

  void setSeparator(boolean b);

  boolean isSelected();

  void setSelected(boolean b);

  boolean isEnabled();

  void setEnabled(boolean b);

  boolean isVisible();

  void setVisible(boolean b);

  /**
   * @return true if {@link #prepareAction()} should in addition consider the context of the action to decide for
   *         visibility and enabled.<br>
   *         For example a menu of a table field with {@link #isInheritAccessibility()}==true is invisible when the
   *         table field is disabled or invisible
   */
  boolean isInheritAccessibility();

  /**
   * @see #isInheritAccessibility()
   */
  void setInheritAccessibility(boolean b);

  boolean isEnabledInheritAccessibility();

  void setEnabledInheritAccessibility(boolean enabled);

  /**
   * Access control<br>
   * when false, enabled property cannot be set to true
   */
  void setEnabledPermission(Permission p);

  /**
   * Access control<br>
   * when false, enabled property cannot be set to true
   */
  boolean isEnabledGranted();

  /**
   * Access control<br>
   * when false, enabled property cannot be set to true
   */
  void setEnabledGranted(boolean b);

  /**
   * Actions set the property to false while in work.
   *
   * @return true if action is not in {@link IAction#doAction()}
   */
  boolean isEnabledProcessingAction();

  void setEnabledProcessingAction(boolean b);

  /**
   * Access control<br>
   * when false, visible property cannot be set to true
   */
  void setVisiblePermission(Permission p);

  /**
   * Access control<br>
   * when false, visible property cannot be set to true
   */
  boolean isVisibleGranted();

  /**
   * Access control<br>
   * when false, visible property cannot be set to true
   */
  void setVisibleGranted(boolean b);

  boolean isToggleAction();

  void setToggleAction(boolean b);

  char getMnemonic();

  /**
   * UI processes
   */
  IActionUIFacade getUIFacade();

  int acceptVisitor(IActionVisitor visitor);

  /**
   * Looks this action and its every parent are enabled
   *
   * @since 3.8.1
   */
  boolean isThisAndParentsEnabled();

  /**
   * Looks this action and its every parent are visible
   *
   * @since 3.8.1
   */
  boolean isThisAndParentsVisible();

  /**
   * The container of the action, e.g. {@link org.eclipse.scout.rt.client.ui.basic.table.ITable ITable}
   **/
  ITypeWithClassId getContainer();

  /**
   * The container of the action node, e.g. a {@link org.eclipse.scout.rt.client.ui.basic.table.ITable ITable} or
   * {@link org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField ISmartField}
   **/
  void setContainerInternal(ITypeWithClassId container);

  /**
   * @param horizontalAlignment
   *          negative for left and positive for right alignment
   */
  void setHorizontalAlignment(int horizontalAlignment);

  int getHorizontalAlignment();

  void setView(boolean visible, boolean enabled);

  /**
   * Called by the scout framework when the action is disposed in order to release any bound resources. There is usually
   * no need to call this method by the application's code.
   */
  void dispose();
}
