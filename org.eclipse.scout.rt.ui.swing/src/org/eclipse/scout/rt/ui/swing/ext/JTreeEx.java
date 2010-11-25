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

import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.tree.TreeSelectionModel;

import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.dnd.DefaultDropTarget;
import org.eclipse.scout.rt.ui.swing.dnd.TransferHandlerEx;

/**
 * Various enhancements and fixes to JTree
 * - DnD handling using {@link TransferHandlerEx} and {@link DefaultDropTarget} - Support for
 * getPreferredContentSize
 * - Support for setPreferredScrollableViewportSize
 * - fixed setTooltipText that constantly removed tooltip manager registration
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

}
