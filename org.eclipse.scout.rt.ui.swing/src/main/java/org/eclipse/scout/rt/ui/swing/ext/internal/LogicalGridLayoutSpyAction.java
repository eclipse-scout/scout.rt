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
package org.eclipse.scout.rt.ui.swing.ext.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayoutInfo;

/**
 * @author Andreas Hoegger
 * @since 4.0.0
 */
public class LogicalGridLayoutSpyAction extends AbstractAction {
  public static final String GROUP_BOX_MARKER = "groupBoxMarker";
  private static final long serialVersionUID = 1L;
  private GridLayoutCanvas m_canvas;
  private KeyEventDispatcher m_keyEventDispatcher;
  private JWindow m_frame;

  private static final List<Color> colors = CollectionUtility.arrayList(
      /*dark blue*/new Color(0, 0, 100),
      /*dark green*/new Color(0, 100, 0),
      /*dark red*/new Color(204, 0, 0),
      /*dark magenta*/Color.magenta);

  @Override
  public void actionPerformed(ActionEvent e) {
    Component rootComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    while (rootComponent.getParent() != null && !(rootComponent instanceof Window)) {
      rootComponent = rootComponent.getParent();
    }
    Window parentWindow = null;
    if (rootComponent instanceof Window) {
      parentWindow = (Window) rootComponent;
    }

    m_frame = new JWindow(parentWindow);

    m_frame.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e1) {
        dispose();
      }
    });

    m_keyEventDispatcher = new KeyEventDispatcher() {

      @Override
      public boolean dispatchKeyEvent(KeyEvent e1) {
        switch (e1.getKeyCode()) {
          case KeyEvent.VK_DELETE:
          case KeyEvent.VK_ESCAPE:
            dispose();
            break;
        }
        return false;
      }
    };
    KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    kfm.addKeyEventDispatcher(m_keyEventDispatcher);

    m_frame.setBackground(new Color(0, 0, 0, 0));
    m_canvas = new GridLayoutCanvas();
    m_frame.setContentPane(m_canvas);
    //
    List<JComponent> groupBoxComponents = new ArrayList<JComponent>();

    collectGroupBoxes(rootComponent, groupBoxComponents);
    int colorIndex = 0;
    for (JComponent g : groupBoxComponents) {
      renderGroupBoxLayout(g, rootComponent, (LogicalGridLayout) g.getLayout(), colors.get(colorIndex));
      colorIndex = (colorIndex + 1) % colors.size();
    }

    m_frame.setSize(rootComponent.getSize());
    m_frame.setLocationRelativeTo(rootComponent);
    m_frame.setVisible(true);
  }

  private void dispose() {
    KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    kfm.addKeyEventDispatcher(m_keyEventDispatcher);
    m_keyEventDispatcher = null;
    m_frame.dispose();
  }

  protected void collectGroupBoxes(Component comp, List<JComponent> groupBoxComponents) {
    if (comp instanceof JComponent) {
      JComponent jComp = (JComponent) comp;
      if (jComp.getClientProperty(GROUP_BOX_MARKER) != null && jComp.getLayout() instanceof LogicalGridLayout) {
        groupBoxComponents.add(jComp);
      }
    }
    if (comp instanceof Container) {
      for (Component childComp : ((Container) comp).getComponents()) {
        collectGroupBoxes(childComp, groupBoxComponents);
      }
    }
  }

  /**
   * @param group
   * @param layout
   */
  protected void renderGroupBoxLayout(JComponent group, Component rootComponent, LogicalGridLayout layout, Color color) {
    Rectangle bounds = group.getBounds();
    bounds.x = 0;
    bounds.y = 0;
    bounds = SwingUtilities.convertRectangle(group, bounds, rootComponent);
    m_canvas.addBounds(new GridLayoutCanvas.Bounds(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height), color));
    LogicalGridLayoutInfo info = layout.getInfo();
    if (info == null) {
      return;
    }
    Rectangle[][] cellBounds = info.getCellBounds();
    if (cellBounds == null) {
      return;
    }
    if (cellBounds.length > 0 && cellBounds[0].length > 0) {
      int[] xOffsets = new int[cellBounds[0].length - 1];
      int[] yOffsets = new int[cellBounds.length - 1];
      Rectangle prev = null;
      for (int x = 0; x < cellBounds[0].length; x++) {
        Rectangle b = cellBounds[0][x];
        if (prev != null) {
          xOffsets[x - 1] = b.x - ((b.x - (prev.x + prev.width)) / 2);
        }
        prev = b;
      }
      // horizontal
      prev = null;
      for (int y = 0; y < cellBounds.length; y++) {
        Rectangle b = cellBounds[y][0];
        if (prev != null) {
          yOffsets[y - 1] = b.y - ((b.y - (prev.y + prev.height)) / 2);
        }
        prev = b;
      }
      // vertical
      for (int x = 0; x < xOffsets.length; x++) {
        m_canvas.addLine(new GridLayoutCanvas.Line(SwingUtilities.convertPoint(group, xOffsets[x], 0, rootComponent), SwingUtilities.convertPoint(group, xOffsets[x], bounds.height, rootComponent), color));
      }

      for (int y = 0; y < yOffsets.length; y++) {
        m_canvas.addLine(new GridLayoutCanvas.Line(SwingUtilities.convertPoint(group, 0, yOffsets[y], rootComponent), SwingUtilities.convertPoint(group, bounds.width, yOffsets[y], rootComponent), color));
      }
    }
  }
}
