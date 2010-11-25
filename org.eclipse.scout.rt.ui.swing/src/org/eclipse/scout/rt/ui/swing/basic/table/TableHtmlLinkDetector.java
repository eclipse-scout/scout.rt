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
package org.eclipse.scout.rt.ui.swing.basic.table;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

/**
 *
 */
public class TableHtmlLinkDetector {
  private int m_rowIndex;
  private int m_columnIndex;
  private URL m_hyperlink;

  public boolean detect(JTable table, Point p) {
    m_rowIndex = -1;
    m_columnIndex = -1;
    m_hyperlink = null;
    //
    int row = table.rowAtPoint(p);
    int col = table.columnAtPoint(p);
    if (row >= 0 && col >= 0) {
      try {
        Component c = table.prepareRenderer(table.getCellRenderer(row, col), row, col);
        if (c instanceof JComponent) {
          View v = (View) ((JComponent) c).getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
          if (v != null) {
            HTMLDocument doc = (HTMLDocument) v.getDocument();
            //v is the renderer, the first child is the html element
            v = v.getView(0);
            if (v != null && doc != null) {
              Rectangle r = table.getCellRect(row, col, false);
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
                        m_rowIndex = row;
                        m_columnIndex = col;
                        return true;
                      }
                      catch (MalformedURLException mfue) {
                        m_hyperlink = new URL(new URL("http://local"), s);
                        m_rowIndex = row;
                        m_columnIndex = col;
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

  public int getRowIndex() {
    return m_rowIndex;
  }

  public int getColumnIndex() {
    return m_columnIndex;
  }
}
