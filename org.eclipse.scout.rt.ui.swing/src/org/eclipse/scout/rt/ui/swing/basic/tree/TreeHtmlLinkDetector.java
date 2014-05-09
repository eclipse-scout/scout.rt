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
package org.eclipse.scout.rt.ui.swing.basic.tree;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.eclipse.scout.rt.ui.swing.basic.AbstractHtmlLinkDetector;

/**
 * Detects a hyperlink in a JTree
 */
public class TreeHtmlLinkDetector extends AbstractHtmlLinkDetector<JTree> {
  private TreePath m_treePath;

  @Override
  public boolean detect(JTree container, Point p) {
    boolean found = super.detect(container, p);
    if (found) {
      m_treePath = getPath(p);
    }
    return found;
  }

  public TreePath getTreePath() {
    return m_treePath;
  }

  @Override
  protected Rectangle getCellRectangle(Point p) {
    return getContainer().getPathBounds(getPath(p));
  }

  @Override
  protected Component getComponent(Point p) {
    JTree tree = getContainer();
    TreePath path = getPath(p);
    TreeCellRenderer renderer = getContainer().getCellRenderer();
    Component c = renderer.getTreeCellRendererComponent(tree, path.getLastPathComponent(), tree.isPathSelected(path), tree.isExpanded(path), tree.getModel().isLeaf(path.getLastPathComponent()), tree.getRowForPath(path), true);
    return c;
  }

  private TreePath getPath(Point p) {
    return getContainer().getPathForLocation(p.x, p.y);
  }

}
