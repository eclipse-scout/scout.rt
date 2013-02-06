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
package org.eclipse.scout.rt.ui.swing.form.fields.tabbox;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.swing.ext.JTabbedPaneEx;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;
import org.eclipse.scout.rt.ui.swing.form.fields.tabbox.SwingScoutTabItem.SwingTabIcon;

public class SwingScoutTabBox extends SwingScoutFieldComposite<ITabBox> implements ISwingScoutTabBox {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutTabBox.class);

  private HashMap<IGroupBox, Integer> m_scoutGroupToIndex;
  private HashMap<Component, IGroupBox> m_swingGroupToScoutGroup;
  private PropertyChangeListener m_scoutVisiblePropertyListener;
  private OptimisticLock m_tabAddingLock;
  private int m_oldSelectedIndex = 0;

  public SwingScoutTabBox() {
  }

  @Override
  protected void initializeSwing() {
    m_scoutGroupToIndex = new HashMap<IGroupBox, Integer>();
    m_swingGroupToScoutGroup = new HashMap<Component, IGroupBox>();
    m_tabAddingLock = new OptimisticLock();
    // swing layout
    JTabbedPane swingPane = new JTabbedPaneEx();
    swingPane.setOpaque(false);
    swingPane.setFocusable(true);
    IGroupBox[] scoutGroupBoxes = getScoutObject().getGroupBoxes();
    for (int i = 0; i < scoutGroupBoxes.length; i++) {
      m_scoutGroupToIndex.put(scoutGroupBoxes[i], i);
    }
    //
    setSwingField(swingPane);
    setSwingLabel(null);
    setSwingContainer(swingPane);
    //
    for (int i = 0; i < scoutGroupBoxes.length; i++) {
      checkTabItemFor(scoutGroupBoxes[i]);
    }
    setTabItemSelected(0, true);
    //
    swingPane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        setSelectedTabFromSwing();
      }
    });
  }

  private void checkTabItemFor(IGroupBox g) {
    JTabbedPane pane = getSwingTabbedPane();
    if (g.isVisible()) {
      // check if already added
      int modelIndex = m_scoutGroupToIndex.get(g);
      int viewIndex = 0;
      for (int i = 0, n = pane.getTabCount(); i < n; i++) {
        Component comp = pane.getComponentAt(i);
        IGroupBox test = m_swingGroupToScoutGroup.get(comp);
        if (test == g) {
          return;
        }
        int x = m_scoutGroupToIndex.get(test);
        if (x < modelIndex) {
          viewIndex++;
        }
      }
      // add
      ISwingScoutTabItem tabComposite = getSwingEnvironment().createTabItem(pane, g);
      if (tabComposite != null) {
        Component comp = tabComposite.getSwingContainer();
        m_swingGroupToScoutGroup.put(comp, g);
        try {
          getTabAddingLock().acquire();
          pane.insertTab("", tabComposite.getSwingTabIcon(), comp, null, viewIndex);
        }
        finally {
          getTabAddingLock().release();
        }
      }
    }
    else {
      for (int i = 0, n = pane.getTabCount(); i < n; i++) {
        Component comp = pane.getComponentAt(i);
        IGroupBox test = m_swingGroupToScoutGroup.get(comp);
        if (test == g) {
          m_swingGroupToScoutGroup.remove(comp);
          pane.removeTabAt(i);
          break;
        }
      }
    }
  }

  @Override
  public JTabbedPane getSwingTabbedPane() {
    return (JTabbedPane) getSwingField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    applyTopMarginIfTabBoxIsNotOnTop();
    if (m_scoutVisiblePropertyListener == null) {
      m_scoutVisiblePropertyListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent e) {
          getSwingEnvironment().invokeSwingLater(new Runnable() {
            @Override
            public void run() {
              checkTabItemFor((IGroupBox) e.getSource());
            }
          });
        }
      };
      for (IGroupBox g : m_scoutGroupToIndex.keySet()) {
        g.addPropertyChangeListener(IGroupBox.PROP_VISIBLE, m_scoutVisiblePropertyListener);
      }
    }
    setSelectedTabFromScout();
  }

  /**
   * Whenever the tabbox is not the top element in a container, we need to add a bit of extra margin on top.
   * The name set is interpreted by the Rayo L/F.
   */
  private void applyTopMarginIfTabBoxIsNotOnTop() {
    if (getScoutObject().getGridData().y != 0) {
      getSwingTabbedPane().setName("TabbedPane.topMargin");
    }
  }

  @Override
  protected void detachScout() {
    if (m_scoutVisiblePropertyListener != null) {
      for (IGroupBox g : m_scoutGroupToIndex.keySet()) {
        g.removePropertyChangeListener(IGroupBox.PROP_VISIBLE, m_scoutVisiblePropertyListener);
      }
      m_scoutVisiblePropertyListener = null;
    }
    super.detachScout();
  }

  protected void setSelectedTabFromSwing() {
    updateTabItemSelected();
    //
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    if (getTabAddingLock().isAcquired()) {
      // The m_tabAddingLock is acquired, when the current event comes from adding a new tab (make visible).
      // In that case do not fire a setSelectedTabFromUI because there has actually nothing changed in
      // the tab selection and you might overrule some selection changes from the model (compare bug 368991).
      return;
    }
    //
    Component comp = getSwingTabbedPane().getSelectedComponent();
    final IGroupBox box = m_swingGroupToScoutGroup.get(comp);
    if (box != null) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setSelectedTabFromUI(box);
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 0);
    }
  }

  /**
   * scout settings
   */
  protected void setSelectedTabFromScout() {
    IGroupBox selectedTab = getScoutObject().getSelectedTab();
    JTabbedPane pane = getSwingTabbedPane();
    int index = 0;
    while (index < pane.getTabCount()) {
      Component comp = pane.getComponentAt(index);
      IGroupBox test = m_swingGroupToScoutGroup.get(comp);
      if (test == selectedTab) {
        break;
      }
      // next
      index++;
    }
    if (index >= pane.getTabCount()) {
      index = 0;
    }
    if (index != pane.getSelectedIndex()) {
      if (index >= 0 && index < pane.getTabCount()) {
        getSwingTabbedPane().setSelectedIndex(index);
      }
    }
  }

  /**
   * scout property observer
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ITabBox.PROP_SELECTED_TAB)) {
      setSelectedTabFromScout();
    }
  }

  /**
   * Since the tabs used in scout swing UI are painted with an icon the icon has to be informed about
   * the selection change so it can update its foreground color. This wouldn't be necessary if scout
   * would use a regular tab where the text is really a text (and not part of the icon).
   */
  private void updateTabItemSelected() {
    JTabbedPane pane = getSwingTabbedPane();
    int index = pane.getSelectedIndex();
    if (index != m_oldSelectedIndex) {
      setTabItemSelected(m_oldSelectedIndex, false);
      setTabItemSelected(index, true);
      m_oldSelectedIndex = index;
    }
  }

  private void setTabItemSelected(int index, boolean selected) {
    if (index >= 0 && index < getSwingTabbedPane().getTabCount()) {
      Icon icon = getSwingTabbedPane().getIconAt(index);
      if (icon instanceof SwingTabIcon) {
        ((SwingTabIcon) icon).setSelected(selected);
      }
    }
  }

  protected OptimisticLock getTabAddingLock() {
    return m_tabAddingLock;
  }
}
