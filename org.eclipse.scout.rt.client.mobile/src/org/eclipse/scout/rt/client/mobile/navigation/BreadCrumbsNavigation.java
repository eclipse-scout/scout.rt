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

import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.scout.commons.EventListenerList;
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
public class BreadCrumbsNavigation implements IBreadCrumbsNavigation {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BreadCrumbsNavigation.class);

  private final EventListenerList m_listenerList;
  private Stack<IBreadCrumb> m_breadCrumbs;
  private P_DesktopListener m_desktopListener;
  private List<String> m_navigationFormsDisplayViewIds;
  private P_OutlineListener m_outlineListener;
  private IOutline m_activeOutline;
  private IBreadCrumb m_currentBreadCrumb;

  public BreadCrumbsNavigation() {
    this(null);
  }

  public BreadCrumbsNavigation(IDesktop desktop) {
    if (desktop == null) {
      desktop = getDesktop();
    }

    m_breadCrumbs = new Stack<IBreadCrumb>();
    m_listenerList = new EventListenerList();

    m_desktopListener = new P_DesktopListener();

    if (desktop == null) {
      throw new IllegalArgumentException("No desktop found. Cannot create bread crumbs navigation.");
    }

    desktop.addDesktopListener(m_desktopListener);
  }

  @Override
  public Stack<IBreadCrumb> getBreadCrumbs() {
    return m_breadCrumbs;
  }

  @Override
  public void trackDisplayViewId(String displayViewId) {
    if (m_navigationFormsDisplayViewIds == null) {
      m_navigationFormsDisplayViewIds = new LinkedList<String>();
    }

    if (!m_navigationFormsDisplayViewIds.contains(displayViewId)) {
      m_navigationFormsDisplayViewIds.add(displayViewId);
    }
  }

  @Override
  public void stepBack() throws ProcessingException {
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop == null) {
      return;
    }

    Stack<IBreadCrumb> breadCrumbs = getBreadCrumbs();
    if (breadCrumbs.size() == 0) {
      LOG.debug("Stepping back not possible because no bread crumbs found.");
      return;
    }

    m_currentBreadCrumb = breadCrumbs.pop();
    m_currentBreadCrumb.activate();

    LOG.debug("Stepped back to: " + m_currentBreadCrumb);

    fireBreadCrumbsChanged();
  }

  @Override
  public boolean isSteppingBackPossible() {
    return getBreadCrumbs().size() > 0;
  }

  @Override
  public boolean isGoingHomePossible() {
    return isSteppingBackPossible();
  }

  @Override
  public void goHome() throws ProcessingException {
    if (getBreadCrumbs().size() == 0) {
      return;
    }

    activate(getBreadCrumbs().get(0));
  }

  public void activate(IBreadCrumb breadCrumb) throws ProcessingException {
    if (!getBreadCrumbs().contains(breadCrumb)) {
      return;
    }

    do {
      m_currentBreadCrumb = getBreadCrumbs().pop();
    }
    while (m_currentBreadCrumb != breadCrumb);

    m_currentBreadCrumb.activate();
    LOG.debug("Activated bread crumb: " + m_currentBreadCrumb);

    fireBreadCrumbsChanged();
  }

  @Override
  public List<IForm> getCurrentNavigationForms() {
    List<IForm> navigationForms = new LinkedList<IForm>();

    IForm[] viewStack = getDesktop().getViewStack();
    for (IForm form : viewStack) {
      if (m_navigationFormsDisplayViewIds != null && m_navigationFormsDisplayViewIds.contains(form.getDisplayViewId())) {
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

    for (IBreadCrumb breadCrumb : getBreadCrumbs()) {
      if (form == breadCrumb.getForm()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public IForm getCurrentNavigationForm() {
    if (m_currentBreadCrumb != null) {
      return m_currentBreadCrumb.getForm();
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

    getBreadCrumbs().clear();
  }

  private void addNewBreadCrumb(IForm form, IPage page) {
    if (m_currentBreadCrumb != null) {
      //Ignore attempts to insert the same bread crumb again
      if (m_currentBreadCrumb.getForm() == form && m_currentBreadCrumb.getPage() == page) {
        return;
      }

      getBreadCrumbs().add(m_currentBreadCrumb);
      LOG.debug("Added new bread crumb: " + m_currentBreadCrumb);

      fireBreadCrumbsChanged();
    }

    if (form instanceof OutlineChooserForm) {
      m_currentBreadCrumb = new OutlineChooserBreadCrumb(this, form, page);
    }
    else {
      m_currentBreadCrumb = new BreadCrumb(this, form, page);
    }
  }

  @Override
  public void addBreadCrumbsListener(BreadCrumbsListener listener) {
    m_listenerList.add(BreadCrumbsListener.class, listener);
  }

  @Override
  public void removeBreadCrumbsListener(BreadCrumbsListener listener) {
    m_listenerList.remove(BreadCrumbsListener.class, listener);
  }

  private void fireBreadCrumbsChanged() {
    BreadCrumbsEvent e = new BreadCrumbsEvent(this, BreadCrumbsEvent.TYPE_CHANGED);
    fireBreadCrumbsEvent(e);
  }

  private void fireBreadCrumbsEvent(BreadCrumbsEvent e) {
    EventListener[] a = m_listenerList.getListeners(BreadCrumbsListener.class);
    if (a != null) {
      for (int i = 0; i < a.length; i++) {
        ((BreadCrumbsListener) a[i]).breadCrumbsChanged(e);
      }
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
      if (IForm.DISPLAY_HINT_VIEW == form.getDisplayHint() && m_navigationFormsDisplayViewIds != null && !m_navigationFormsDisplayViewIds.contains(form.getDisplayViewId())) {
        return;
      }

      if (m_currentBreadCrumb == null) {
        addNewBreadCrumb(form, null);
      }
      else if (form != getDesktop().getOutlineTableForm()) {
        addNewBreadCrumb(form, null);
      }
      else if (m_currentBreadCrumb.getForm() != form) {
        IPage page = null;
        if (getDesktop().getOutline() != null) {
          page = getDesktop().getOutline().getActivePage();
        }
        addNewBreadCrumb(form, page);
      }

    }

    private void handleFormRemoved(DesktopEvent e) {
      if (getBreadCrumbs().size() == 0) {
        return;
      }

      IForm form = e.getForm();
      if (m_currentBreadCrumb.getForm() == form && m_currentBreadCrumb.getPage() == null) {
        m_currentBreadCrumb = getBreadCrumbs().pop();
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

      if (m_currentBreadCrumb == null) {
        addNewBreadCrumb(getDesktop().getOutlineTableForm(), page);
      }
      else if (m_currentBreadCrumb.getPage() != null) {
        addNewBreadCrumb(getDesktop().getOutlineTableForm(), page);
      }
      else {
        IOutlineTableForm outlineTableForm = getDesktop().getOutlineTableForm();
        if (m_currentBreadCrumb.getForm() == outlineTableForm) {
          //If the current form already is the outline table form then only update the current bread crumb if a page has been activated.
          m_currentBreadCrumb = new BreadCrumb(BreadCrumbsNavigation.this, outlineTableForm, page);
        }
        else {
          addNewBreadCrumb(getDesktop().getOutlineTableForm(), page);
        }
      }
    }

  }
}
