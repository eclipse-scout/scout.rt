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
package org.eclipse.scout.rt.ui.rap.mobile.window.desktop.toolbar;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.mobile.navigation.AbstractMobileBackAction;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.ui.rap.mobile.action.AbstractRwtScoutActionBar;
import org.eclipse.scout.rt.ui.rap.mobile.action.ActionButtonBar;
import org.eclipse.scout.rt.ui.rap.mobile.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutToolbar;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileFormToolbar extends AbstractRwtScoutActionBar<IForm> implements IRwtScoutToolbar<IForm> {
  public static final String VARIANT_FORM_TOOLBAR_CONTAINER = RwtScoutMobileToolbarContainer.VARIANT_TOOLBAR_CONTAINER;

  private P_BackAction m_backAction;
  private P_CancelAction m_cancelAction;
  private P_FormToolsAction m_formToolsAction;
  private List<IMenu> m_rightBarActions;

  @Override
  protected void initializeUi(Composite parent) {
    m_backAction = new P_BackAction();
    m_cancelAction = new P_CancelAction();

    if (getScoutObject().getRootGroupBox().getCustomProcessButtonCount() > 0) {
      m_formToolsAction = createFormToolsAction();
    }
    if (getScoutObject().getRootGroupBox().getSystemProcessButtonCount() > 0) {
      m_rightBarActions = createRightBarActions();
    }

    super.initializeUi(parent);

    getUiContainer().setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FORM_TOOLBAR_CONTAINER);
  }

  @Override
  protected void attachScout() {
    super.attachScout();

    setAskIfNeedSaveFromScout(getScoutObject().isAskIfNeedSave());
    setTitle(getScoutObject().getTitle());
  }

  private void setAskIfNeedSaveFromScout(boolean askIfNeedSave) {
    handleAskIfNeedSaveInScout(askIfNeedSave);
  }

  private void handleAskIfNeedSaveInScout(final boolean askIfNeedSave) {
    Runnable job = new Runnable() {

      @Override
      public void run() {
        m_backAction.setVisible(!askIfNeedSave);
        m_backAction.setEnabled(!askIfNeedSave);

        m_cancelAction.setVisible(askIfNeedSave);
        m_cancelAction.setEnabled(askIfNeedSave);
      }
    };

    getUiEnvironment().invokeScoutLater(job, 0);
  }

  @Override
  public void handleRightViewPositionChanged(int rightViewX) {
    // nothing to do on mobile because there is no view on the right side.
  }

  @Override
  protected void collectMenusForLeftButtonBar(List<IMenu> menuList) {
    menuList.add(m_backAction);
    menuList.add(m_cancelAction);
  }

  @Override
  protected void adaptLeftButtonBar(ActionButtonBar buttonBar) {
    buttonBar.setPilingEnabled(false);
  }

  @Override
  protected void adaptRightButtonBar(ActionButtonBar buttonBar) {
    buttonBar.setPilingEnabled(false);
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

  protected List<IMenu> createRightBarActions() {
    IButton[] systemProcessButtons = getScoutObject().getRootGroupBox().getSystemProcessButtons();
    if (systemProcessButtons == null || systemProcessButtons.length == 0) {
      return null;
    }

    List<Integer> relevantSystemTypes = getRelevantSystemTypesForRightBar();
    if (relevantSystemTypes == null || relevantSystemTypes.size() == 0) {
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

  protected List<Integer> getRelevantSystemTypesForRightBar() {
    List<Integer> systemTypesToConsider = new LinkedList<Integer>();
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_OK);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_RESET);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_SAVE);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE);

    return systemTypesToConsider;
  }

  protected P_FormToolsAction createFormToolsAction() {
    IButton[] customProcessButtons = getScoutObject().getRootGroupBox().getCustomProcessButtons();
    if (customProcessButtons == null || customProcessButtons.length == 0) {
      return null;
    }

    List<IMenu> actions = ActionButtonBarUtility.convertButtonsToActions(getScoutObject().getRootGroupBox().getCustomProcessButtons());
    P_FormToolsAction formToolsAction = new P_FormToolsAction();
    formToolsAction.setChildActions(actions);

    return formToolsAction;
  }

  private class P_BackAction extends AbstractMobileBackAction {

  }

  private class P_CancelAction extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Cancel");
    }

    @Override
    protected String getConfiguredTooltipText() {
      return TEXTS.get("CloseButtonTooltip");
    }

    @Override
    protected String getConfiguredIconId() {
      return null;
    }

    @Override
    protected void execAction() throws ProcessingException {
      IForm form = getScoutObject();
      form.doClose();
    }
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
