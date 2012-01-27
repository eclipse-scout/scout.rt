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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.dnd.DefaultDropTarget;
import org.eclipse.scout.rt.ui.swing.dnd.TransferHandlerEx;

/**
 * Various enhancements and fixes to JTree
 * <ul>
 * <li>DnD handling using {@link TransferHandlerEx} and {@link DefaultDropTarget}</li>
 * <li>Support for getPreferredContentSize</li>
 * <li>Support for setPreferredScrollableViewportSize</li>
 * <li>fixed setTooltipText that constantly removed tooltip manager registration</li>
 * </ul>
 */
public class JTreeEx extends JTree {
  private static final long serialVersionUID = 1L;

  private Dimension m_preferredScrollableViewportSize;

  public JTreeEx() {
    // focus corrections
    SwingUtility.installDefaultFocusHandling(this);
    setFocusCycleRoot(false);
    //
    setVerifyInputWhenFocusTarget(true);
    //
    setModel(null);
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setRootVisible(true);
    // nice: skin needs to improve this to make decent insets
    setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    // register tree for tooltips
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  //Performance optimization. See: http://hameister.org/JavaSwingJTreePerformance.html
  //the plaf library calls this method to update the expanded state of a path.
  //However, the implementation in JTree is very slow, and there is probably no use
  //for this functionality in Scout.
  @Override
  public Enumeration<TreePath> getExpandedDescendants(final TreePath path) {
    return null;
  }

  /**
   * workaround for bug in swing when using custom tooltips
   */
  @Override
  public void setToolTipText(String text) {
    super.setToolTipText(text);
    if (text == null) {
      // re-register
      ToolTipManager.sharedInstance().registerComponent(this);
    }
  }

  /**
   * bug in swing: tooltip flickers (show, hide, show, hide,...) when shown at exact mouse position.
   * <p>
   * Shift tooltip 4px right, 16px down
   */
  @Override
  public Point getToolTipLocation(MouseEvent event) {
    if (getToolTipText(event) != null) {
      return new Point(event.getX() + 4, event.getY() + 16);
    }
    else {
      return null;
    }
  }

  // WORKAROUND for background color when disabled
  // nice: add to look and feel
  @Override
  public Color getBackground() {
    if (!isEnabled()) {
      Color bg = UIManager.getColor("control");
      if (bg != null) {
        return bg;
      }
    }
    return super.getBackground();
  }

  @Override
  public void setName(String name) {
    super.setName(name);
    firePropertyChange("name", null, name);
  }

  @Override
  public void setTransferHandler(TransferHandler newHandler) {
    TransferHandler oldHandler = (TransferHandler) getClientProperty("TransferHandler");
    putClientProperty("TransferHandler", newHandler);
    DropTarget dropHandler = getDropTarget();
    if ((dropHandler == null) || (dropHandler instanceof UIResource)) {
      if (newHandler == null) {
        setDropTarget(null);
      }
      else if (!GraphicsEnvironment.isHeadless()) {
        setDropTarget(new DefaultDropTarget(this));
      }
    }
    firePropertyChange("transferHandler", oldHandler, newHandler);
  }

  public Dimension getPreferredContentSize(int maxRowCount) {
    Rectangle max = new Rectangle();
    for (int r = 0, nr = getRowCount(); r < nr && r < maxRowCount; r++) {
      max = max.union(getRowBounds(r));
    }
    return new Dimension(max.x + max.width, max.y + max.height);
  }

  @Override
  public TransferHandler getTransferHandler() {
    return (TransferHandler) getClientProperty("TransferHandler");
  }

  @Override
  public Dimension getPreferredScrollableViewportSize() {
    if (m_preferredScrollableViewportSize != null) {
      return m_preferredScrollableViewportSize;
    }
    else {
      return super.getPreferredScrollableViewportSize();
    }
  }

  public void setPreferredScrollableViewportSize(Dimension d) {
    m_preferredScrollableViewportSize = d;
  }

  /**
   * Custom implementation that tries to prevent unnecessary horizontal
   * scrolling of the tree. It's a copy of the default implementation,
   * with the only difference that it limits the width of the visible
   * rect of the target node to <i>max(30 Pixels, 25% of total width)</i>.
   */
  @Override
  public void scrollPathToVisible(TreePath treePath) {
    if (treePath != null) {
      makeVisible(treePath);
      Rectangle pathBounds = getPathBounds(treePath);
      if (pathBounds != null) {
        // <NoHorizontalScrollPatch>
        pathBounds.width = Math.max(30, (int) (0.25 * pathBounds.width));
        // </NoHorizontalScrollPatch>
        scrollRectToVisible(pathBounds);
        if (accessibleContext != null) {
          ((AccessibleJTree) accessibleContext).fireVisibleDataPropertyChange();
        }
      }
    }
  }
}
