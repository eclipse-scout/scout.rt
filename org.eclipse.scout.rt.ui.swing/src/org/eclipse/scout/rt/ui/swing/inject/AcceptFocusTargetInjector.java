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
package org.eclipse.scout.rt.ui.swing.inject;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;

import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

/**
 *
 */
public class AcceptFocusTargetInjector {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AcceptFocusTargetInjector.class);

  public AcceptFocusTargetInjector() {
  }

  public boolean inject(ISwingEnvironment env, Component comp) {
    if (!(comp.isVisible() && comp.isDisplayable() && comp.isFocusable() && comp.isEnabled())) {
      return false;
    }
    // Verify that the Component is recursively enabled. Disabling a
    // heavyweight Container disables its children, whereas disabling
    // a lightweight Container does not.
    if (!(comp instanceof Window)) {
      for (Container enableTest = comp.getParent(); enableTest != null; enableTest = enableTest.getParent()) {
        if (!(enableTest.isEnabled() || enableTest.isLightweight())) {
          return false;
        }
        if (enableTest instanceof Window) {
          break;
        }
      }
    }
    // Pass 0: non-lightweight components
    if (!comp.isLightweight()) {
      return false;
    }
    // Pass 1: special revocations, non-focusable items
    if (comp instanceof JScrollBar) {
      return false;
    }
    if (comp instanceof JPanel) {
      return false;
    }
    if (comp instanceof BasicSplitPaneDivider) {
      return false;
    }
    if (comp instanceof JInternalFrame) {
      return false;
    }
    // Pass 2: tabbed pane, only accept items of active tab
    JTabbedPane tabbedPane = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, comp);
    if (tabbedPane != null) {
      if (!SwingUtilities.isDescendingFrom(comp, tabbedPane.getSelectedComponent())) {
        return false;
      }
    }
    // Pass 3: special accepts
    if (comp instanceof JTable) {
      // JTable only has ancestor focus bindings, we thus force it
      // to be focusable by returning true here.
      return true;
    }
    if (comp instanceof JComboBox) {
      // JComboBox only has ancestor focus bindings, we thus force it
      // to be focusable by returning true here.
      return true;
    }
    // Pass 4: only accept valid focus targets
    if (comp instanceof JComponent) {
      JComponent jcomp = (JComponent) comp;
      InputMap inputMap = jcomp.getInputMap(JComponent.WHEN_FOCUSED);
      while (inputMap != null && inputMap.size() == 0) {
        inputMap = inputMap.getParent();
      }
      if (inputMap == null) {
        return false;
      }
    }
    return true;
  }

}
