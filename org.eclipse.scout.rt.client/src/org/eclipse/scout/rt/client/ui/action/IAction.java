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
package org.eclipse.scout.rt.client.ui.action;

import java.security.Permission;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Actions have a trigger scope that is a combination of the "locations" {@link #isSingleSelectionAction()},
 * {@link #isMultiSelectionAction()}, {@link #isNonSelectionAction()}, {@link #isEmptySpaceAction()} and the
 * granting {@link #isInheritAccessibility()}
 * <p>
 * Examples:<br>
 * A typical NEW menu on a table that is only visible on the empty space of the table and only when the table field is
 * enabled would have emptySpaceAction=false;
 */
public interface IAction extends IPropertyObserver {

  String PROP_ICON_ID = "iconId";
  String PROP_TEXT = "text";
  String PROP_TOOLTIP_TEXT = "tooltipText";
  String PROP_ENABLED = "enabled";
  String PROP_SELECTED = "selected";
  String PROP_VISIBLE = "visible";
  String PROP_MNEMONIC = "mnemonic";
  String PROP_KEYSTROKE = "keystroke";
  /**
   * property-type: String
   */
  String PROP_SEPARATOR = "separator";

  /**
   * called to perform action
   */
  void doAction() throws ProcessingException;

  boolean hasProperty(String name);

  String getActionId();

  String getIconId();

  void setIconId(String iconId);

  boolean isSeparator();

  void setSeparator(boolean b);

  String getText();

  void setText(String text);

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

  boolean isSelected();

  void setSelected(boolean b);

  boolean isEnabled();

  void setEnabled(boolean b);

  boolean isVisible();

  void setVisible(boolean b);

  /**
   * @return true if {@link #prepareAction()} should in addition consider the
   *         context of the action to decide for visibility and enabled.<br>
   *         For example a menu of a table field with {@link #isInheritAccessibility()}==true is invisible when the
   *         table
   *         field is disabled or invisible
   */
  boolean isInheritAccessibility();

  /**
   * @see #isInheritAccessibility()
   */
  void setInheritAccessibility(boolean b);

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

  /**
   * action is chosen on a single selected item
   */
  boolean isSingleSelectionAction();

  void setSingleSelectionAction(boolean b);

  /**
   * action is chosen on any of multiple (>=2) selected items
   */
  boolean isMultiSelectionAction();

  void setMultiSelectionAction(boolean b);

  /**
   * action is chosen on empty space (not on items)
   */
  boolean isEmptySpaceAction();

  void setEmptySpaceAction(boolean b);

  boolean isToggleAction();

  void setToggleAction(boolean b);

  char getMnemonic();

  /**
   * called before this action is used
   */
  void prepareAction();

  /**
   * UI processes
   */
  IActionUIFacade getUIFacade();

  int acceptVisitor(IActionVisitor visitor);
}
