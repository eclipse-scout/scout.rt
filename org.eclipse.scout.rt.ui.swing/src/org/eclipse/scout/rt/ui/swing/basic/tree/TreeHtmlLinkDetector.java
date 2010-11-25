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
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 *
 */
public class TreeHtmlLinkDetector {
  private TreePath m_treePath;
  private URL m_hyperlink;

  public boolean detect(JTree tree, Point p) {
    m_treePath = null;
    m_hyperlink = null;
    //
    TreePath path = tree.getPathForLocation(p.x, p.y);
    if (path != null && path.getPathCount() > 0) {
      try {
        TreeCellRenderer renderer = tree.getCellRenderer();
        Component c = renderer.getTreeCellRendererComponent(tree, path.getLastPathComponent(), tree.isPathSelected(path), tree.isExpanded(path), tree.getModel().isLeaf(path.getLastPathComponent()), tree.getRowForPath(path), true);
        if (c instanceof JComponent) {
          View v = (View) ((JComponent) c).getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
          if (v != null) {
            HTMLDocument doc = (HTMLDocument) v.getDocument();
            //v is the renderer, the first child is the html element
            v = v.getView(0);
            if (v != null && doc != null) {
              Rectangle r = tree.getPathBounds(path);
              Rectangle allocation = new Rectangle(0, 0, r.width, r.height);
              v.setSize(r.width, r.height);
              int pos = v.viewToModel(p.x - r.x, p.y - r.y, allocation, new Bias[1]);
              if (pos >= 0) {
                Element elem = doc.getCharacterElement(pos);
                if (elem != null) {
                  AttributeSet set = (AttributeSet) elem.getAttributes().getAttribute(HTML.Tag.A);
                  if (set != null) {
                    String s = (String) set.getAttribute(HTML.Attribute.HREF);
                    if (s != null) {
                      try {
                        m_hyperlink = new URL(s);
                        m_treePath = path;
                        return true;
                      }
                      catch (MalformedURLException mfue) {
                        m_hyperlink = new URL(new URL("http://local"), s);
                        m_treePath = path;
                        return true;
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      catch (Throwable t) {
        //nop
      }
    }
    return false;
  }

  /**
   * @return URL
   *         If the url is not a valid external url (for example a local url "test/abc") then the prefix http://local/
   *         is used.
   */
  public URL getHyperlink() {
    return m_hyperlink;
  }

  public TreePath getTreePath() {
    return m_treePath;
  }

}
