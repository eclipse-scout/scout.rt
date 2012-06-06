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
package org.eclipse.scout.rt.client.mobile.ui.desktop;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.mobile.navigation.AbstractMobileBackAction;
import org.eclipse.scout.rt.client.mobile.navigation.AbstractMobileHomeAction;
import org.eclipse.scout.rt.client.mobile.transformation.IDeviceTransformer;
import org.eclipse.scout.rt.client.mobile.transformation.MobileDeviceTransformer;
import org.eclipse.scout.rt.client.mobile.transformation.TabletDeviceTransformer;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.MobileOutlineTableForm;
import org.eclipse.scout.rt.client.mobile.ui.forms.OutlineChooserForm;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.ui.desktop.ContributionCommand;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

public class MobileDesktopExtension extends AbstractDesktopExtension {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileDesktopExtension.class);

  private boolean m_active;
  private OutlineChooserForm m_outlineChooserForm;
  private IDeviceTransformer m_deviceTransformer;
  private P_SearchFormCloseListener m_searchFormCloseListener;

  public MobileDesktopExtension() {
    setActive(UserAgentUtility.isTouchDevice());
  }

  public boolean isActive() {
    return m_active;
  }

  public void setActive(boolean active) {
    m_active = active;
  }

  public IDeviceTransformer getDeviceTransformer() {
    return m_deviceTransformer;
  }

  protected IDeviceTransformer createDeviceTransformer() {
    if (UserAgentUtility.isTabletDevice()) {
      return new TabletDeviceTransformer();
    }
    else {
      return new MobileDeviceTransformer();
    }
  }

  @Override
  public void contributeActions(Collection<IAction> actions) {
    if (!isActive()) {
      return;
    }

    //remove outline buttons, keystrokes and Menus
    for (Iterator<IAction> iterator = actions.iterator(); iterator.hasNext();) {
      IAction action = iterator.next();
      if (action instanceof IViewButton || action instanceof IKeyStroke || action instanceof IMenu) {
        iterator.remove();
      }
    }
    super.contributeActions(actions);
  }

  @Override
  protected ContributionCommand execInit() throws ProcessingException {
    if (!isActive()) {
      return super.execInit();
    }

    m_deviceTransformer = createDeviceTransformer();
    m_deviceTransformer.transformDesktop(getCoreDesktop());

    m_outlineChooserForm = new OutlineChooserForm();

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execGuiAttached() throws ProcessingException {
    if (!isActive()) {
      return super.execGuiAttached();
    }

    showOutlineChooser();

    MobileOutlineTableForm mobileOutlineTableForm = new MobileOutlineTableForm();
    mobileOutlineTableForm.startView();

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execOutlineChanged(IOutline oldOutline, IOutline newOutline) throws ProcessingException {
    if (!isActive()) {
      return super.execOutlineChanged(oldOutline, newOutline);
    }

    getDeviceTransformer().transformOutline(newOutline);

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execPageDetailTableChanged(ITable oldTable, ITable newTable) throws ProcessingException {
    if (!isActive()) {
      return super.execPageDetailTableChanged(oldTable, newTable);
    }

    getDeviceTransformer().transformPageDetailTable(newTable);

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execCustomFormModification(IHolder<IForm> formHolder) {
    if (!isActive()) {
      return super.execCustomFormModification(formHolder);
    }

    IForm form = formHolder.getValue();
    if (form == null) {
      return ContributionCommand.Stop;
    }

    if (!getDeviceTransformer().acceptForm(form)) {
      formHolder.setValue(null);
      return ContributionCommand.Stop;
    }

    getDeviceTransformer().transformForm(form);

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execPageSearchFormChanged(IForm oldForm, final IForm newForm) throws ProcessingException {
    if (!isActive()) {
      return super.execPageSearchFormChanged(oldForm, newForm);
    }

    if (m_searchFormCloseListener == null) {
      m_searchFormCloseListener = new P_SearchFormCloseListener();
    }

    if (oldForm != null) {
      oldForm.removeFormListener(m_searchFormCloseListener);
    }

    if (newForm != null) {
      newForm.addFormListener(m_searchFormCloseListener);
    }

    return ContributionCommand.Continue;
  }

  private void showOutlineChooser() throws ProcessingException {
    if (getCoreDesktop().isShowing(m_outlineChooserForm)) {
      return;
    }

    if (!m_outlineChooserForm.isFormOpen()) {
      m_outlineChooserForm.startView();
    }
    else {
      getCoreDesktop().addForm(m_outlineChooserForm);
    }
  }

  @Order(10)
  public class BackViewButton extends AbstractMobileBackAction {

    @Override
    protected void execInitAction() throws ProcessingException {
      init(getCoreDesktop());
    }

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

  }

  @Order(15)
  public class SeparatorMenu extends AbstractMenu {
    @Override
    protected boolean getConfiguredSeparator() {
      return true;
    }
  }

  @Order(20)
  public class HomeViewButton extends AbstractMobileHomeAction {

    @Override
    protected void execInitAction() throws ProcessingException {
      init(getCoreDesktop());
    }

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

  }

  @Order(30.0)
  public class LogoutMenu extends AbstractMenu {

    @Override
    protected boolean getConfiguredSingleSelectionAction() {
      return true;
    }

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Logoff");
    }

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

    @Override
    protected void execAction() throws ProcessingException {
      ClientJob.getCurrentSession().stopSession();
    }
  }

  private class P_SearchFormCloseListener implements FormListener {
    @Override
    public void formChanged(FormEvent e) throws ProcessingException {
      if (FormEvent.TYPE_STORE_AFTER == e.getType()) {
        //after an search, the searchform is automatically removed
        //to do that, we unselect all visible toolbuttons (one of them will be the search toolbutton)
        for (IToolButton toolButton : getCoreDesktop().getToolButtons()) {
          if (toolButton.isVisible()) {
            toolButton.setSelected(false);
          }
        }
      }
    }
  }
}
