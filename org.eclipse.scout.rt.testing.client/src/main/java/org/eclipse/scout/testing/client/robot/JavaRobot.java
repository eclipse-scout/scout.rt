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
package org.eclipse.scout.testing.client.robot;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.eclipse.scout.testing.client.IGuiMock;

/**
 * Wrapper around {@link Robot} with high level api.
 */
public class JavaRobot {
  private Robot m_robot;
  private int m_x;
  private int m_y;

  public JavaRobot() {
    try {
      m_robot = new Robot();
      m_robot.setAutoDelay(10);
      m_robot.setAutoWaitForIdle(false);
    }
    catch (AWTException e) {
      throw new RuntimeException(e);
    }
  }

  public int getAutoDelay() {
    return m_robot.getAutoDelay();
  }

  public void setAutoDelay(int ms) {
    m_robot.setAutoDelay(ms);
  }

  public void sleep(int ms) {
    try {
      Thread.sleep(ms);
    }
    catch (InterruptedException ite) {
      ite.printStackTrace();
    }
  }

  public void moveTo(int x, int y) {
    this.m_x = x;
    this.m_y = y;
    m_robot.mouseMove(x, y);
  }

  public void moveDelta(int dx, int dy) {
    this.m_x += dx;
    this.m_y += dy;
    m_robot.mouseMove(m_x, m_y);
  }

  public void pressLeft() {
    m_robot.mousePress(MouseEvent.BUTTON1_MASK);
  }

  public void releaseLeft() {
    m_robot.mouseRelease(MouseEvent.BUTTON1_MASK);
  }

  public void clickLeft() {
    m_robot.mousePress(MouseEvent.BUTTON1_MASK);
    m_robot.mouseRelease(MouseEvent.BUTTON1_MASK);
  }

  public void pressRight() {
    m_robot.mousePress(MouseEvent.BUTTON3_MASK);
  }

  public void releaseRight() {
    m_robot.mouseRelease(MouseEvent.BUTTON3_MASK);
  }

  public void clickRight() {
    m_robot.mousePress(MouseEvent.BUTTON3_MASK);
    m_robot.mouseRelease(MouseEvent.BUTTON3_MASK);
  }

  public void pressKey(IGuiMock.Key key) {
    m_robot.keyPress(toKeyCode(key));
  }

  public void releaseKey(IGuiMock.Key key) {
    m_robot.keyRelease(toKeyCode(key));
  }

  public void typeKey(IGuiMock.Key key) {
    m_robot.keyPress(toKeyCode(key));
    m_robot.keyRelease(toKeyCode(key));
  }

  public void typeText(String s) {
    if (s == null || s.length() == 0) {
      return;
    }
    for (char c : s.toCharArray()) {
      boolean shift = Character.isUpperCase(c);
      char upperChar = Character.toUpperCase(c);
      if ((upperChar == ' ') || (upperChar >= 'A' && upperChar <= 'Z') || (upperChar >= '0' && upperChar <= '9')) {
        try {
          if (shift) {
            m_robot.keyPress(KeyEvent.VK_SHIFT);
          }
          //
          m_robot.keyPress((int) upperChar);
          m_robot.keyRelease((int) upperChar);
        }
        finally {
          if (shift) {
            m_robot.keyRelease(KeyEvent.VK_SHIFT);
          }
        }
      }
      else {
        pressSpecialKey(c);
      }
    }
  }

  private void pressSpecialKey(char ch) {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable oldContent = clipboard.getContents(null);
    try {
      StringSelection selection = new StringSelection("" + ch);
      clipboard.setContents(selection, null);
      try {
        //press paste (ctrl-V)
        m_robot.keyPress(KeyEvent.VK_CONTROL);
        m_robot.keyPress(KeyEvent.VK_V);
        m_robot.keyRelease(KeyEvent.VK_V);
        m_robot.keyRelease(KeyEvent.VK_CONTROL);
      }
      catch (Throwable ex) {
        //alternatively press Paste key
        m_robot.keyPress(KeyEvent.VK_PASTE);
        m_robot.keyRelease(KeyEvent.VK_PASTE);
      }
    }
    finally {
      clipboard.setContents(oldContent, null);
    }
  }

  protected int toKeyCode(IGuiMock.Key key) {
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
      case Space:
        return KeyEvent.VK_SPACE;
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
      case Windows:
        return KeyEvent.VK_WINDOWS;
      case F1:
        return KeyEvent.VK_F1;
      case F2:
        return KeyEvent.VK_F2;
      case F3:
        return KeyEvent.VK_F3;
      case F4:
        return KeyEvent.VK_F4;
      case F5:
        return KeyEvent.VK_F5;
      case F6:
        return KeyEvent.VK_F6;
      case F7:
        return KeyEvent.VK_F7;
      case F8:
        return KeyEvent.VK_F8;
      case F9:
        return KeyEvent.VK_F9;
      case F10:
        return KeyEvent.VK_F10;
      case F11:
        return KeyEvent.VK_F11;
      case F12:
        return KeyEvent.VK_F12;
      case Home:
        return KeyEvent.VK_HOME;
      case End:
        return KeyEvent.VK_END;
      case PageUp:
        return KeyEvent.VK_PAGE_UP;
      case PageDown:
        return KeyEvent.VK_PAGE_DOWN;
      case NumPad0:
        return KeyEvent.VK_NUMPAD0;
      case NumPad1:
        return KeyEvent.VK_NUMPAD1;
      case NumPad2:
        return KeyEvent.VK_NUMPAD2;
      case NumPad3:
        return KeyEvent.VK_NUMPAD3;
      case NumPad4:
        return KeyEvent.VK_NUMPAD4;
      case NumPad5:
        return KeyEvent.VK_NUMPAD5;
      case NumPad6:
        return KeyEvent.VK_NUMPAD6;
      case NumPad7:
        return KeyEvent.VK_NUMPAD7;
      case NumPad8:
        return KeyEvent.VK_NUMPAD8;
      case NumPadMultiply:
        return KeyEvent.VK_MULTIPLY;
      case NumPadDivide:
        return KeyEvent.VK_DIVIDE;
      case NumPadAdd:
        return KeyEvent.VK_ADD;
      case NumPadSubtract:
        return KeyEvent.VK_SUBTRACT;
      case NumPadDecimal:
        return KeyEvent.VK_DECIMAL;
      case NumPadSeparator:
        return KeyEvent.VK_SEPARATOR;
      default:
        throw new IllegalArgumentException("Unknown keyboard key: " + key);
    }
  }
}
