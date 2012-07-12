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

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;

/**
 * @since 3.9.0
 */
public class ActiveOutlineObserver {
  private IOutline m_activeOutline;
  private IDesktop m_desktop;
  private P_DesktopListener m_desktopListener;
  private Set<TreeListener> m_outlineTreeListeners;
  private Set<PropertyChangeListener> m_outlinePropertyChangeListeners;

  public ActiveOutlineObserver() {
    this(null);
  }

  public ActiveOutlineObserver(IDesktop desktop) {
    if (desktop == null) {
      desktop = ClientSyncJob.getCurrentSession().getDesktop();
    }
    m_desktop = desktop;
    if (m_desktop == null) {
      throw new IllegalArgumentException("No desktop found. Cannot create ActiveOutlineObserver.");
    }

    m_activeOutline = desktop.getOutline();
    m_outlineTreeListeners = new HashSet<TreeListener>();
    m_outlinePropertyChangeListeners = new HashSet<PropertyChangeListener>();
    m_desktopListener = new P_DesktopListener();
    desktop.addDesktopListener(m_desktopListener);
  }

  public IDesktop getDesktop() {
    return m_desktop;
  }

  public IOutline getActiveOutline() {
    return m_activeOutline;
  }

  private void destroy() {
    if (m_desktopListener != null) {
      getDesktop().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }
    m_outlineTreeListeners.clear();
    m_outlinePropertyChangeListeners.clear();
  }

  public void addOutlineTreeListener(TreeListener treeListener) {
    if (treeListener == null) {
      return;
    }

    if (m_activeOutline != null) {
      m_activeOutline.addTreeListener(treeListener);
    }
    m_outlineTreeListeners.add(treeListener);
  }

  public void addOutlinePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    if (propertyChangeListener == null) {
      return;
    }

    if (m_activeOutline != null) {
      m_activeOutline.addPropertyChangeListener(propertyChangeListener);
    }
    m_outlinePropertyChangeListeners.add(propertyChangeListener);
  }

  public void removeOutlineTreeListener(TreeListener treeListener) {
    if (treeListener == null) {
      return;
    }

    if (m_activeOutline != null) {
      m_activeOutline.removeTreeListener(treeListener);
    }
    m_outlineTreeListeners.remove(treeListener);
  }

  public void removeOutlinePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    if (propertyChangeListener == null) {
      return;
    }

    if (m_activeOutline != null) {
      m_activeOutline.removePropertyChangeListener(propertyChangeListener);
    }
    m_outlineTreeListeners.remove(propertyChangeListener);
  }

  private class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {

      switch (e.getType()) {
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
        for (TreeListener treeListener : m_outlineTreeListeners) {
          m_activeOutline.removeTreeListener(treeListener);
        }
        for (PropertyChangeListener propertyChangeListener : m_outlinePropertyChangeListeners) {
          m_activeOutline.removePropertyChangeListener(propertyChangeListener);
        }
      }

      if (outline != null) {
        for (TreeListener treeListener : m_outlineTreeListeners) {
          outline.addTreeListener(treeListener);
        }
        for (PropertyChangeListener propertyChangeListener : m_outlinePropertyChangeListeners) {
          outline.addPropertyChangeListener(propertyChangeListener);
        }
      }

      m_activeOutline = outline;
    }
  }
}
