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
package org.eclipse.scout.rt.ui.swing.ext.activitymap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputListener;

import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.ui.swing.ext.MouseClickedBugFix;

/**
 * activity map with column model and data model
 */
public class JActivityMap extends JComponent implements Scrollable {
  private static final long serialVersionUID = 1L;

  private EventListenerList m_listeners = new EventListenerList();
  private ActivityMapModel m_model;
  private ActivityMapColumnModel m_columnModel;
  private JActivityMapHeader m_header;
  private JSelector m_selector;
  private ActivityMapSelection m_selection;
  private MouseInputListener m_activityProxyMouseListener;
  private FocusListener m_activityProxyFocusListener;
  private JViewport m_viewport;// cached viewport
  // mouse state
  private boolean m_pressedInsideMap;
  private int m_selectorResizeType = 0;
  private IActivityMap<?, ?> m_scoutActivityMap;

  private int m_rowStartedDrag;
  private int m_currentRowDrag;

  public JActivityMap() {
    m_header = new JActivityMapHeader(this);
    setBackground(Color.white);
    m_selection = new ActivityMapSelection();
    m_selector = new JSelector(this);
    // proxy selector events to lower level (activity item / map), activity
    // items will proxy to map
    m_selector.addMouseListener(
        new MouseAdapter() {
          MouseClickedBugFix fix;

          @Override
          public void mousePressed(MouseEvent e) {
            fix = new MouseClickedBugFix(e);
            Component parent = getParentAt(e.getComponent(), e.getPoint());
            e = SwingUtilities.convertMouseEvent(e.getComponent(), e, parent);
            if (parent != null) {
              parent.dispatchEvent(e);
            }
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            Component parent = getParentAt(e.getComponent(), e.getPoint());
            if (parent != null) {
              parent.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, parent));
              if (fix != null) {
                fix.mouseReleased(this, e);
              }
            }
          }

          @Override
          public void mouseClicked(MouseEvent e) {
            if (fix.mouseClicked()) {
              return;
            }
            Component parent = getParentAt(e.getComponent(), e.getPoint());
            if (parent != null) {
              parent.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, parent));
            }
          }
        }
        );
    // proxy selector events to lower level (activity item / map), activity
    // items will proxy to map
    m_selector.addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseDragged(MouseEvent e) {
            Component parent = getParentAt(e.getComponent(), e.getPoint());
            if (parent != null) {
              parent.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, parent));
            }
          }
        }
        );
    setLayout(new ActivityMapLayout());
    add(m_selector);
    // listeners
    this.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            m_pressedInsideMap = false;
            if (isButton1(e)) {
              m_pressedInsideMap = true;
              ActivityMapSelection s = new ActivityMapSelection(m_selection);
              int row = pixToRow(e.getY());
              m_rowStartedDrag = row;
              double[] mouseRange = pixToRange(e.getX());
              if (m_selection.getRange() != null && m_selector.getCursor().getType() == Cursor.E_RESIZE_CURSOR) {
                m_selectorResizeType = m_selector.getCursor().getType();
                s.clear();
                s.setRows(m_selection.getRows());
                s.setRange(new double[]{m_selection.getRange()[0], mouseRange[1]});
              }
              else if (m_selection.getRange() != null && m_selector.getCursor().getType() == Cursor.W_RESIZE_CURSOR) {
                m_selectorResizeType = m_selector.getCursor().getType();
                s.clear();
                s.setRows(m_selection.getRows());
                s.setRange(new double[]{mouseRange[0], m_selection.getRange()[1]});
              }
              else if (e.isShiftDown()) {
                if (!s.hasAnchor()) {
                  s.setAnchor(row, mouseRange);
                }
                s.setLead(row, mouseRange);
              }
              else if (e.isControlDown()) {
                s.setAnchor(row, mouseRange);
                s.setLead(row, mouseRange);
              }
              else {
                s.clear();
                s.setAnchor(row, mouseRange);
                s.setLead(row, mouseRange);
              }
              setSelectionInternal(s);
            }
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            if (isButton1(e) && m_pressedInsideMap) {
              ActivityMapSelection s = new ActivityMapSelection(m_selection);
              int row = pixToRow(e.getY());
              double[] mouseRange = pixToRange(e.getX());
              s.consumeAnchorLead();
              s.setAnchor(row, mouseRange);
              setSelectionInternal(s);
            }
            m_selectorResizeType = 0;
            m_pressedInsideMap = false;
          }

          @Override
          public void mouseEntered(MouseEvent e) {
            if (isButton1(e) && m_pressedInsideMap) {
              ActivityMapSelection s = new ActivityMapSelection(m_selection);
              int row = pixToRow(e.getY());
              double[] mouseRange = pixToRange(e.getX());
              if (m_selectorResizeType == Cursor.E_RESIZE_CURSOR) {
                s.setRange(new double[]{m_selection.getRange()[0], mouseRange[1]});
              }
              else if (m_selectorResizeType == Cursor.W_RESIZE_CURSOR) {
                s.setRange(new double[]{mouseRange[0], m_selection.getRange()[1]});
              }
              else if (!s.hasAnchor()) {
                s.setAnchor(row, mouseRange);
                s.setLead(row, mouseRange);
              }
              setSelectionInternal(s);
            }
          }

          @Override
          public void mouseExited(MouseEvent e) {
            if (isButton1(e) && m_pressedInsideMap) {
              ActivityMapSelection s = new ActivityMapSelection(m_selection);
              int row = pixToRow(e.getY());
              double[] mouseRange = pixToRange(e.getX());
              if (m_selectorResizeType == Cursor.E_RESIZE_CURSOR) {
                s.setRange(new double[]{m_selection.getRange()[0], mouseRange[1]});
              }
              else if (m_selectorResizeType == Cursor.W_RESIZE_CURSOR) {
                s.setRange(new double[]{mouseRange[0], m_selection.getRange()[1]});
              }
              else {
                s.setLead(row, mouseRange);
              }
              setSelectionInternal(s);
            }
          }

        }
        );
    this.addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseDragged(MouseEvent e) {
            if (isButton1(e) && m_pressedInsideMap) {
              ActivityMapSelection s = new ActivityMapSelection(m_selection);
              int row = pixToRow(e.getY());
              m_currentRowDrag = row;
              double[] mouseRange = pixToRange(e.getX());
              if (m_selectorResizeType == Cursor.E_RESIZE_CURSOR) {
                s.setRange(new double[]{m_selection.getRange()[0], mouseRange[1]});
              }
              else if (m_selectorResizeType == Cursor.W_RESIZE_CURSOR) {
                s.setRange(new double[]{mouseRange[0], m_selection.getRange()[1]});
              }
              else {
                s.setRange(new double[]{mouseRange[0], mouseRange[1]});
                s.setLead(row, mouseRange);

                int[] selectedRows = new int[2];
                selectedRows[0] = m_rowStartedDrag;
                selectedRows[1] = m_currentRowDrag;
                s.setRows(selectedRows);
              }
              setSelectionInternal(s);
            }
          }
        }
        );
    // activity proxy mouse listener
    m_activityProxyMouseListener = new MouseInputListener() {
      MouseClickedBugFix fix;

      @Override
      public void mouseEntered(MouseEvent e) {
        EventListener[] listeners = m_listeners.getListeners(MouseListener.class);
        for (EventListener listener : listeners) {
          ((MouseListener) listener).mouseEntered(e);
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        EventListener[] listeners = m_listeners.getListeners(MouseListener.class);
        for (EventListener listener : listeners) {
          ((MouseListener) listener).mouseExited(e);
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {
        fix = new MouseClickedBugFix(e);
        EventListener[] listeners = m_listeners.getListeners(MouseListener.class);
        for (EventListener listener : listeners) {
          ((MouseListener) listener).mousePressed(e);
        }
        // only send down to map when button1
        if (isButton1(e)) {
          e = SwingUtilities.convertMouseEvent(e.getComponent(), e, JActivityMap.this);
          dispatchEvent(e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        EventListener[] listeners = m_listeners.getListeners(MouseListener.class);
        for (EventListener listener : listeners) {
          ((MouseListener) listener).mouseReleased(e);
        }
        // only send down to map when button1
        if (isButton1(e)) {
          dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, JActivityMap.this));
        }
        if (fix != null) {
          fix.mouseReleased(this, e);
        }
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if (fix.mouseClicked()) {
          return;
        }
        EventListener[] listeners = m_listeners.getListeners(MouseListener.class);
        for (EventListener listener : listeners) {
          ((MouseListener) listener).mouseClicked(e);
        }
        // only send down to map when button1 and single-click
        if (isButton1(e) && e.getClickCount() == 1) {
          e = SwingUtilities.convertMouseEvent(e.getComponent(), e, JActivityMap.this);
          dispatchEvent(e);
        }
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        // only send down to map when button1
        if (isButton1(e)) {
          dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, JActivityMap.this));
        }
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        // only send down to map when button1
        if (isButton1(e)) {
          dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, JActivityMap.this));
        }
      }
    };
    // activity proxy focus listener
    m_activityProxyFocusListener = new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        EventListener[] listeners = m_listeners.getListeners(FocusListener.class);
        for (EventListener listener : listeners) {
          ((FocusListener) listener).focusGained(e);
        }
      }

      @Override
      public void focusLost(FocusEvent e) {
        EventListener[] listeners = m_listeners.getListeners(FocusListener.class);
        for (EventListener listener : listeners) {
          ((FocusListener) listener).focusLost(e);
        }
      }
    };
    // set models
    setModel(new ActivityMapModel() {
      @Override
      public int getRowCount() {
        return 0;
      }

      @Override
      public int getRowHeight(int rowIndex) {
        return 0;
      }

      @Override
      public int getRowLocation(int rowIndex) {
        return 0;
      }

      @Override
      public int getRowAtLocation(int y) {
        return -1;
      }

      @Override
      public int getHeaderHeight() {
        return 0;
      }

      @Override
      public ActivityComponent[] getActivities() {
        return new ActivityComponent[0];
      }

      @Override
      public double[] getActivityRange(ActivityComponent a) {
        return null;
      }
    });
    setColumnModel(new ActivityMapColumnModel() {
      @Override
      public double[] getMajorColumnRange(Object majorColumn) {
        return new double[2];
      }

      @Override
      public String getMajorColumnText(Object column, int size) {
        return null;
      }

      @Override
      public Object[] getMajorColumns() {
        return new Object[0];
      }

      @Override
      public double[] getMinorColumnRange(Object mainorColumn) {
        return new double[2];
      }

      @Override
      public String getMinorColumnText(Object column, int size) {
        return null;
      }

      @Override
      public Object[] getMinorColumns(Object majorColumn) {
        return new Object[0];
      }

      @Override
      public double[] snapRange(double d) {
        return new double[]{0, 0};
      }

      @Override
      public String getMajorColumnTooltipText(Object column) {
        return null;
      }

      @Override
      public String getMinorColumnTooltipText(Object column) {
        return null;
      }
    });
  }

  public void addActivityMapSelectionListener(ActivityMapSelectionListener listener) {
    m_listeners.add(ActivityMapSelectionListener.class, listener);
  }

  public void removeActivityMapSelectionListener(ActivityMapSelectionListener listener) {
    m_listeners.remove(ActivityMapSelectionListener.class, listener);
  }

  public void addActivityProxyMouseListener(MouseListener listener) {
    m_listeners.add(MouseListener.class, listener);
  }

  public void removeActivityProxyMouseListener(MouseListener listener) {
    m_listeners.remove(MouseListener.class, listener);
  }

  public void addActivityProxyFocusListener(FocusListener listener) {
    m_listeners.add(FocusListener.class, listener);
  }

  public void removeActivityProxyFocusListener(FocusListener listener) {
    m_listeners.remove(FocusListener.class, listener);
  }

  private void fireSelectionChanged() {
    fireActivityMapSelectionEvent(new ActivityMapSelectionEvent(this, getSelectedRows(), getSelectedRange()));
  }

  private void fireActivityMapSelectionEvent(ActivityMapSelectionEvent e) {
    EventListener[] listeners = m_listeners.getListeners(ActivityMapSelectionListener.class);
    for (EventListener listener : listeners) {
      ((ActivityMapSelectionListener) listener).selectionChanged(e);
    }
  }

  public int getRowCount() {
    return m_model.getRowCount();
  }

  public double[] pixToRange(int x) {
    return getColumnModel().snapRange(1.0 * x / getVisibleViewportView().width);
  }

  public int pixToRow(int y) {
    int i = getModel().getRowAtLocation(y);
    if (i < 0) {
      i = 0;
    }
    else if (i >= getModel().getRowCount()) {
      i = getModel().getRowCount() - 1;
    }
    return i;
  }

  private Rectangle getVisibleViewportView() {
    if (m_viewport != null) {
      return m_viewport.getVisibleRect();
    }
    else {
      return getBounds();
    }
  }

  public boolean isInsideSelection(MouseEvent e) {
    if (m_selection != null) {
      Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), this);
      return m_selection.contains(pixToRow(p.y), pixToRange(p.x));
    }
    else {
      return false;
    }
  }

  public ActivityMapModel getModel() {
    return m_model;
  }

  public void setModel(ActivityMapModel m) {
    if (m != null && m != m_model) {
      m_model = m;
      // rebuild activities
      for (Component c : getComponents()) {
        if (c instanceof ActivityComponent) {
          c.removeMouseListener(m_activityProxyMouseListener);
          c.removeMouseMotionListener(m_activityProxyMouseListener);
          c.removeFocusListener(m_activityProxyFocusListener);
        }
      }
      removeAll();
      add(m_selector);
      for (ActivityComponent a : m_model.getActivities()) {
        add((JComponent) a);
        ((JComponent) a).addMouseListener(m_activityProxyMouseListener);
        ((JComponent) a).addMouseMotionListener(m_activityProxyMouseListener);
        ((JComponent) a).addFocusListener(m_activityProxyFocusListener);
      }
      revalidateAndRepaint();
    }
  }

  public ActivityMapColumnModel getColumnModel() {
    return m_columnModel;
  }

  public void setColumnModel(ActivityMapColumnModel m) {
    if (m != null && m != m_columnModel) {
      m_columnModel = m;
      revalidateAndRepaint();
    }
  }

  public JActivityMapHeader getActivityMapHeader() {
    return m_header;
  }

  public Rectangle getCellRect(int beginRowIndex, int endRowIndex, double[] normalizedColumnRange) {
    Rectangle r = getRect(normalizedColumnRange);
    if (beginRowIndex <= endRowIndex) {
      if (beginRowIndex >= 0) {
        r.y = getModel().getRowLocation(beginRowIndex);
      }
      if (endRowIndex >= 0) {
        r.height = getModel().getRowLocation(endRowIndex) + getModel().getRowHeight(endRowIndex) - r.y;
      }
    }
    return r;
  }

  public Rectangle getRect(double[] normalizedColumnRange) {
    Rectangle r = new Rectangle();
    if (normalizedColumnRange != null) {
      int w = getVisibleViewportView().width;
      int a = (int) (normalizedColumnRange[0] * w);
      if (a < 0) {
        a = -1;
      }
      if (a >= w) {
        a = w;
      }
      int b = (int) (normalizedColumnRange[1] * w);
      if (b < 0) {
        b = -1;
      }
      if (b >= w) {
        b = w;
      }
      r.x = a;
      r.width = b - a;
    }
    return r;
  }

  public JSelector getSelector() {
    return m_selector;
  }

  /**
   * @return the selectors normalized location [begin,end] in the range
   *         0..1,0..1 or null if there is no selection
   */
  public double[] getSelectedRange() {
    return m_selection.getRange();
  }

  private void setSelectionInternal(ActivityMapSelection s) {
    if (!s.equals(m_selection)) {
      m_selection = s;
      m_selector.revalidate();
      m_selector.repaint();
      fireSelectionChanged();
    }
  }

  public int[] getSelectedRows() {
    return m_selection.getRows();
  }

  public ActivityComponent getFocusedActivity() {
    Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    if (comp instanceof ActivityComponent) {
      if (SwingUtilities.isDescendingFrom(comp, this)) {
        return (ActivityComponent) comp;
      }
    }
    return null;
  }

  /**
   * set the selectors normalized location [begin,end] in the range 0..1,0..1
   */
  public void setSelection(int[] newRows, double[] newRange) {
    ActivityMapSelection s = new ActivityMapSelection(m_selection);
    s.setRows(newRows);
    s.setRange(newRange);
    setSelectionInternal(s);
  }

  @Override
  public void addNotify() {
    super.addNotify();
    configureEnclosingScrollPane();
  }

  @Override
  public void removeNotify() {
    unconfigureEnclosingScrollPane();
    super.removeNotify();
  }

  protected void configureEnclosingScrollPane() {
    Container p = getParent();
    if (p instanceof JViewport) {
      Container gp = p.getParent();
      if (gp instanceof JScrollPane) {
        JScrollPane scrollPane = (JScrollPane) gp;
        // Make certain we are the viewPort's view and not, for
        // example, the rowHeaderView of the scrollPane -
        // an implementor of fixed columns might do this.
        final JViewport viewport = scrollPane.getViewport();
        if (viewport == null || viewport.getView() != this) {
          return;
        }
        m_viewport = viewport;
        scrollPane.setColumnHeaderView(getActivityMapHeader());
      }
    }
  }

  protected void unconfigureEnclosingScrollPane() {
    Container p = getParent();
    if (p instanceof JViewport) {
      Container gp = p.getParent();
      if (gp instanceof JScrollPane) {
        JScrollPane scrollPane = (JScrollPane) gp;
        // Make certain we are the viewPort's view and not, for
        // example, the rowHeaderView of the scrollPane -
        // an implementor of fixed columns might do this.
        JViewport viewport = scrollPane.getViewport();
        if (viewport == null || viewport.getView() != this) {
          return;
        }
        m_viewport = null;
        scrollPane.setColumnHeaderView(null);
      }
    }
  }

  protected void revalidateAndRepaint() {
    revalidate();
    m_header.revalidate();
    repaint();
    m_header.repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    paintGrid(g);
  }

  @Override
  protected void paintChildren(Graphics g) {
    super.paintChildren(g);
  }

  private void paintGrid(Graphics g) {
    int rowCount = getModel().getRowCount();
    if (rowCount > 0) {
      Object[] majorCols = getColumnModel().getMajorColumns();
      ArrayList<Object> minorColList = new ArrayList<Object>();
      for (Object o : majorCols) {
        minorColList.addAll(Arrays.asList(getColumnModel().getMinorColumns(o)));
      }
      Object[] minorCols = minorColList.toArray();
      //
      int[] rows = new int[rowCount];
      int[] heights = new int[rowCount];
      int minY;
      int maxY;
      Rectangle[] majorRects = new Rectangle[majorCols.length];
      Rectangle[] minorRects = new Rectangle[minorCols.length];
      for (int i = 0; i < rows.length; i++) {
        rows[i] = getModel().getRowLocation(i);
        heights[i] = getModel().getRowHeight(i);
      }
      minY = rows[0];
      maxY = rows[rows.length - 1] + heights[heights.length - 1];
      for (int i = 0; i < minorRects.length; i++) {
        minorRects[i] = getRect(getColumnModel().getMinorColumnRange(minorCols[i]));
        minorRects[i].y = minY;
        minorRects[i].height = maxY - minY;
        // correct aliasing effects
        if (i > 0) {
          if (minorRects[i - 1].x + minorRects[i - 1].width + 1 >= minorRects[i].x) {
            minorRects[i - 1].width = minorRects[i].x - minorRects[i - 1].x;
          }
        }
      }
      for (int i = 0; i < majorRects.length; i++) {
        majorRects[i] = getRect(getColumnModel().getMajorColumnRange(majorCols[i]));
        majorRects[i].y = minY;
        majorRects[i].height = maxY - minY;
      }
      // fine grid
      g.setColor(new Color(0xeeeeee));
      // h
      for (Rectangle rect : majorRects) {
        for (int r = 0; r < rows.length; r++) {
          g.drawLine(rect.x, rows[r], rect.x + rect.width, rows[r]);
        }
      }
      // v
      for (Rectangle rect : minorRects) {
        g.drawLine(rect.x, minY, rect.x, maxY);
        g.drawLine(rect.x + rect.width, minY, rect.x + rect.width, maxY);
      }
      // grid outline
      g.setColor(Color.lightGray);
      g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
      Graphics2D g2d = (Graphics2D) g;
      Stroke old = g2d.getStroke();
      try {
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, new float[]{1f, 3f}, 1));
        for (int i = 0; i < majorRects.length; i++) {
          Rectangle r = majorRects[i];
          if (i > 0) {
            g.drawLine(r.x, r.y, r.x, r.height);
          }
          if (i + 1 < majorRects.length) {
            g.drawLine(r.x + r.width - 1, r.y, r.x + r.width - 1, r.height);
          }
        }
      }
      finally {
        g2d.setStroke(old);
      }
    }
  }

  private static boolean isButton1(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1) {
      return true;
    }
    if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
      return true;
    }
    return false;
  }

  /*
   * Implementation of Scrollable
   */

  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return new Dimension(10240, 10240);
  }

  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (direction == SwingConstants.HORIZONTAL) {
      return 100;
    }
    else {
      if (getModel().getRowCount() > 0) {
        return getModel().getRowHeight(0);
      }
      else {
        return 24;
      }
    }
  }

  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (direction == SwingConstants.HORIZONTAL) {
      return visibleRect.width;
    }
    else {
      return visibleRect.height;
    }
  }

  @Override
  public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  @Override
  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  public static Component getParentAt(Component comp, Point p) {
    return getDeepestComponentAt(comp.getParent(), comp.getX() + p.x, comp.getY() + p.y, comp);
  }

  public static Component getDeepestComponentAt(Component parent, int x, int y, Component toExclude) {
    if (!parent.contains(x, y)) {
      return null;
    }
    if (parent instanceof Container) {
      Component[] components = ((Container) parent).getComponents();
      for (int i = 0; i < components.length; i++) {
        Component comp = components[i];
        if (comp != null && comp.isVisible() && comp != toExclude) {
          Point loc = comp.getLocation();
          if (comp instanceof Container) {
            comp = getDeepestComponentAt(comp, x - loc.x, y - loc.y, toExclude);
          }
          else {
            comp = comp.getComponentAt(x - loc.x, y - loc.y);
          }
          if (comp != null && comp.isVisible()) {
            return comp;
          }
        }
      }
    }
    return parent;
  }

  public void setActivityMap(IActivityMap<?, ?> scoutObject) {
    m_scoutActivityMap = scoutObject;
  }

  public IActivityMap<?, ?> getActivityMap() {
    return m_scoutActivityMap;
  }
}
