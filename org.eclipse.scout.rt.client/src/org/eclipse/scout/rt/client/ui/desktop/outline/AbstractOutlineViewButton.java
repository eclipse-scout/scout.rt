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
import org.eclipse.scout.rt.client.ui.action.view.AbstractViewButton;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public abstract class AbstractOutlineViewButton extends AbstractViewButton {

  private final IDesktop m_desktop;
  private final IOutline m_outline;

  /**
   * call using {@link AbstractDesktop}.this or {@link AbstractDesktopExtension#getDelegatingDesktop()}
   */
  public AbstractOutlineViewButton(IDesktop desktop, Class<? extends IOutline> outlineType) {
    super(false);
    m_desktop = desktop;
    IOutline outline = null;
    for (IOutline o : desktop.getAvailableOutlines()) {
      if (o.getClass() == outlineType) {
        outline = o;
        break;
      }
    }
    m_outline = outline;
    if (m_desktop == null) {
      throw new IllegalArgumentException("Desktop can not be null");
    }
    if (m_outline == null) {
      throw new IllegalArgumentException("the outline type " + outlineType.getName() + " is not registered in the desktop");
    }
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
          @Override
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
          @Override
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
  protected void execSelectionChanged(boolean selection) throws ProcessingException {
    if (selection) {
      if (m_desktop.getOutline() != null && m_desktop.getOutline() == m_outline) {
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

  @Override
  protected boolean getConfiguredToggleAction() {
    return true;
  }

  /**
   * since execInitAction sets the value of this method to the value of the outline. The getConfigured does not have any
   * affect.
   */
  @Override
  protected final boolean getConfiguredEnabled() {
    return super.getConfiguredEnabled();
  }

  /**
   * since execInitAction sets the value of this method to the value of the outline. The getConfigured does not have any
   * affect.
   */
  @Override
  protected final String getConfiguredIconId() {
    return super.getConfiguredIconId();
  }

  /**
   * since execInitAction sets the value of this method to the value of the outline. The getConfigured does not have any
   * affect.
   */
  @Override
  protected final String getConfiguredText() {
    return super.getConfiguredText();
  }

  /**
   * since execInitAction sets the value of this method to the value of the outline. The getConfigured does not have any
   * affect.
   */
  @Override
  protected final boolean getConfiguredVisible() {
    return super.getConfiguredVisible();
  }

}
