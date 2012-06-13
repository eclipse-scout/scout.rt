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
package org.eclipse.scout.rt.ui.rap.mobile.form;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.mobile.navigation.AbstractMobileBackAction;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.ui.rap.mobile.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.ui.rap.mobile.action.ButtonWrappingAction;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileFormHeader extends AbstractRwtScoutFormHeader {
  private List<IMenu> m_leftBarActions;
  private P_FormToolsAction m_formToolsAction;
  private List<IMenu> m_rightBarActions;
  private boolean m_autoAddBackButtonEnabled;

  @Override
  protected void initializeUi(Composite parent) {
    if (getScoutObject().getRootGroupBox().getSystemProcessButtonCount() > 0) {
      m_leftBarActions = createLeftBarActions();
    }
    if (getScoutObject().getRootGroupBox().getCustomProcessButtonCount() > 0 || getScoutObject().getRootGroupBox().getSystemProcessButtonCount() > 0) {
      m_formToolsAction = createFormToolsAction();
    }
    if (getScoutObject().getRootGroupBox().getSystemProcessButtonCount() > 0) {
      m_rightBarActions = createRightBarActions();
    }

    super.initializeUi(parent);
  }

  @Override
  protected void collectMenusForLeftButtonBar(List<IMenu> menuList) {
    if (m_leftBarActions != null && m_leftBarActions.size() > 0) {
      menuList.addAll(m_leftBarActions);
    }

    if (isAutoAddBackButtonEnabled() && !containsCloseAction()) {
      menuList.add(new P_BackAction());
    }
  }

  protected boolean containsCloseAction() {
    if (m_leftBarActions == null) {
      return false;
    }

    for (IMenu action : m_leftBarActions) {
      if (action instanceof ButtonWrappingAction) {
        IButton wrappedButton = ((ButtonWrappingAction) action).getWrappedButton();
        switch (wrappedButton.getSystemType()) {
          case IButton.SYSTEM_TYPE_CANCEL:
          case IButton.SYSTEM_TYPE_CLOSE:
          case IButton.SYSTEM_TYPE_OK:
            if (wrappedButton.isVisible() && wrappedButton.isEnabled()) {
              return true;
            }
        }
      }
    }

    return false;
  }

  @Override
  protected void collectMenusForRightButtonBar(List<IMenu> menuList) {
    if (m_formToolsAction != null) {
      menuList.add(m_formToolsAction);
    }
    if (m_rightBarActions != null) {
      menuList.addAll(m_rightBarActions);
    }
  }

  protected List<IMenu> convertCustomProcessButtons() {
    IButton[] customProcessButtons = getScoutObject().getRootGroupBox().getCustomProcessButtons();
    if (customProcessButtons == null || customProcessButtons.length == 0) {
      return null;
    }

    return ActionButtonBarUtility.convertButtonsToActions(getScoutObject().getRootGroupBox().getCustomProcessButtons());
  }

  protected List<IMenu> convertSystemProcessButtons(List<Integer> relevantSystemTypes) {
    if (relevantSystemTypes == null || relevantSystemTypes.size() == 0) {
      return null;
    }

    IButton[] systemProcessButtons = getScoutObject().getRootGroupBox().getSystemProcessButtons();
    if (systemProcessButtons == null || systemProcessButtons.length == 0) {
      return null;
    }

    List<IMenu> actions = new LinkedList<IMenu>();
    for (IButton button : systemProcessButtons) {
      if (relevantSystemTypes.contains(button.getSystemType())) {
        actions.add(ActionButtonBarUtility.convertButtonToAction(button));
      }
    }

    return actions;
  }

  protected List<IMenu> createLeftBarActions() {
    return convertSystemProcessButtons(getRelevantSystemTypesForLeftBar());
  }

  protected List<IMenu> createRightBarActions() {
    return convertSystemProcessButtons(getRelevantSystemTypesForRightBar());
  }

  protected List<Integer> getRelevantSystemTypesForLeftBar() {
    List<Integer> systemTypesToConsider = new LinkedList<Integer>();
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_CANCEL);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_CLOSE);
    return systemTypesToConsider;
  }

  protected List<Integer> getRelevantSystemTypesForFormToolsAction() {
    List<Integer> systemTypesToConsider = new LinkedList<Integer>();
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_RESET);

    return systemTypesToConsider;
  }

  protected List<Integer> getRelevantSystemTypesForRightBar() {
    List<Integer> systemTypesToConsider = new LinkedList<Integer>();
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_OK);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_SAVE);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE);

    return systemTypesToConsider;
  }

  protected P_FormToolsAction createFormToolsAction() {
    List<IMenu> actions = new LinkedList<IMenu>();

    List<IMenu> customActions = convertCustomProcessButtons();
    if (customActions != null) {
      actions.addAll(customActions);
    }

    List<IMenu> systemActions = convertSystemProcessButtons(getRelevantSystemTypesForFormToolsAction());
    if (systemActions != null) {
      actions.addAll(systemActions);
    }

    if (actions.size() > 0) {
      P_FormToolsAction formToolsAction = new P_FormToolsAction();
      formToolsAction.setChildActions(actions);
      return formToolsAction;
    }

    return null;
  }

  public boolean isAutoAddBackButtonEnabled() {
    return m_autoAddBackButtonEnabled;
  }

  /**
   * If this property is set to true it automatically adds a back button if there is no other button on the left side
   * which is able to close the form.
   */
  public void setAutoAddBackButtonEnabled(boolean autoAddBackButtonEnabled) {
    m_autoAddBackButtonEnabled = autoAddBackButtonEnabled;
  }

  private class P_BackAction extends AbstractMobileBackAction {

  }

  private class P_FormToolsAction extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return null;
    }

    @Override
    protected String getConfiguredTooltipText() {
      return TEXTS.get("FormToolsButtonTooltip");
    }

    @Override
    protected String getConfiguredIconId() {
      return Icons.FormToolsAction;
    }

  }

}
