package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.ext.DropDownButtonIcon.MouseOver;

/**
 * How to have an icon inside a textfield:
 * 
 * @see: http://www.coderanch.com/t/343579/Swing-AWT-SWT-JFace/java/Inserting-Image-JTextField
 * @author awe
 */
public class JTextFieldWithDropDownButton extends JTextFieldEx {

  private static final long serialVersionUID = 1L;

  enum Region {
    TEXTAREA,
    ICON,
    MENU
  }

  private DropDownButtonIcon m_dropDownButton;
  private Region m_cursorOverRegion = Region.TEXTAREA;
  private Cursor m_defaultCursor;
  private int m_insetsRight = 0;
  private int m_originalMarginRight = -1;
  private boolean m_dropDownButtonVisible;

  private Collection<IDropDownButtonListener> m_listeners = new ArrayList<IDropDownButtonListener>();

  public JTextFieldWithDropDownButton(ISwingEnvironment env) {
    registerMouseMotionListener();
    setTextFieldMargin();
    m_defaultCursor = getCursor();
    m_dropDownButton = new DropDownButtonIcon(env);
    m_dropDownButtonVisible = true;
  }

  public void setIconGroup(IconGroup iconGroup) {
    m_dropDownButton.setIconGroup(iconGroup);
    setTextFieldMargin();
  }

  private void registerMouseMotionListener() {
    addMouseListener(new MouseAdapter() {
      MouseClickedBugFix fix;

      @Override
      public void mouseExited(MouseEvent e) {
        showNormalIcon();
      }

      @Override
      public void mousePressed(MouseEvent e) {
        fix = new MouseClickedBugFix(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (fix != null) {
          fix.mouseReleased(this, e);
        }
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if (fix != null && fix.mouseClicked()) {
          return;
        }
        Region r = getRegionTouchedByCursor(e.getPoint());
        if (r == Region.TEXTAREA) {
          return;
        }

        // right click always means "show menu"
        // left click only means "show menu" when cursor hovers over the arrow of the icon
        boolean clickedMenu = false;
        if (e.getButton() == MouseEvent.BUTTON3) {
          clickedMenu = true;
        }
        else if (r == Region.MENU) {
          clickedMenu = true;
        }
        if (clickedMenu) {
          for (IDropDownButtonListener l : m_listeners) {
            l.menuClicked(e.getSource());
          }
        }
        else {
          for (IDropDownButtonListener l : m_listeners) {
            l.iconClicked(e.getSource());
          }
        }
      }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        Region r = getRegionTouchedByCursor(e.getPoint());
        switch (r) {
          case TEXTAREA:
            showNormalIcon();
            break;
          case ICON:
          case MENU:
            highlightIconOrMenu(r);
            break;
        }
      }
    });
  }

  protected void setCursorOverRegion(Region cursorOverRegion) {
    Region oldRegion = this.m_cursorOverRegion;
    this.m_cursorOverRegion = cursorOverRegion;
    highlightIconPart();
    if (oldRegion != cursorOverRegion) {
      this.repaint();
    }
  }

  private void highlightIconPart() {
    switch (m_cursorOverRegion) {
      case TEXTAREA:
        m_dropDownButton.setMouseOver(MouseOver.NONE);
        break;
      case MENU:
        m_dropDownButton.setMouseOver(MouseOver.ARROW);
        break;
      case ICON:
        m_dropDownButton.setMouseOver(MouseOver.ICON);
        break;
    }
  }

  protected void showNormalIcon() {
    setCursor(m_defaultCursor);
    setCursorOverRegion(Region.TEXTAREA);
  }

  private void highlightIconOrMenu(Region region) {
    Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    if (region == Region.ICON && !m_dropDownButton.isIconEnabled()) {
      cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }
    else if (region == Region.MENU && !m_dropDownButton.isArrowEnabled()) {
      cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }
    setCursor(cursor);
    setCursorOverRegion(region);
  }

  protected Region getRegionTouchedByCursor(Point cursorPosition) {
    if (!isDropDownButtonVisible()) {
      return Region.TEXTAREA;
    }
    int menuSize = 7;
    if (cursorPosition.x >= getWidth() - menuSize - m_insetsRight &&
        cursorPosition.y <= getY() + menuSize) {
      return Region.MENU;
    }
    if (cursorPosition.x >= getWidth() - m_dropDownButton.getIconWidth() - m_insetsRight) {
      return Region.ICON;
    }
    return Region.TEXTAREA;
  }

  /**
   * This method may be called multiple times.
   */
  private void setTextFieldMargin() {
    Insets marginAndBorderInsets = getInsets();
    Insets marginInsets = getMargin();
    if (m_originalMarginRight == -1) {
      m_originalMarginRight = marginInsets.right;
    }
    m_insetsRight = marginAndBorderInsets.right - marginInsets.right;
    int iconWidth = 0;
    if (m_dropDownButton != null && isDropDownButtonVisible()) {
      iconWidth = m_dropDownButton.getIconWidth();
    }
    setMargin(new Insets(0, 0, 0, m_originalMarginRight + iconWidth));
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    paintIcon(g);
  }

  private void paintIcon(Graphics g) {
    if (!isDropDownButtonVisible()) {
      return;
    }
    if (m_dropDownButton != null) {
      int x = getWidth() - m_dropDownButton.getIconWidth() - 6/*- m_insetsRight*/;
      int y = (getHeight() - m_dropDownButton.getIconHeight()) / 2;
      m_dropDownButton.paintIcon(this, g, x, y);
    }
  }

  public void addDropDownButtonListener(IDropDownButtonListener l) {
    m_listeners.add(l);
  }

  public void removeDropDownButtonListener(IDropDownButtonListener l) {
    m_listeners.remove(l);
  }

  public boolean isDropDownButtonEnabled() {
    return m_dropDownButton.isIconEnabled();
  }

  public void setDropDownButtonEnabled(boolean iconEnabled) {
    m_dropDownButton.setIconEnabled(iconEnabled);
  }

  public boolean isDropDownButtonVisible() {
    return m_dropDownButtonVisible;
  }

  public void setDropDownButtonVisible(boolean iconVisible) {
    m_dropDownButtonVisible = iconVisible;
    setTextFieldMargin();
  }

  public void setMenuEnabled(boolean menuEnabled) {
    m_dropDownButton.setArrowEnabled(menuEnabled);
  }

}
