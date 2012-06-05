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
package org.eclipse.scout.rt.client.mobile.navigation;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.ui.forms.OutlineChooserForm;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.8.0
 */
public class MobileDeviceNavigator implements IDeviceNavigator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileDeviceNavigator.class);

  private Stack<INavigationPoint> m_navigationHistory;
  private P_DesktopListener m_desktopListener;
  private List<String> m_navigationFormsDisplayViewIds;
  private P_OutlineListener m_outlineListener;
  private IOutline m_activeOutline;
  private INavigationPoint m_currentNavigationPoint;

  public MobileDeviceNavigator(List<String> navigationFormsDisplayViewIds) {
    m_navigationFormsDisplayViewIds = navigationFormsDisplayViewIds;
    m_navigationHistory = new Stack<INavigationPoint>();

    m_desktopListener = new P_DesktopListener();
    getDesktop().addDesktopListener(m_desktopListener);
  }

  @Override
  public Stack<INavigationPoint> getNavigationHistory() {
    return m_navigationHistory;
  }

  @Override
  public void stepBack() throws ProcessingException {
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop == null) {
      return;
    }

    Stack<INavigationPoint> navigationHistory = getNavigationHistory();
    if (navigationHistory.size() == 0) {
      LOG.info("Stepping back not possible because navigation history is empty.");
      return;
    }

    m_currentNavigationPoint = navigationHistory.pop();
    m_currentNavigationPoint.activate();

    LOG.info("Stepped back to: " + m_currentNavigationPoint);
  }

  @Override
  public boolean isSteppingBackPossible() {
    return getNavigationHistory().size() > 0;
  }

  @Override
  public boolean isGoingHomePossible() {
    return isSteppingBackPossible();
  }

  @Override
  public void goHome() throws ProcessingException {
    if (getNavigationHistory().size() == 0) {
      return;
    }

    activate(getNavigationHistory().get(0));
  }

  public void activate(INavigationPoint navigationPoint) throws ProcessingException {
    if (!getNavigationHistory().contains(navigationPoint)) {
      return;
    }

    do {
      m_currentNavigationPoint = getNavigationHistory().pop();
    }
    while (m_currentNavigationPoint != navigationPoint);

    m_currentNavigationPoint.activate();
    LOG.info("Activated navigation point: " + m_currentNavigationPoint);
  }

  @Override
  public List<IForm> getCurrentNavigationForms() {
    List<IForm> navigationForms = new LinkedList<IForm>();

    IForm[] viewStack = getDesktop().getViewStack();
    for (IForm form : viewStack) {
      if (m_navigationFormsDisplayViewIds.contains(form.getDisplayViewId())) {
        navigationForms.add(form);
      }
    }

    return navigationForms;
  }

  @Override
  public boolean containsFormInHistory(IForm form) {
    if (form == null) {
      return false;
    }

    for (INavigationPoint navigationHistoryPoint : getNavigationHistory()) {
      if (form == navigationHistoryPoint.getForm()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public IForm getCurrentNavigationForm() {
    if (m_currentNavigationPoint != null) {
      return m_currentNavigationPoint.getForm();
    }

    return null;
  }

  @Override
  public boolean isOutlineTreeAvailable() {
    return false;
  }

  protected IDesktop getDesktop() {
    return ClientSyncJob.getCurrentSession().getDesktop();
  }

  private void destroy() {
    if (m_desktopListener != null) {
      getDesktop().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }

    getNavigationHistory().clear();
  }

  private void addNewNavigationPoint(IForm form, IPage page) {
    if (m_currentNavigationPoint != null) {
      getNavigationHistory().add(m_currentNavigationPoint);
      LOG.info("Added new navigation point to history: " + m_currentNavigationPoint);
    }

    if (form instanceof OutlineChooserForm) {
      m_currentNavigationPoint = new HomeNavigationPoint(this, form, page);
    }
    else {
      m_currentNavigationPoint = new NavigationPoint(this, form, page);
    }
  }

  private class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {

      switch (e.getType()) {
        case DesktopEvent.TYPE_FORM_ADDED: {
          handleFormAdded(e);
          break;
        }
        case DesktopEvent.TYPE_FORM_REMOVED: {
          handleFormRemoved(e);
          break;
        }
        case DesktopEvent.TYPE_DESKTOP_CLOSED: {
          destroy();
          break;
        }
        case DesktopEvent.TYPE_OUTLINE_CHANGED: {
          handleOutlineChanged(e);
        }
        default:
          break;
      }
    }

    private void handleOutlineChanged(DesktopEvent e) {
      IOutline outline = e.getOutline();

      if (m_activeOutline != null) {
        m_activeOutline.removeTreeListener(m_outlineListener);
      }

      if (outline != null) {
        if (m_outlineListener == null) {
          m_outlineListener = new P_OutlineListener();
        }

        outline.addTreeListener(m_outlineListener);
      }

      m_activeOutline = outline;
    }

    private void handleFormAdded(DesktopEvent e) {
      IForm form = e.getForm();
      if (IForm.DISPLAY_HINT_VIEW == form.getDisplayHint() && !m_navigationFormsDisplayViewIds.contains(form.getDisplayViewId())) {
        return;
      }

      if (m_currentNavigationPoint == null) {
        addNewNavigationPoint(form, null);
      }
      else if (form != getDesktop().getOutlineTableForm()) {
        addNewNavigationPoint(form, null);
      }
      else if (m_currentNavigationPoint.getForm() != form) {
        IPage page = null;
        if (getDesktop().getOutline() != null) {
          page = getDesktop().getOutline().getActivePage();
        }
        addNewNavigationPoint(form, page);
      }

    }

    private void handleFormRemoved(DesktopEvent e) {
      if (getNavigationHistory().size() == 0) {
        return;
      }

      IForm form = e.getForm();
      if (m_currentNavigationPoint.getForm() == form && m_currentNavigationPoint.getPage() == null) {
        m_currentNavigationPoint = getNavigationHistory().pop();
      }
    }
  }

  private class P_OutlineListener extends TreeAdapter {

    @Override
    public void treeChanged(TreeEvent e) {
      switch (e.getType()) {
        case TreeEvent.TYPE_NODES_SELECTED: {
          handleNodesSelected(e);
          break;
        }
      }
    }

    private void handleNodesSelected(TreeEvent event) {
      IPage page = getDesktop().getOutline().getActivePage();
      if (page == null) {
        return;
      }

      if (m_currentNavigationPoint == null) {
        addNewNavigationPoint(getDesktop().getOutlineTableForm(), page);
      }
      else if (m_currentNavigationPoint.getPage() != null) {
        if (m_currentNavigationPoint.getPage() != page) {
          addNewNavigationPoint(getDesktop().getOutlineTableForm(), page);
        }
      }
      else {
        IOutlineTableForm outlineTableForm = getDesktop().getOutlineTableForm();
        if (m_currentNavigationPoint.getForm() == outlineTableForm) {
          //If the current form already is the outline table form then only update the current navigation point if a page has been activated.
          m_currentNavigationPoint = new NavigationPoint(MobileDeviceNavigator.this, outlineTableForm, page);
        }
        else {
          addNewNavigationPoint(getDesktop().getOutlineTableForm(), page);
        }
      }
    }

  }
}
