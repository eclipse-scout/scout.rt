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
package org.eclipse.scout.rt.ui.swt.internal.debug.layout.spy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayoutInfo;
import org.eclipse.scout.rt.ui.swt.internal.debug.layout.spy.GridCanvas.Bounds;
import org.eclipse.scout.rt.ui.swt.internal.debug.layout.spy.GridCanvas.Line;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class LogicalGridLayoutSpy extends Shell {
  public static final String GROUP_BOX_MARKER = "groupBoxMarker";

  private Shell m_parentShell;
  private GridCanvas m_canvas;
  private static int[] SYSTEM_COLORS = new int[]{SWT.COLOR_BLUE, SWT.COLOR_DARK_GREEN
      , SWT.COLOR_RED, SWT.COLOR_DARK_MAGENTA};

  /**
   * @param parent
   */
  public LogicalGridLayoutSpy(Shell parent) {
    super(parent, SWT.NO_TRIM | SWT.APPLICATION_MODAL);
    m_parentShell = parent;
    setAlpha(150);
    m_canvas = new GridCanvas(this);
    setLayout(new FillLayout());
    m_canvas.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        close();
      }
    });
  }

  @Override
  protected void checkSubclass() {
  }

  public void activate() {
    // find groupBoxes
    setBounds(m_parentShell.getBounds());
    List<Composite> groupBoxComposites = new ArrayList<Composite>();
    collectGroupBoxes(m_parentShell, groupBoxComposites);
    int i = 0;
    for (Composite group : groupBoxComposites) {
      int col = SYSTEM_COLORS[i];
      i = (i + 1) % SYSTEM_COLORS.length;
      renderGroupBoxLayout(group, (LogicalGridLayout) group.getLayout(), col);
    }
    setVisible(true);
  }

  /**
   * @param group
   * @param layout
   */
  protected void renderGroupBoxLayout(Composite group, LogicalGridLayout layout, int color) {
    Rectangle bounds = group.getClientArea();
    Point displayRelativePosition = group.toDisplay(0, 0);
    Point shellRelativePosition = getShell().toControl(displayRelativePosition);
    m_canvas.addBounds(new Bounds(new Rectangle(shellRelativePosition.x, shellRelativePosition.y, bounds.width, bounds.height), m_canvas.getDisplay().getSystemColor(color)));
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
        m_canvas.addLine(new Line(transformToShell(group, xOffsets[x], 0), transformToShell(group, xOffsets[x], bounds.y + bounds.height), group.getDisplay().getSystemColor(color)));
      }

      for (int y = 0; y < yOffsets.length; y++) {
        m_canvas.addLine(new Line(transformToShell(group, 0, yOffsets[y]), transformToShell(group, bounds.x + bounds.width, yOffsets[y]), group.getDisplay().getSystemColor(color)));
      }
    }
  }

  protected Point transformToShell(Control c, int x, int y) {
    Point displayRelativePosition = c.toDisplay(x, y);
    Point shellRelativePosition = getShell().toControl(displayRelativePosition);
    return shellRelativePosition;
  }

  /**
   * @param parentShell
   * @param groupBoxComposites
   */
  private void collectGroupBoxes(Composite composite, List<Composite> groupBoxComposites) {
    if (composite.getData(GROUP_BOX_MARKER) != null) {
      if (composite.getLayout() instanceof LogicalGridLayout) {
        groupBoxComposites.add(composite);
      }
    }
    // children
    for (Control child : composite.getChildren()) {
      if (child instanceof Composite) {
        collectGroupBoxes((Composite) child, groupBoxComposites);
      }
    }
  }

}
