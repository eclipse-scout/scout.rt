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
package org.eclipse.scout.rt.testing.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.JWindow;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.TextTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.WaitCondition;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.icons.CheckboxIcon;
import org.eclipse.scout.testing.client.IGuiMock;
import org.eclipse.scout.testing.client.robot.JavaRobot;

/**
 * Uses {@link Robot}
 */
public class SwingMock implements IGuiMock {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingMock.class);

  static interface MockRunnable<T> {
    T run() throws Throwable;
  }

  private final IClientSession m_session;
  private final JavaRobot m_bot;
  private int m_treeNodeToExpandIconGap;

  public SwingMock(IClientSession session) {
    m_session = session;
    m_bot = new JavaRobot();
  }

  @Override
  public void initializeMock() {
  }

  @Override
  public void shutdownMock() {
  }

  @Override
  public void beforeTest() {
  }

  @Override
  public void afterTest() {
  }

  @Override
  public GuiStrategy getStrategy() {
    return GuiStrategy.Swing;
  }

  public int getTreeNodeToExpandIconGap() {
    if (m_treeNodeToExpandIconGap <= 0) {
      String s = Activator.getDefault().getBundle().getBundleContext().getProperty("IGuiMock.treeNodeToExpandIconGap");
      if (s == null) {
        LOG.warn("Missing config.ini property 'IGuiMock.treeNodeToExpandIconGap'; using default value of 4");
        s = "4";
      }
      m_treeNodeToExpandIconGap = Integer.parseInt(s);
    }
    return m_treeNodeToExpandIconGap;
  }

  public void setTreeNodeToExpandIconGap(int treeNodeToExpandIconGap) {
    m_treeNodeToExpandIconGap = treeNodeToExpandIconGap;
  }

  @Override
  public void waitForIdle() {
    if (SwingUtilities.isEventDispatchThread()) {
      return;
    }
    //
    for (int pass = 0; pass < 2; pass++) {
      m_bot.sleep(80);
      //wait until gui queue is empty
      syncExec(new MockRunnable<Object>() {
        @Override
        public Object run() throws Throwable {
          return null;
        }
      });
      //wait until model queue is empty
      ClientSyncJob idleJob = new ClientSyncJob("Check for idle", m_session) {
        @Override
        protected void runVoid(IProgressMonitor m) throws Throwable {
        }
      };
      idleJob.setSystem(true);

      final CountDownLatch idleJobScheduledSignal = new CountDownLatch(1);
      JobChangeAdapter listener = new JobChangeAdapter() {
        @Override
        public void done(IJobChangeEvent event) {
          idleJobScheduledSignal.countDown();
        }
      };

      try {
        idleJob.addJobChangeListener(listener);
        idleJob.schedule();
        try {
          idleJobScheduledSignal.await();
        }
        catch (InterruptedException e) {
          throw new IllegalStateException("Interrupted");
        }
      }
      finally {
        idleJob.removeJobChangeListener(listener);
      }
    }
  }

  @Override
  public void waitForActiveWindow(final String title) {
    waitUntil(new WaitCondition<Object>() {
      @Override
      public Object run() {
        if (isWindowActive(title)) {
          return true;
        }
        else {
          return null;
        }
      }
    });
    waitForIdle();
  }

  @Override
  public void waitForOpenWindow(final String title) {
    waitUntil(new WaitCondition<Object>() {
      @Override
      public Object run() {
        if (isWindowOpen(title)) {
          return true;
        }
        else {
          return null;
        }
      }
    });
    waitForIdle();
  }

  @Override
  public int getSleepDelay() {
    return m_bot.getAutoDelay();
  }

  @Override
  public void setSleepDelay(int sleepDelay) {
    m_bot.setAutoDelay(sleepDelay);
  }

  @Override
  public void sleep() {
    sleep(getSleepDelay());
  }

  @Override
  public void sleep(int millis) {
    //only sleep when NOT in gui thread
    if (SwingUtilities.isEventDispatchThread()) {
      return;
    }
    m_bot.sleep(millis);
    waitForIdle();
  }

  @Override
  public boolean isWindowActive(final String title) {
    return syncExec(new MockRunnable<Boolean>() {
      @Override
      public Boolean run() throws Throwable {
        for (Window w : findWindows(title)) {
          if (w.isActive()) {
            return true;
          }
        }
        JInternalFrame f = findInternalFrame(title);
        if (f != null) {
          return f.isSelected();
        }
        return false;
      }
    });
  }

  @Override
  public boolean isWindowOpen(final String title) {
    return syncExec(new MockRunnable<Boolean>() {
      @Override
      public Boolean run() throws Throwable {
        for (Window w : findWindows(title)) {
          if (w.isVisible()) {
            return true;
          }
        }
        JInternalFrame f = findInternalFrame(title);
        if (f != null) {
          return f.isVisible();
        }
        return false;
      }
    });
  }

  @Override
  public void activateWindow(final String title) {
    waitForOpenWindow(title);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        Window w = findWindow(title);
        if (w != null) {
          w.requestFocusInWindow();
          return null;
        }
        JInternalFrame f = findInternalFrame(title);
        if (f != null) {
          f.requestFocusInWindow();
          f.setSelected(true);
          return null;
        }
        throw new IllegalStateException("There is no view with title " + title);
      }
    });
    waitForIdle();
  }

  @Override
  public void clickOnPushButton(String text) {
    final JComponent c = waitForPushButtonWithLabel(text);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        Point p = c.getLocationOnScreen();
        gotoPoint(p.x + c.getWidth() / 2, p.y + c.getHeight() / 2);
        clickLeft();
        return null;
      }
    });
    waitForIdle();
  }

  @Override
  public void gotoField(FieldType type, int fieldIndex) {
    final JComponent c = waitForIndexedField(type, fieldIndex);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        Point p = c.getLocationOnScreen();
        gotoPoint(p.x + c.getWidth() / 2, p.y + c.getHeight() / 2);
        return null;
      }
    });
  }

  @Override
  public void gotoScoutField(String name) {
    final JComponent c = waitForScoutField(name);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        Point p = c.getLocationOnScreen();
        gotoPoint(p.x + c.getWidth() / 2, p.y + c.getHeight() / 2);
        return null;
      }
    });
  }

  @Override
  public void gotoTable(int tableIndex, final int rowIndex, final int columnIndex) {
    final JTable table = (JTable) waitForIndexedField(FieldType.Table, tableIndex);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        Rectangle r = getTableCellBounds(table, rowIndex, columnIndex);
        if (!table.getVisibleRect().contains(r.x + r.width / 2, r.y + r.height / 2)) {
          throw new IllegalStateException("table cell " + rowIndex + "," + columnIndex + " is not visible on screen");
        }
        Point p = table.getLocationOnScreen();
        gotoPoint(p.x + r.x + r.width / 2, p.y + r.y + r.height / 2);
        return null;
      }
    });
  }

  @Override
  public void gotoTableHeader(int tableIndex, final int columnIndex) {
    final JTable table = (JTable) waitForIndexedField(FieldType.Table, tableIndex);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        Rectangle r = getTableHeaderCellBounds(table, columnIndex);
        Point p = table.getTableHeader().getLocationOnScreen();
        gotoPoint(p.x + r.x + r.width / 2, p.y + r.y + r.height / 2);
        return null;
      }
    });
  }

  @Override
  public void gotoTree(int treeIndex, final String nodeText) {
    final JTree tree = (JTree) waitForIndexedField(FieldType.Tree, treeIndex);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        Rectangle r = getTreeRowBounds(tree, getTreeRowIndex(tree, nodeText));
        if (!tree.getVisibleRect().contains(r.x + r.width / 2, r.y + r.height / 2)) {
          throw new IllegalStateException("tree node " + nodeText + " is not visible on screen");
        }
        Point p = tree.getLocationOnScreen();
        gotoPoint(p.x + r.x + r.width / 2, p.y + r.y + r.height / 2);
        return null;
      }
    });
  }

  @Override
  public void gotoTreeExpandIcon(int treeIndex, final String nodeText) {
    final JTree tree = (JTree) waitForIndexedField(FieldType.Tree, treeIndex);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        Rectangle r = getTreeRowBounds(tree, getTreeRowIndex(tree, nodeText));
        if (!tree.getVisibleRect().contains(r.x + r.width / 2, r.y + r.height / 2)) {
          throw new IllegalStateException("tree node " + nodeText + " is not visible on screen");
        }
        Point p = tree.getLocationOnScreen();
        gotoPoint(p.x + r.x - getTreeNodeToExpandIconGap() - 2, p.y + r.y + r.height / 2);
        return null;
      }
    });
  }

  @Override
  public void contextMenu(final String... names) {
    //move to menu
    for (int i = 0; i < names.length; i++) {
      String label = names[i];
      final boolean lastItem = i == names.length - 1;
      final Component m = waitForContextMenu(label);
      syncExec(new MockRunnable<Boolean>() {
        @Override
        public Boolean run() throws Throwable {
          Point p = m.getLocationOnScreen();
          gotoPoint(p.x + m.getWidth() / 2, p.y + m.getHeight() / 2);
          if (lastItem) {
            //click on last menu
            clickLeft();
          }
          return null;
        }
      });
      waitForIdle();
    }
  }

  @Override
  public List<String> getTableCells(int tableIndex, final int columnIndex) {
    final JTable table = (JTable) waitForIndexedField(FieldType.Table, tableIndex);
    return syncExec(new MockRunnable<List<String>>() {
      @Override
      public List<String> run() throws Throwable {
        ArrayList<String> list = new ArrayList<String>();
        for (int row = 0; row < table.getRowCount(); row++) {
          list.add(getTableCellText(table, row, columnIndex));
        }
        return list;
      }
    });
  }

  @Override
  public List<String> getTreeNodes(int treeIndex) {
    final JTree tree = (JTree) waitForIndexedField(FieldType.Tree, treeIndex);
    return syncExec(new MockRunnable<List<String>>() {
      @Override
      public List<String> run() throws Throwable {
        ArrayList<String> list = new ArrayList<String>();
        for (int row = 0; row < tree.getRowCount(); row++) {
          list.add(getTreeRowText(tree, row));
        }
        return list;
      }
    });
  }

  @Override
  public Set<String> getSelectedTableCells(int tableIndex, final int columnIndex) {
    final JTable table = (JTable) waitForIndexedField(FieldType.Table, tableIndex);
    return syncExec(new MockRunnable<Set<String>>() {
      @Override
      public Set<String> run() throws Throwable {
        TreeSet<String> set = new TreeSet<String>();
        int[] sel = table.getSelectedRows();
        if (sel != null) {
          for (int row : sel) {
            set.add(getTableCellText(table, row, columnIndex));
          }
        }
        return set;
      }
    });
  }

  /**
   * Works only if checked Row is visible
   */
  @Override
  public Set<String> getCheckedTableCells(int tableIndex, final int columnIndex) {
    final JTable table = (JTable) waitForIndexedField(FieldType.Table, tableIndex);
    return syncExec(new MockRunnable<Set<String>>() {
      @Override
      public Set<String> run() throws Throwable {
        TreeSet<String> check = new TreeSet<String>();
        for (int i = 0; i < table.getRowCount(); i++) {
          Icon icon = getTableCellIcon(table, i);
          if (icon instanceof CheckboxIcon) {
            if (((CheckboxIcon) icon).isSelecetd()) {
              check.add(getTableCellText(table, i, columnIndex));
            }
          }
        }
        return check;
      }
    });
  }

  @Override
  public Set<String> getSelectedTreeNodes(int treeIndex) {
    final JTree tree = (JTree) waitForIndexedField(FieldType.Tree, treeIndex);
    return syncExec(new MockRunnable<Set<String>>() {
      @Override
      public Set<String> run() throws Throwable {
        TreeSet<String> set = new TreeSet<String>();
        int[] sel = tree.getSelectionRows();
        if (sel != null) {
          for (int row : sel) {
            set.add(getTreeRowText(tree, row));
          }
        }
        return set;
      }
    });
  }

  @Override
  public FieldState getFocusFieldState() {
    return syncExec(new MockRunnable<FieldState>() {
      @Override
      public FieldState run() throws Throwable {
        checkActiveWindow();
        Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (c == null) {
          throw new IllegalStateException("There is no focus owner");
        }
        if (!(c instanceof JComponent)) {
          throw new IllegalStateException("Focus owner is not a swing field");
        }
        return getFieldStateInternal((JComponent) c);
      }
    });
  }

  @Override
  public FieldState getFieldState(FieldType type, int index) {
    final JComponent c = waitForIndexedField(type, index);
    return syncExec(new MockRunnable<FieldState>() {
      @Override
      public FieldState run() throws Throwable {
        return getFieldStateInternal(c);
      }
    });
  }

  @Override
  public FieldState getScoutFieldState(String name) {
    final JComponent c = waitForScoutField(name);
    return syncExec(new MockRunnable<FieldState>() {
      @Override
      public FieldState run() throws Throwable {
        return getFieldStateInternal(c);
      }
    });
  }

  @Override
  public FieldState getScoutFieldContainerState(String name) {
    final JComponent c = waitForScoutField(name);
    return syncExec(new MockRunnable<FieldState>() {
      @Override
      public FieldState run() throws Throwable {
        ISwingScoutComposite swingScoutComposite = SwingScoutComposite.getCompositeOnWidget(c);
        if (swingScoutComposite == null) {
          return null;
        }

        return getFieldStateInternal(swingScoutComposite.getSwingContainer());
      }
    });
  }

  @Override
  public List<FieldState> getFieldStates(final FieldType type) {
    return syncExec(new MockRunnable<List<FieldState>>() {
      @Override
      public List<FieldState> run() throws Throwable {
        checkActiveWindow();
        List<FieldState> list = new ArrayList<FieldState>();
        for (Component parent : enumerateParentContainers()) {
          for (JComponent c : SwingUtility.findChildComponents(parent, JComponent.class)) {
            if (type == null && getFieldTypeOf(c) != null) {
              list.add(getFieldStateInternal(c));
            }
            else if (type != null && getFieldTypeOf(c) == type) {
              list.add(getFieldStateInternal(c));
            }
          }
        }
        return list;
      }
    });
  }

  @Override
  public void gotoPoint(int x, int y) {
    m_bot.moveTo(x, y);
  }

  @Override
  public void move(int deltaX, int deltaY) {
    m_bot.moveDelta(deltaX, deltaY);
  }

  @Override
  public void clickLeft() {
    m_bot.clickLeft();
    waitForIdle();
  }

  @Override
  public void clickRight() {
    m_bot.clickRight();
    waitForIdle();
  }

  @Override
  public void pressLeft() {
    m_bot.pressLeft();
  }

  @Override
  public void releaseLeft() {
    m_bot.releaseLeft();
    waitForIdle();
  }

  @Override
  public void drag(int x1, int y1, int x2, int y2) {
    gotoPoint(x1, y1);
    m_bot.pressLeft();
    gotoPoint(x2, y2);
    m_bot.releaseLeft();
    waitForIdle();
  }

  @Override
  public void dragWindowRightBorder(WindowState windowState, int pixelToMoveOnX) {
    int borderSize = 2;

    int xPos = windowState.x + windowState.width + borderSize;
    int yPos = windowState.y + windowState.height / 2;
    drag(xPos, yPos, xPos + pixelToMoveOnX, yPos);
  }

  @Override
  public void typeText(final String text) {
    m_bot.typeText(text);
    waitForIdle();
  }

  @Override
  public void pressKey(Key key) {
    m_bot.pressKey(key);
    waitForIdle();
  }

  @Override
  public void paste(final String text) {
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    //press paste (ctrl-V)
    m_bot.pressKey(Key.Control);
    m_bot.typeText("v");
    m_bot.releaseKey(Key.Control);
  }

  @Override
  public void releaseKey(Key key) {
    m_bot.releaseKey(key);
    waitForIdle();
  }

  @Override
  public void typeKey(Key key) {
    m_bot.typeKey(key);
    waitForIdle();
  }

  @Override
  public WindowState getWindowState(final String title) {
    return syncExec(new MockRunnable<WindowState>() {
      @Override
      public WindowState run() throws Throwable {
        checkActiveWindow();
        JInternalFrame part = findInternalFrame(title);
        if (part != null) {
          Rectangle r = part.getBounds();
          WindowState state = new WindowState();
          Point p = part.getLocationOnScreen();
          state.x = p.x;
          state.y = p.y;
          state.width = r.width;
          state.height = r.height;
          return state;
        }
        Window w = findWindow(title);
        if (w != null) {
          Rectangle r = w.getBounds();
          WindowState state = new WindowState();
          Point p = w.getLocationOnScreen();
          state.x = p.x;
          state.y = p.y;
          state.width = r.width;
          state.height = r.height;
          return state;
        }
        throw new IllegalStateException("Window " + title + " not found");
      }
    });
  }

  @Override
  public String getClipboardText() {
    waitForIdle();
    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    TransferObject o = SwingUtility.createScoutTransferable(t);
    if (o != null && o.isText()) {
      return ((TextTransferObject) o).getPlainText();
    }
    return null;
  }

  @Override
  public Object internal0(final Object o) {
    return null;
  }

  protected FieldState getFieldStateInternal(Container c) {
    FieldState state = new FieldState();
    //type
    state.type = getFieldTypeOf(c);
    //scout name
    state.scoutName = getScoutNameOf(c);
    //focus
    state.focus = c.isFocusOwner();
    //bounds
    Point p = c.getLocationOnScreen();
    state.x = p.x;
    state.y = p.y;
    state.width = c.getWidth();
    state.height = c.getHeight();
    //text
    if (c instanceof JLabel) {
      state.text = ((JLabel) c).getText();
    }
    if (c instanceof JTextComponent) {
      state.text = ((JTextComponent) c).getText();
    }
    if (c instanceof AbstractButton) {
      state.text = ((AbstractButton) c).getText();
    }
    return state;
  }

  protected int getTreeRowIndex(JTree tree, String nodeText) {
    for (int i = 0; i < tree.getRowCount(); i++) {
      Component tmp = tree.getCellRenderer().getTreeCellRendererComponent(tree, tree.getPathForRow(i).getLastPathComponent(), false, false, false, i, false);
      if (tmp instanceof JLabel) {
        JLabel label = (JLabel) tmp;
        if (nodeText.equals(label.getText())) {
          return i;
        }
      }
    }
    return -1;
  }

  protected String getTreeRowText(JTree tree, int rowIndex) {
    if (rowIndex < 0 || rowIndex > tree.getRowCount()) {
      throw new IllegalStateException("Tree has " + tree.getRowCount() + " rows (accessing " + rowIndex + ")");
    }
    Component tmp = tree.getCellRenderer().getTreeCellRendererComponent(tree, tree.getPathForRow(rowIndex).getLastPathComponent(), false, false, false, rowIndex, false);
    if (tmp instanceof JLabel) {
      JLabel label = (JLabel) tmp;
      return label.getText();
    }
    return null;
  }

  protected Rectangle getTreeRowBounds(JTree tree, int rowIndex) {
    if (rowIndex < 0 || rowIndex > tree.getRowCount()) {
      throw new IllegalStateException("Tree has " + tree.getRowCount() + " rows (accessing " + rowIndex + ")");
    }
    return tree.getPathBounds(tree.getPathForRow(rowIndex));
  }

  protected String getTableCellText(JTable table, int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex > table.getRowCount()) {
      throw new IllegalStateException("Table has " + table.getRowCount() + " rows (accessing " + rowIndex + ")");
    }
    if (columnIndex < 0 || columnIndex > table.getColumnCount()) {
      throw new IllegalStateException("Table has " + table.getColumnCount() + " columns (accessing " + columnIndex + ")");
    }
    Component label = table.prepareRenderer(table.getCellRenderer(rowIndex, columnIndex), rowIndex, columnIndex);
    if (label instanceof JLabel) {
      return ((JLabel) label).getText();
    }
    return null;
  }

  protected Icon getTableCellIcon(JTable table, int rowIndex) {
    if (rowIndex < 0 || rowIndex > table.getRowCount()) {
      throw new IllegalStateException("Table has " + table.getRowCount() + " rows (accessing " + rowIndex + ")");
    }
    Component label = table.prepareRenderer(table.getCellRenderer(rowIndex, 0), rowIndex, 0);
    if (label instanceof JLabel) {
      return ((JLabel) label).getIcon();
    }
    return null;
  }

  protected Rectangle getTableCellBounds(JTable table, int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex > table.getRowCount()) {
      throw new IllegalStateException("Table has " + table.getRowCount() + " rows (accessing " + rowIndex + ")");
    }
    if (columnIndex < 0 || columnIndex > table.getColumnCount()) {
      throw new IllegalStateException("Table has " + table.getColumnCount() + " columns (accessing " + columnIndex + ")");
    }
    return table.getCellRect(rowIndex, columnIndex, true);
  }

  protected Rectangle getTableHeaderCellBounds(JTable table, int columnIndex) {
    if (columnIndex < 0 || columnIndex > table.getColumnCount()) {
      throw new IllegalStateException("Table has " + table.getColumnCount() + " columns (accessing " + columnIndex + ")");
    }
    return table.getTableHeader().getHeaderRect(columnIndex);
  }

  protected void checkActiveWindow() {
    if (getActiveWindow() == null) {
      throw new IllegalStateException("There is no active view");
    }
  }

  protected Window getActiveWindow() {
    Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    return w;
  }

  protected String cleanButtonLabel(String s) {
    return StringUtility.removeMnemonic(s);
  }

  protected Window findWindow(String title) {
    for (Window w : Window.getWindows()) {
      if (w.isVisible()) {
        if (w instanceof Dialog && title.equals(((Dialog) w).getTitle())) {
          return w;
        }
        else if (w instanceof Frame && title.equals(((Frame) w).getTitle())) {
          return w;
        }
      }
    }
    return null;
  }

  protected List<Window> findWindows(String title) {
    ArrayList<Window> list = new ArrayList<Window>();
    for (Window w : Window.getWindows()) {
      if (w.isVisible()) {
        if (w instanceof Dialog && title.equals(((Dialog) w).getTitle())) {
          list.add(w);
        }
        else if (w instanceof Frame && title.equals(((Frame) w).getTitle())) {
          list.add(w);
        }
      }
    }
    return list;
  }

  protected JInternalFrame findInternalFrame(String title) {
    for (Window w : JWindow.getWindows()) {
      for (JInternalFrame i : SwingUtility.findChildComponents(w, JInternalFrame.class)) {
        if (i.isVisible()) {
          if (title.equals(i.getTitle())) {
            return i;
          }
        }
      }
    }
    return null;
  }

  //wait for window containing this button
  protected Component waitForContextMenu(final String text) {
    return waitUntil(new WaitCondition<Component>() {
      @Override
      public Component run() {
        return syncExec(new MockRunnable<Component>() {
          @Override
          public Component run() throws Throwable {
            String label = cleanButtonLabel(text);
            //find lightweight popup
            Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            Component popupContainer = null;
            if (popupContainer == null) {
              if (activeWindow instanceof RootPaneContainer) {
                Component[] a = ((RootPaneContainer) activeWindow).getLayeredPane().getComponentsInLayer(JLayeredPane.POPUP_LAYER);
                if (a.length > 0) {
                  popupContainer = a[0];
                }
              }
            }
            //find heavyweight popup
            if (popupContainer == null) {
              for (Window w : activeWindow.getOwnedWindows()) {
                if (w.getClass().getName().equals("javax.swing.Popup$HeavyWeightWindow")) {
                  if (w instanceof RootPaneContainer) {
                    popupContainer = ((RootPaneContainer) w).getContentPane();
                  }
                  else {
                    popupContainer = w;
                  }
                  break;
                }
              }
            }
            if (popupContainer != null) {
              for (AbstractButton b : SwingUtility.findChildComponents(popupContainer, AbstractButton.class)) {
                if (label.equals(b.getText())) {
                  return b;
                }
              }
            }
            return null;
          }
        });
      }
    });
  }

  protected JComponent waitForPushButtonWithLabel(final String label) {
    return waitUntil(new WaitCondition<JComponent>() {
      @Override
      public JComponent run() {
        return syncExec(new MockRunnable<JComponent>() {
          @Override
          public JComponent run() throws Throwable {
            for (Component parent : enumerateParentContainers()) {
              for (Component o : SwingUtility.findChildComponents(parent, Component.class)) {
                if (o instanceof AbstractButton) {
                  if (cleanButtonLabel(label).equals(cleanButtonLabel(((AbstractButton) o).getText()))) {
                    return (AbstractButton) o;
                  }
                }
              }
            }
            return null;
          }
        });
      }
    });
  }

  protected JComponent waitForIndexedField(final FieldType type, final int fieldIndex) {
    return waitUntil(new WaitCondition<JComponent>() {
      @Override
      public JComponent run() {
        return syncExec(new MockRunnable<JComponent>() {
          @Override
          public JComponent run() throws Throwable {
            for (Component parent : enumerateParentContainers()) {
              int index = 0;
              for (JComponent c : SwingUtility.findChildComponents(parent, JComponent.class)) {
                if (getFieldTypeOf(c) == type) {
                  if (index == fieldIndex) {
                    return c;
                  }
                  index++;
                }
              }
            }
            return null;
          }
        });
      }
    });
  }

  protected JComponent waitForScoutField(final String name) {
    return waitUntil(new WaitCondition<JComponent>() {
      @Override
      public JComponent run() {
        return syncExec(new MockRunnable<JComponent>() {
          @Override
          public JComponent run() throws Throwable {
            JComponent lastSecondaryCandidate = null;
            for (Component parent : enumerateParentContainers()) {
              for (JComponent c : SwingUtility.findChildComponents(parent, JComponent.class)) {
                String s = getScoutNameOf(c);
                if (s != null && ("." + s).endsWith("." + name)) {
                  lastSecondaryCandidate = c;
                  if (getFieldTypeOf(c) != null) {
                    //primary match
                    return c;
                  }
                }
              }
            }
            return lastSecondaryCandidate;
          }
        });
      }
    });
  }

  protected List<Component> enumerateParentContainers() {
    ArrayList<Component> parents = new ArrayList<Component>();
    //find leightweight popup
    Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    if (activeWindow instanceof RootPaneContainer) {
      Component[] a = ((RootPaneContainer) activeWindow).getLayeredPane().getComponentsInLayer(JLayeredPane.POPUP_LAYER);
      if (a.length > 0) {
        parents.add(a[0]);
      }
    }
    //find heavyweight popup
    for (Window w : activeWindow.getOwnedWindows()) {
      if (w.getClass().getName().equals("javax.swing.Popup$HeavyWeightWindow")) {
        if (w.isShowing()) {
          if (w instanceof RootPaneContainer) {
            parents.add(((RootPaneContainer) w).getContentPane());
          }
          else {
            parents.add(w);
          }
        }
        break;
      }
    }
    for (Window w : JWindow.getWindows()) {
      if (w.isShowing()) {
        if (w instanceof RootPaneContainer) {
          parents.add(((RootPaneContainer) w).getContentPane());
        }
        else {
          parents.add(w);
        }
      }
    }
    return parents;
  }

  protected FieldType getFieldTypeOf(Container c) {
    if (c == null) {
      return null;
    }
    if (!c.isShowing()) {
      return null;
    }

    if (c instanceof JComponent && ((JComponent) c).getVisibleRect().isEmpty()) {
      return null;
    }
    //
    if (c instanceof JLabel) {
      return FieldType.Label;
    }
    if (c instanceof JTextComponent) {
      return FieldType.Text;
    }
    if (c instanceof JCheckBox) {
      return FieldType.Checkbox;
    }
    if (c instanceof JRadioButton) {
      return FieldType.RadioButton;
    }
    if (c instanceof JTable) {
      return FieldType.Table;
    }
    if (c instanceof JTree) {
      return FieldType.Tree;
    }
    if (c instanceof AbstractButton) {
      if (SwingUtilities.getAncestorOfClass(JScrollBar.class, c) != null) {
        return FieldType.ScrollButton;
      }
      else {
        return FieldType.PushButton;
      }
    }
    return null;
  }

  protected String getScoutNameOf(Container c) {
    IPropertyObserver scoutObject = SwingScoutComposite.getScoutModelOnWidget(c);
    if (scoutObject != null) {
      return scoutObject.getClass().getName();
    }
    return null;
  }

  protected int toKeyCode(Key key) {
    switch (key) {
      case Shift:
        return KeyEvent.VK_SHIFT;
      case Control:
        return KeyEvent.VK_CONTROL;
      case Alt:
        return KeyEvent.VK_ALT;
      case Delete:
        return KeyEvent.VK_DELETE;
      case Backspace:
        return KeyEvent.VK_BACK_SPACE;
      case Enter:
        return KeyEvent.VK_ENTER;
      case Esc:
        return KeyEvent.VK_ESCAPE;
      case Tab:
        return KeyEvent.VK_TAB;
      case ContextMenu:
        return KeyEvent.VK_CONTEXT_MENU;
      case Up:
        return KeyEvent.VK_UP;
      case Down:
        return KeyEvent.VK_DOWN;
      case Left:
        return KeyEvent.VK_LEFT;
      case Right:
        return KeyEvent.VK_RIGHT;
      default:
        throw new IllegalArgumentException("Unknown keyboard key: " + key);
    }
  }

  protected <T> T syncExec(final MockRunnable<T> r) {
    final AtomicReference<T> ret = new AtomicReference<T>();
    if (!SwingUtilities.isEventDispatchThread()) {
      final AtomicReference<Throwable> ex = new AtomicReference<Throwable>();
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            try {
              ret.set(syncExec(r));
            }
            catch (Throwable t) {
              ex.set(t);
            }
          }
        });
        if (ex.get() != null) {
          throw ex.get();
        }
        return ret.get();
      }
      catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
    //
    try {
      return r.run();
    }
    catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  protected <T> T waitUntil(final WaitCondition<T> w) {
    try {
      return TestingUtility.waitUntil(WAIT_TIMEOUT, w);
    }
    catch (Throwable t) {
      throw new RuntimeException(t);
    }
    finally {
      waitForIdle();
    }
  }

  @Override
  public void clickLeftOnSmartFieldMagnifier(FieldState fieldState) {
    gotoPoint(fieldState.x + fieldState.width - 10, fieldState.y + 10);
    clickLeft();
  }

  @Override
  public void clickRightOnSmartFieldMagnifier(FieldState fieldState) {
    gotoPoint(fieldState.x + fieldState.width - 10, fieldState.y + 10);
    clickRight();
  }
}
