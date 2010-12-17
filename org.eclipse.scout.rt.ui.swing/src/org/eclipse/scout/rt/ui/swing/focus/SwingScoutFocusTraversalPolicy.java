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
package org.eclipse.scout.rt.ui.swing.focus;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.InputMap;
import javax.swing.InternalFrameFocusTraversalPolicy;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.SwingUtility;

public class SwingScoutFocusTraversalPolicy extends InternalFrameFocusTraversalPolicy {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutFocusTraversalPolicy.class);

  public SwingScoutFocusTraversalPolicy() {
    super();
  }

  @Override
  public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
    if (aComponent != null) {
      boolean accept = SwingUtility.runInputVerifier(aComponent);
      if (!accept) {
        return null;
      }
    }
    return getComponentAfterV(focusCycleRoot, aComponent);
  }

  private Component getComponentAfterV(Container focusCycleRoot, Component aComponent) {
    if (focusCycleRoot == null || aComponent == null) {
      throw new IllegalArgumentException("focusCycleRoot and aComponent cannot be null");
    }
    if (!aComponent.isFocusCycleRoot(focusCycleRoot)) {
      throw new IllegalArgumentException("focusCycleRoot is not a focus cyle root of aComponent");
    }
    ArrayList<Component> cycle = new ArrayList<Component>();
    enumerateCycle(focusCycleRoot, aComponent, cycle);
    int index = cycle.indexOf(aComponent);
    Component nextComp = null;
    if (index >= 0) {
      nextComp = cycle.get((index + 1) % cycle.size());
    }
    else if (cycle.size() > 0) {
      nextComp = cycle.get(0);
    }
    if (LOG.isDebugEnabled()) LOG.debug("curr: " + aComponent);
    if (LOG.isDebugEnabled()) LOG.debug("next: " + nextComp);
    return nextComp;
  }

  @Override
  public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
    if (aComponent != null) {
      boolean accept = SwingUtility.runInputVerifier(aComponent);
      if (!accept) {
        return null;
      }
    }
    return getComponentBeforeV(focusCycleRoot, aComponent);
  }

  private Component getComponentBeforeV(Container focusCycleRoot, Component aComponent) {
    if (focusCycleRoot == null || aComponent == null) {
      throw new IllegalArgumentException("focusCycleRoot and aComponent cannot be null");
    }
    if (!aComponent.isFocusCycleRoot(focusCycleRoot)) {
      throw new IllegalArgumentException("focusCycleRoot is not a focus cyle root of aComponent");
    }
    ArrayList<Component> cycle = new ArrayList<Component>();
    enumerateCycle(focusCycleRoot, aComponent, cycle);
    int index = cycle.indexOf(aComponent);
    Component prevComp = null;
    if (index >= 0) {
      prevComp = cycle.get((index - 1 + cycle.size()) % cycle.size());
    }
    else if (cycle.size() > 0) {
      prevComp = cycle.get(cycle.size() - 1);
    }
    if (LOG.isDebugEnabled()) LOG.debug("curr: " + aComponent);
    if (LOG.isDebugEnabled()) LOG.debug("prev: " + prevComp);
    return prevComp;
  }

  @Override
  public Component getDefaultComponent(Container focusCycleRoot) {
    return getDefaultComponentV(focusCycleRoot);
  }

  private Component getDefaultComponentV(Container focusCycleRoot) {
    return getFirstComponentV(focusCycleRoot);
  }

  @Override
  public Component getFirstComponent(Container focusCycleRoot) {
    return getFirstComponentV(focusCycleRoot);
  }

  private Component getFirstComponentV(Container focusCycleRoot) {
    if (focusCycleRoot == null) {
      throw new IllegalArgumentException("focusCycleRoot cannot be null");
    }
    ArrayList<Component> cycle = new ArrayList<Component>();
    enumerateCycle(focusCycleRoot, null, cycle);
    if (cycle.size() > 0) {
      return cycle.get(0);
    }
    else {
      return null;
    }
  }

  @Override
  public Component getLastComponent(Container focusCycleRoot) {
    return getLastComponentV(focusCycleRoot);
  }

  private Component getLastComponentV(Container focusCycleRoot) {
    if (focusCycleRoot == null) {
      throw new IllegalArgumentException("focusCycleRoot cannot be null");
    }
    ArrayList<Component> cycle = new ArrayList<Component>();
    enumerateCycle(focusCycleRoot, null, cycle);
    if (cycle.size() > 0) {
      return cycle.get(cycle.size() - 1);
    }
    else {
      return null;
    }
  }

  /**
   * currentOwner must be part of the enumeration, regardless of its state
   */
  private void enumerateCycle(Container focusCycleRoot, Component currentOwner, List<Component> cycle) {
    enumerateCycleRec(focusCycleRoot, currentOwner, cycle);
  }

  private void enumerateCycleRec(Container container, Component currentOwner, List<Component> cycle) {
    if (container == currentOwner || accept(container)) {
      cycle.add(container);
    }
    Component[] components = container.getComponents();
    for (int i = 0; i < components.length; i++) {
      Component comp = components[i];
      if (comp instanceof Container) {
        enumerateCycleRec((Container) comp, currentOwner, cycle);
      }
      else {
        //component
        if (comp == currentOwner || accept(comp)) {
          cycle.add(comp);
        }
      }
    }
  }

  public boolean accept(Component comp) {
    if (!(comp.isVisible() && comp.isFocusable() && comp.isEnabled())) {
      return false;
    }
    // Verify that the Component is recursively enabled and visible. Disabling a
    // heavyweight Container disables its children, whereas disabling
    // a lightweight Container does not.
    if (!(comp instanceof Window)) {
      for (Container test = comp.getParent(); test != null; test = test.getParent()) {
        if (!test.isVisible()) {
          return false;
        }
        if (!test.isEnabled() && !test.isLightweight()) {
          return false;
        }
        if (test instanceof Window) {
          break;
        }
      }
    }
    // Pass 0: non-lightweight components (must check displayable to check peer!=null)
    if (comp.isDisplayable() && !comp.isLightweight()) {
      if (comp instanceof Canvas) {
        return true;
      }
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
    if (comp instanceof JTextComponent) {
      if (!((JTextComponent) comp).isEditable()) {
        return false;
      }
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
