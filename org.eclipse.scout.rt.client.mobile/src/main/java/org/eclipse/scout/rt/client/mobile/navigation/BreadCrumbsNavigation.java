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

import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.IOutlineChooserForm;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.9.0
 */
public class BreadCrumbsNavigation implements IBreadCrumbsNavigation {
  private static final Logger LOG = LoggerFactory.getLogger(BreadCrumbsNavigation.class);

  private final EventListenerList m_listenerList;
  private Stack<IBreadCrumb> m_breadCrumbs;
  private P_DesktopListener m_desktopListener;
  private List<String> m_navigationFormsDisplayViewIds;
  private P_FormListener m_formListener;
  private IBreadCrumb m_currentBreadCrumb;
  private IDesktop m_desktop;

  public BreadCrumbsNavigation() {
    this(null);
  }

  public BreadCrumbsNavigation(IDesktop desktop) {
    if (desktop == null) {
      desktop = ClientSessionProvider.currentSession().getDesktop();
    }
    m_desktop = desktop;
    if (m_desktop == null) {
      throw new IllegalArgumentException("No desktop found. Cannot create bread crumbs navigation.");
    }

    m_breadCrumbs = new Stack<IBreadCrumb>();
    m_listenerList = new EventListenerList();
    m_desktopListener = new P_DesktopListener();
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
  public void stepBack() {
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
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
    LOG.debug("Current bread crumbs way: " + toString());

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
  public void goHome() {
    if (getBreadCrumbs().size() == 0) {
      return;
    }

    activate(getBreadCrumbs().get(0));
  }

  public void activate(IBreadCrumb breadCrumb) {
    if (!getBreadCrumbs().contains(breadCrumb)) {
      return;
    }

    do {
      m_currentBreadCrumb = getBreadCrumbs().pop();
    }
    while (m_currentBreadCrumb != breadCrumb);

    m_currentBreadCrumb.activate();
    LOG.debug("Activated bread crumb: " + m_currentBreadCrumb);
    LOG.debug("Current bread crumbs way: " + toString());

    fireBreadCrumbsChanged();
  }

  @Override
  public List<IForm> getCurrentNavigationForms() {
    List<IForm> navigationForms = new LinkedList<IForm>();

    navigationForms.addAll(getDesktop().getDialogs());

    for (IForm view : getDesktop().getViews()) {
      if (m_navigationFormsDisplayViewIds != null && m_navigationFormsDisplayViewIds.contains(view.getDisplayViewId())) {
        navigationForms.add(view);
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
  public IDesktop getDesktop() {
    return m_desktop;
  }

  private void destroy() {
    if (m_desktopListener != null) {
      getDesktop().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }

    getBreadCrumbs().clear();
  }

  private void removeExistingBreadCrumb(IForm form) {
    if (m_currentBreadCrumb == null) {
      return;
    }

    if (m_currentBreadCrumb.belongsTo(form)) {
      LOG.debug("Removing existing bread crumb: " + m_currentBreadCrumb);

      if (getBreadCrumbs().size() > 0) {
        m_currentBreadCrumb = getBreadCrumbs().pop();
      }
      else {
        m_currentBreadCrumb = null;
      }

      LOG.debug("Current bread crumbs way: " + toString());
      fireBreadCrumbsChanged();
    }
    else {
      IBreadCrumb[] breadCrumbs = getBreadCrumbs().toArray(new IBreadCrumb[getBreadCrumbs().size()]);
      int pos = 0;
      for (IBreadCrumb breadCrumb : breadCrumbs) {
        if (breadCrumb.belongsTo(form)) {
          LOG.debug("Removing existing bread crumb: " + breadCrumb);

          getBreadCrumbs().remove(breadCrumb);
          mergeDuplicates(pos);

          LOG.debug("Current bread crumbs way: " + toString());
          fireBreadCrumbsChanged();
          return;
        }
        pos++;
      }
    }
  }

  /**
   * Makes sure that the bread crumbs at position pos and pos -1 are not the same.
   * <p>
   * Such successive duplicate bread crumbs make no sense because pressing back on such a duplicate would just open the
   * same form again.
   */
  private void mergeDuplicates(int pos) {
    if (pos <= 0 || pos >= getBreadCrumbs().size()) {
      return;
    }

    IBreadCrumb predecessor = getBreadCrumbs().get(pos - 1);
    IBreadCrumb successor = getBreadCrumbs().get(pos);
    if (predecessor.getForm() == successor.getForm()) {
      getBreadCrumbs().remove(successor);
      LOG.debug("Removing duplicate bread crumb: " + successor);
    }
  }

  private void addNewBreadCrumb(IForm form) {
    if (m_currentBreadCrumb != null) {
      //Ignore attempts to insert the same bread crumb again
      if (m_currentBreadCrumb.belongsTo(form)) {
        return;
      }

      getBreadCrumbs().add(m_currentBreadCrumb);
      LOG.debug("Added new bread crumb: " + m_currentBreadCrumb);
    }

    if (form instanceof IOutlineChooserForm) {
      m_currentBreadCrumb = new OutlineChooserBreadCrumb(this, form);
    }
    else {
      m_currentBreadCrumb = new BreadCrumb(this, form);
    }
    LOG.debug("Current bread crumbs way: " + toString());
    fireBreadCrumbsChanged();
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

  @Override
  public String toString() {
    String breadCrumbsWay = "";
    for (IBreadCrumb breadCrumb : getBreadCrumbs()) {
      breadCrumbsWay += "[" + breadCrumb + "] > ";
    }
    if (m_currentBreadCrumb != null) {
      breadCrumbsWay += "[" + m_currentBreadCrumb + "]";
    }
    else {
      breadCrumbsWay = "empty bread crumbs navigation";
    }

    return breadCrumbsWay;
  }

  private class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {

      switch (e.getType()) {
        case DesktopEvent.TYPE_FORM_SHOW: {
          handleFormAdded(e);
          break;
        }
        case DesktopEvent.TYPE_FORM_HIDE: {
          handleFormRemoved(e);
          break;
        }
        case DesktopEvent.TYPE_DESKTOP_CLOSED: {
          destroy();
          break;
        }
        default:
          break;
      }
    }

    private void handleFormAdded(DesktopEvent e) {
      IForm form = e.getForm();
      if (IForm.DISPLAY_HINT_VIEW == form.getDisplayHint() && m_navigationFormsDisplayViewIds != null && !m_navigationFormsDisplayViewIds.contains(form.getDisplayViewId())) {
        return;
      }

      if (m_currentBreadCrumb == null) {
        addNewBreadCrumb(form);
      }
      else if (m_currentBreadCrumb.getForm() != form) {
        addNewBreadCrumb(form);
      }

      attachFormListener(form);
    }

    private void handleFormRemoved(DesktopEvent e) {
      if (getBreadCrumbs().size() == 0) {
        return;
      }

      IForm form = e.getForm();
      if (MobileDesktopUtility.isToolForm(form)) {
        // Stepping back must never open a tool form -> Remove from bread crumbs
        removeExistingBreadCrumb(form);
      }
    }

    private void attachFormListener(IForm form) {
      if (m_formListener == null) {
        m_formListener = new P_FormListener();
      }
      form.removeFormListener(m_formListener);
      form.addFormListener(m_formListener);
    }

  }

  /**
   * Makes sure closed forms get removed from the bread crumbs.
   */
  private class P_FormListener implements FormListener {

    @Override
    public void formChanged(FormEvent e) {
      if (FormEvent.TYPE_CLOSED == e.getType()) {
        IForm form = e.getForm();
        removeExistingBreadCrumb(form);
        detachFormListener(form);
      }
    }

    private void detachFormListener(IForm form) {
      form.removeFormListener(this);
    }
  }
}
