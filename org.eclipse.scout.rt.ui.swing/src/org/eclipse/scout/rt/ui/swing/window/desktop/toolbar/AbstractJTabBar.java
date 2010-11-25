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
package org.eclipse.scout.rt.ui.swing.window.desktop.toolbar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.JPanel;

/**
 * Base class for tab panels.
 * 
 * @author awe
 */
public abstract class AbstractJTabBar extends JPanel {

  private static final long serialVersionUID = 1L;

  private AbstractButton m_activeTab;

  private PropertyChangeListener m_pcl = new PropertyChangeListener() {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (AbstractJTab.PROP_ACTIVE.equals(evt.getPropertyName())) {
        AbstractButton tab = (AbstractButton) evt.getSource();
        boolean active = tab.isSelected();
        if (active) {
          m_activeTab = tab;
          tabActivated(tab);
        }
        else if (m_activeTab == tab) {
          m_activeTab = null;
        }
        repaint();
      }
    }
  };

  protected void tabActivated(AbstractButton tab) {
  }

  public AbstractButton getActiveTab() {
    return m_activeTab;
  }

  protected void addActiveTabListener(AbstractButton tab) {
    tab.removePropertyChangeListener(m_pcl);
    tab.addPropertyChangeListener(m_pcl);
  }
}
