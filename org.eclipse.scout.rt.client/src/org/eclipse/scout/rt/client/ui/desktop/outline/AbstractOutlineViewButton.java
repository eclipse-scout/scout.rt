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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.view.AbstractViewButton;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public abstract class AbstractOutlineViewButton extends AbstractViewButton {

  private final IDesktop m_desktop;
  private IOutline m_outline;

  public AbstractOutlineViewButton(IDesktop desktop, Class<? extends IOutline> outlineType) {
    super(false);
    m_desktop = desktop;
    for (IOutline o : desktop.getAvailableOutlines()) {
      if (o.getClass() == outlineType) {
        m_outline = o;
        break;
      }
    }
    if (m_outline == null) throw new IllegalArgumentException("the outline type " + outlineType.getName() + " is not registered in the desktop");
    callInitializer();
  }

  @Override
  protected void execInitAction() {
    setVisible(m_outline.isVisible());
    setEnabled(m_outline.isEnabled());
    setText(m_outline.getTitle());
    setIconId(m_outline.getIconId());
    setSelected(m_desktop.getOutline() == m_outline);
    // add selection listener
    m_desktop.addDesktopListener(
        new DesktopListener() {
          public void desktopChanged(DesktopEvent e) {
            switch (e.getType()) {
              case DesktopEvent.TYPE_OUTLINE_CHANGED: {
                setSelected(e.getOutline() == m_outline);
                break;
              }
            }
          }
        }
        );
    // add change listener
    m_outline.addPropertyChangeListener(
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            String n = e.getPropertyName();
            Object v = e.getNewValue();
            if (n.equals(IOutline.PROP_VISIBLE)) {
              setVisible((Boolean) v);
            }
            else if (n.equals(IOutline.PROP_ENABLED)) {
              setEnabled((Boolean) v);
            }
            else if (n.equals(IOutline.PROP_TITLE)) {
              setText((String) v);
            }
            else if (n.equals(IOutline.PROP_ICON_ID)) {
              setIconId((String) v);
            }
          }
        }
        );
  }

  @Override
  protected boolean getConfiguredToggleAction() {
    return true;
  }

  @Override
  protected void execAction() throws ProcessingException {
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (isSelected()) {
      if (desktop != null && desktop.getOutline() != null && desktop.getOutline() == m_outline) {
        //determine new selection
        ITreeNode newSelectedNode;
        if (m_outline.isRootNodeVisible()) {
          newSelectedNode = m_outline.getRootPage();
        }
        else {
          newSelectedNode = m_outline.getSelectedNode();
          while (newSelectedNode != null && newSelectedNode.getParentNode() != m_outline.getRootPage()) {
            newSelectedNode = newSelectedNode.getParentNode();
          }
        }
        m_outline.selectNode(newSelectedNode);
        // collapse outline
        if (m_outline.isRootNodeVisible()) {
          m_outline.collapseAll(m_outline.getRootPage());
          if (m_outline.getRootPage() instanceof AbstractPage && ((AbstractPage) m_outline.getRootPage()).isInitialExpanded()) {
            m_outline.setNodeExpanded(m_outline.getRootPage(), true);
          }
        }
        else {
          for (IPage root : m_outline.getRootPage().getChildPages()) {
            m_outline.collapseAll(root);
          }
          for (IPage root : m_outline.getRootPage().getChildPages()) {
            if (root instanceof AbstractPage && ((AbstractPage) root).isInitialExpanded()) {
              m_outline.setNodeExpanded(root, true);
            }
          }
        }
      }
      else {
        // activate outline
        m_desktop.setOutline(m_outline);
      }
    }
  }

}
