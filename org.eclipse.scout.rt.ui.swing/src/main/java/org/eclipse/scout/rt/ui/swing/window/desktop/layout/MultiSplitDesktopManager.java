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
package org.eclipse.scout.rt.ui.swing.window.desktop.layout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;

public class MultiSplitDesktopManager extends DefaultDesktopManager {
  private static final long serialVersionUID = 1L;
  private ArrayList<WeakReference<JInternalFrame>> m_activationHistory = new ArrayList<WeakReference<JInternalFrame>>();

  public MultiSplitDesktopManager() {
  }

  @Override
  public void iconifyFrame(JInternalFrame f) {
    boolean wasSelected = f.isSelected();
    // cache old size
    f.putClientProperty("sizeBeforeIconify", f.getSize());
    // set new size
    JDesktopPane pane = f.getDesktopPane();
    LayoutManager layout = pane.getLayout();
    if (layout instanceof MultiSplitLayout) {
      MultiSplitLayout pLayout = (MultiSplitLayout) layout;
      Dimension prefSize = f.getPreferredSize();
      Dimension d = new Dimension(f.getWidth(), prefSize.height);
      pLayout.getModel(pane).iconify(f, new Rectangle(f.getLocation(), d));
      reassignFrameSizes(pane);
    }
    if (wasSelected) {
      // activate next frame
      Action a = f.getDesktopPane().getActionMap().get("selectNextFrame");
      if (a != null) {
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
      }
    }
    f.getDesktopPane().revalidate();
  }

  @Override
  public void deiconifyFrame(JInternalFrame f) {
    JDesktopPane pane = f.getDesktopPane();
    LayoutManager layout = pane.getLayout();
    if (layout instanceof MultiSplitLayout) {
      MultiSplitLayout pLayout = (MultiSplitLayout) layout;
      // set to previous size, when null then use preferred size, (but at least
      // 64px)
      Dimension d = (Dimension) f.getClientProperty("sizeBeforeIconify");
      if (d != null) {
        Dimension curSize = f.getSize();
        Dimension minSize = f.getMinimumSize();
        Dimension maxSize = f.getMaximumSize();
        d.width = Math.max(minSize.width, Math.min(curSize.width, maxSize.width));
        d.height = Math.max(minSize.height, Math.min(d.height, maxSize.height));
      }
      else {
        Dimension prefSize = f.getPreferredSize();
        Dimension maxSize = f.getMaximumSize();
        d = new Dimension(f.getWidth(), Math.max(prefSize.height, Math.min(maxSize.height, 64)));
      }
      pLayout.getModel(pane).fitSize(f, d);
      reassignFrameSizes(pane);
    }
  }

  @Override
  public void activateFrame(JInternalFrame f) {
    super.activateFrame(f);
    addToActiveHistory(f);
  }

  @Override
  public void deactivateFrame(JInternalFrame f) {
    super.deactivateFrame(f);
  }

  @Override
  public void openFrame(JInternalFrame f) {
    super.openFrame(f);
    JDesktopPane pane = f.getDesktopPane();
    reassignFrameSizes(pane);
  }

  public void fitFrames(JInternalFrame[] frames) {
    if (frames != null && frames.length > 0) {
      JDesktopPane pane = frames[0].getDesktopPane();
      LayoutManager layout = pane.getLayout();
      if (layout instanceof MultiSplitLayout) {
        MultiSplitLayout pLayout = (MultiSplitLayout) layout;
        // set at least preferred size
        for (JInternalFrame f : frames) {
          Dimension cur = f.getSize();
          Dimension[] sizes = SwingLayoutUtility.getValidatedSizes(f);
          Dimension d = new Dimension(Math.max(sizes[0].width, Math.min(cur.width, sizes[2].width)), Math.max(sizes[0].height, Math.min(cur.height, sizes[2].height)));
          d.width = Math.max(d.width, sizes[1].width);
          d.height = Math.max(d.height, sizes[1].height);
          pLayout.getModel(pane).fitSize(f, d);
        }
        reassignFrameSizes(pane);
      }
    }
  }

  @Override
  public void closeFrame(JInternalFrame f) {
    JDesktopPane pane = f.getDesktopPane();
    super.closeFrame(f);
    if (pane != null) {
      pane.revalidate();
    }
    removeFromActiveHistory(f);
    JInternalFrame lastActiveFrame = getLastActivatedFrame();
    if (lastActiveFrame != null) {
      if (!lastActiveFrame.isSelected()) {
        try {
          lastActiveFrame.setSelected(true);
        }
        catch (PropertyVetoException e) {
          // ignore
        }
      }
    }
  }

  private JInternalFrame getLastActivatedFrame() {
    if (m_activationHistory.size() > 0) {
      return m_activationHistory.get(0).get();
    }
    else {
      return null;
    }
  }

  private void removeFromActiveHistory(JInternalFrame f) {
    for (Iterator<WeakReference<JInternalFrame>> it = m_activationHistory.iterator(); it.hasNext();) {
      WeakReference ref = it.next();
      if (ref.get() == f) {
        it.remove();
      }
      else if (ref.get() == null) {
        it.remove();
      }
    }
  }

  private void addToActiveHistory(JInternalFrame f) {
    removeFromActiveHistory(f);
    for (Iterator<WeakReference<JInternalFrame>> it = m_activationHistory.iterator(); it.hasNext();) {
      WeakReference ref = it.next();
      if (ref.get() == f) {
        it.remove();
      }
      else if (ref.get() == null) {
        it.remove();
      }
    }
    if (f != null) {
      m_activationHistory.add(0, new WeakReference<JInternalFrame>(f));
    }
  }

  @Override
  public void minimizeFrame(JInternalFrame f) {
    super.minimizeFrame(f);
    f.getDesktopPane().revalidate();
  }

  /**
   * Drag
   */
  @Override
  public void beginDraggingFrame(JComponent f) {
    // nop
  }

  @Override
  public void dragFrame(JComponent f, int newX, int newY) {
    // nop
  }

  @Override
  public void endDraggingFrame(JComponent f) {
    // nop
  }

  /**
   * Resize
   */
  @Override
  public void beginResizingFrame(JComponent f, int direction) {
    super.beginResizingFrame(f, direction);
  }

  @Override
  public void resizeFrame(JComponent f, int x, int y, int w, int h) {
    JDesktopPane pane = getDesktopPane2(f);
    LayoutManager layout = pane.getLayout();
    if (layout instanceof MultiSplitLayout) {
      MultiSplitLayout pLayout = (MultiSplitLayout) layout;
      pLayout.getModel(pane).resize((JInternalFrame) f, new Rectangle(x, y, w, h));
      reassignFrameSizes(pane);
    }
  }

  @Override
  public void endResizingFrame(JComponent f) {
    super.endResizingFrame(f);
  }

  /**
   * Position, Dimension
   */
  protected void reassignFrameSizes(JDesktopPane pane) {
    pane.getRootPane().revalidate();
    pane.doLayout();
  }

  @Override
  public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
    super.setBoundsForFrame(f, newX, newY, newWidth, newHeight);
  }

  private JDesktopPane getDesktopPane2(JComponent frame) {
    JDesktopPane pane = null;
    Component c = frame.getParent();
    while (pane == null) {
      if (c instanceof JDesktopPane) {
        pane = (JDesktopPane) c;
      }
      else if (c == null) {
        break;
      }
      else {
        c = c.getParent();
      }
    }
    return pane;
  }

}
