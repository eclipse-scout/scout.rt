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
package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.client.mobile.ui.basic.table.form.TableRowForm;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.TabForm;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public class ToolFormHandler {
  private P_DesktopListener m_desktopListener;
  private P_OutlineListener m_outlineListener;
  private IOutline m_activeOutline;
  private IDesktop m_desktop;
  private boolean m_closeToolFormsAfterTablePageLoaded;

  public ToolFormHandler(IDesktop desktop) {
    m_desktop = desktop;
    m_desktopListener = new P_DesktopListener();
    m_desktop.addDesktopListener(m_desktopListener);
    m_closeToolFormsAfterTablePageLoaded = true;
  }

  private void destroy() {
    if (m_desktopListener != null) {
      getDesktop().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }
  }

  public IDesktop getDesktop() {
    return m_desktop;
  }

  protected boolean execCloseToolFormsOnFormOpen(IForm form) {
    if (IForm.DISPLAY_HINT_VIEW == form.getDisplayHint() && !MobileDesktopUtility.isToolForm(form)) {
      return !(form instanceof TableRowForm) && !(form instanceof TabForm);
    }

    return false;
  }

  public boolean isCloseToolFormsAfterTablePageLoaded() {
    return m_closeToolFormsAfterTablePageLoaded;
  }

  /**
   * Set to true to to close tool forms after a table page has been loaded, which happens after a search or after a
   * bookmark activation.
   */
  public void setCloseToolFormsAfterTablePageLoaded(boolean closeToolFormsAfterTablePageLoaded) {
    m_closeToolFormsAfterTablePageLoaded = closeToolFormsAfterTablePageLoaded;
  }

  /**
   * This is a delegate and needs to be explicitly called.<br>
   * It's purpose is to close tool forms after a search or after a bookmark activation.
   */
  public void notifyTablePageLoaded(IPageWithTable<?> tablePage) {
    if (isCloseToolFormsAfterTablePageLoaded()) {
      MobileDesktopUtility.closeAllToolForms();
    }
  }

  private class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {

      switch (e.getType()) {
        case DesktopEvent.TYPE_FORM_SHOW: {
          handleFormAdded(e);
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
      if (execCloseToolFormsOnFormOpen(form)) {
        //Close tool form if another view is opened
        MobileDesktopUtility.closeAllToolForms();
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
      MobileDesktopUtility.closeAllToolForms();
    }

  }
}
