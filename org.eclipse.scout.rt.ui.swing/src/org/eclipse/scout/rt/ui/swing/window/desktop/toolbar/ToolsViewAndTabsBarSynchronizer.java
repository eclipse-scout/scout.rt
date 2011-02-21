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

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.rt.ui.swing.window.desktop.ToolsViewPlaceholder;

/**
 * Synchronizes the ToolTabsBar widget with the ToolsView so both widget looks like a single unit.
 * 
 * @author awe
 */
public class ToolsViewAndTabsBarSynchronizer {

  private final ToolsViewPlaceholder m_toolsViewPlaceholder;
  private final JToolTabsBar m_toolTabsBar;
  private final OptimisticLock m_syncLock;

  public ToolsViewAndTabsBarSynchronizer(ToolsViewPlaceholder toolsViewPlaceholder, JToolTabsBar toolTabsBar) {
    m_syncLock = new OptimisticLock();
    m_toolsViewPlaceholder = toolsViewPlaceholder;
    m_toolTabsBar = toolTabsBar;
    toolTabsBar.adjustWidthToToolsView(toolsViewPlaceholder.getWidth());
    installListeners();
  }

  protected void reset() {
    //nop
  }

  private void installListeners() {
    m_toolTabsBar.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        try {
          m_syncLock.acquire();
          //
          if (JToolTabsBar.PROP_COLLAPSED.equals(evt.getPropertyName())) {
            boolean collapsed = (Boolean) evt.getNewValue();
            if (collapsed) {
              m_toolsViewPlaceholder.collapseView();
            }
            else {
              m_toolsViewPlaceholder.expandView();
            }
          }
          else if (JToolTabsBar.PROP_MINIMUM_SIZE.equals(evt.getPropertyName())) {
            Dimension d = (Dimension) evt.getNewValue();
            m_toolsViewPlaceholder.setMinimumWidth(d.width - 1);//minus 1
          }
        }
        finally {
          m_syncLock.release();
        }
      }
    });
    m_toolsViewPlaceholder.addComponentListener(new ComponentAdapter() {
      /**
       * WORKAROUND careful: swing is inconsistent in handling gui events: all events are sync except component events!
       * This event is posted after the causing runnable is executed.
       */
      @Override
      public void componentResized(ComponentEvent e) {
        try {
          if (m_syncLock.acquire()) {
            m_toolTabsBar.adjustWidthToToolsView(m_toolsViewPlaceholder.getWidth() + 1);//plus 1
          }
        }
        finally {
          m_syncLock.release();
        }
      }
    });
  }
}
