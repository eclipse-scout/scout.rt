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
package org.eclipse.scout.testing.client;

import java.util.List;
import java.util.Set;

/**
 * This interface is used to support gui testing with an abstraction layer.
 * <p>
 * Therefore swt and swing gui tests can be programmed just once in the client and run with both guis.
 * <p>
 * The term "window" is used for frames, dialogs, swt views, swt editors, swing internal frames.
 * <p>
 * The config.ini property "IGuiMock.treeNodeToExpandIconGap" defines the gap between a tree node and its
 * expand/collapse icon and is used in {@link #gotoTreeExpandIcon(int, String)}
 */
public interface IGuiMock {
  long WAIT_TIMEOUT = 10000L;

  enum GuiStrategy {
    Swt,
    Swing,
    Vaadin,
    Rap
  }

  enum Key {
    Shift,
    Control,
    Alt,
    Delete,
    Backspace,
    Enter,
    Esc,
    Tab,
    ContextMenu,
    Up,
    Down,
    Left,
    Right,
    Windows,
    F1,
    F2,
    F3,
    F4,
    F5,
    F6,
    F7,
    F8,
    F9,
    F10,
    F11,
    F12,
    Home,
    End,
    PageUp,
    PageDown,
    NumPad0,
    NumPad1,
    NumPad2,
    NumPad3,
    NumPad4,
    NumPad5,
    NumPad6,
    NumPad7,
    NumPad8,
    NumPad9,
    NumPadMultiply,
    NumPadAdd,
    NumPadSubtract,
    NumPadDecimal,
    NumPadDivide,
    NumPadSeparator,
  }

  enum FieldType {
    Label,
    Text,
    Checkbox,
    RadioButton,
    Table,
    Tree,
    PushButton,
    ScrollButton,
    DropdownButton,
  }

  class WindowState {
    /**
     * coordinates on screen
     */
    public int x;
    /**
     * coordinates on screen
     */
    public int y;
    public int width;
    public int height;
  }

  class FieldState {
    public FieldType type;
    public String scoutName;
    /**
     * coordinates on screen
     */
    public int x;
    /**
     * coordinates on screen
     */
    public int y;
    public int width;
    public int height;
    public boolean focus;
    public String text;
  }

  void initializeMock();

  void shutdownMock();

  void beforeTest();

  void afterTest();

  GuiStrategy getStrategy();

  int getSleepDelay();

  void setSleepDelay(int ms);

  /**
   * sleep for some time
   */
  void sleep();

  /**
   * sleep for some time
   */
  void sleep(int ms);

  boolean isWindowActive(String title);

  boolean isWindowOpen(String title);

  /**
   * Waits until gui and model are idle
   */
  void waitForIdle();

  /**
   * This only waits for the window to open, NOT to be active.
   * <p>
   * Normally {@link #waitForActiveWindow(String)} is used.
   */
  void waitForOpenWindow(String title);

  void waitForActiveWindow(String title);

  void activateWindow(String title);

  /**
   * @return the field state of the visible and accessible field at the index
   */
  FieldState getFieldState(FieldType type, int index);

  /**
   * @return the field state of the visible and accessible field that represents a scout model with that (class) name
   */
  FieldState getScoutFieldState(String name);

  FieldState getScoutFieldContainerState(String name);

  /**
   * @return all fields af that type (all fileds if type is null)
   */
  List<FieldState> getFieldStates(FieldType type);

  /**
   * @return the state of the current focused /clicked field (textfield, button)
   */
  FieldState getFocusFieldState();

  /**
   * Set mouse to absolute point on screen
   */
  void gotoPoint(int x, int y);

  /**
   * Move mouse from current location to a delta position on screen
   */
  void move(int deltaX, int deltaY);

  /**
   * Click left mouse button
   */
  void clickLeft();

  /**
   * Click right mouse button
   */
  void clickRight();

  /**
   * Press left mouse button (without releasing it)
   */
  void pressLeft();

  /**
   * Release left mouse button (must be called after {@link IGuiMock#pressLeft()}
   */
  void releaseLeft();

  /**
   * Drag the mouse
   */
  void drag(int x1, int y1, int x2, int y2);

  void dragWindowRightBorder(WindowState windowState, int pixelToMoveOnX);

  void pressKey(Key key);

  void releaseKey(Key key);

  void typeKey(Key key);

  void typeText(final String text);

  void paste(final String text);

  /**
   * Convenience for clicking on a button with a label
   */
  void clickOnPushButton(String text);

  /**
   * place the mouse over the center of a field
   * <p>
   * The index is searched in the active window (popup, dialog, frame)
   */
  void gotoField(FieldType type, int index);

  /**
   * place the mouse over the center of a field that represents a scout field.
   * This correspond to {@link #gotoScoutField(String, float, float)} with x=0.5 and y=0.5
   * 
   * @param name
   *          (class) name of the scout field (scout client model)
   */
  void gotoScoutField(String name);

  /**
   * place the mouse over the field that represents a scout field at the position described by the x and y parameter
   * 
   * @param name
   *          (class) name of the scout field (scout client model)
   * @param x
   *          position of the cursor in percent on the horizontal axis (set to 0.5 to be in the center)
   * @param y
   *          position of the cursor in percent on the vertical axis (set to 0.5 to be in the center)
   */
  void gotoScoutField(String name, double x, double y);

  /**
   * place the mouse over the center of a table cell
   * <p>
   * The index is searched in the active window (popup, dialog, frame)
   */
  void gotoTable(int tableIndex, int rowIndex, int columnIndex);

  /**
   * place the mouse over the center of a table header cell
   * <p>
   * The index is searched in the active window (popup, dialog, frame)
   */
  void gotoTableHeader(int tableIndex, int columnIndex);

  /**
   * place the mouse over the center of a tree node
   * <p>
   * The index is searched in the active window (popup, dialog, frame)
   */
  void gotoTree(int treeIndex, String nodeText);

  /**
   * place the mouse over the expand/collapse area of a tree node
   * <p>
   * The index is searched in the active window (popup, dialog, frame)
   */
  void gotoTreeExpandIcon(int treeIndex, String nodeText);

  /**
   * Assumes that a context menu is showing and clicks on the popup menu with the name
   * <p>
   * If multiple names are given this is meant as sub-menus
   */
  void contextMenu(String... names);

  /**
   * @return the list of cells in the original order
   */
  List<String> getTableCells(int tableIndex, int columnIndex);

  /**
   * @return the list of nodes in the original order
   */
  List<String> getTreeNodes(int treeIndex);

  /**
   * @return the sorted set of texts of selected cells
   */
  Set<String> getSelectedTableCells(int tableIndex, int columnIndex);

  /**
   * @return the sorted set of texts of selected nodes
   */
  Set<String> getSelectedTreeNodes(int treeIndex);

  /**
   * Works only if checked Row is visible
   * 
   * @return the sorted set of texts of checked cells
   */
  Set<String> getCheckedTableCells(int tableIndex, int columnIndex);

  /**
   * @return the sorted set of texts of checked nodes
   */
//  Set<String> getCheckedTreeNodes(int treeIndex);XXX

  /**
   */
  WindowState getWindowState(String title);

  String getClipboardText();

  Object internal0(Object o);

  /**
   * Click left on magnifier of a smartfield
   */
  void clickLeftOnSmartFieldMagnifier(FieldState fieldState);

  /**
   * Click right on magnifier of a smartfield
   */
  void clickRightOnSmartFieldMagnifier(FieldState fieldState);
}
