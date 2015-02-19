/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.basic;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Abstract class to detect a hyperlink in a JComponent (e.g. JTable or JTree)
 *
 * @since 4.0-RC1
 */
public abstract class AbstractHtmlLinkDetector<T extends JComponent> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractHtmlLinkDetector.class);

  private URL m_hyperlink;
  private T m_container;

  /**
   * Detects a hyperlink in the container (e.g. JTree oder JTable) for the give mouse position
   *
   * @return <code>true</code> if hyperlink detected, <code>false</code> otherwise
   */
  public boolean detect(T container, Point p) {
    m_hyperlink = null;
    m_container = container;

    try {
      Component c = getComponent(p);
      if (c instanceof JComponent) {
        View v = (View) ((JComponent) c).getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
        if (v != null) {
          HTMLDocument doc = (HTMLDocument) v.getDocument();
          //v is the renderer, the first child is the html element
          v = v.getView(0);
          if (v != null && doc != null) {
            Rectangle cellRectangle = getCellRectangle(p);
            Rectangle contentSize = calculateContentSize((JComponent) c, cellRectangle);

            Point relativeMousePosition = new Point(p.x - cellRectangle.x, p.y - cellRectangle.y);
            if (!contentSize.contains(relativeMousePosition)) {
              return false; //mouse has not entered the contentSize rectangle
            }

            v.setSize(contentSize.width, contentSize.height);
            int pos = v.viewToModel(relativeMousePosition.x, relativeMousePosition.y, contentSize, new Bias[1]);
            if (pos >= 0) {
              Element elem = doc.getCharacterElement(pos);
              if (elem != null) {
                AttributeSet set = (AttributeSet) elem.getAttributes().getAttribute(HTML.Tag.A);
                if (set != null) {
                  String s = (String) set.getAttribute(HTML.Attribute.HREF);
                  if (s != null) {
                    try {
                      m_hyperlink = new URL(s);
                      return true;
                    }
                    catch (MalformedURLException mfue) {
                      m_hyperlink = new URL(new URL("http://local"), s);
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
      LOG.error(t.getMessage(), t);
    }

    return false;
  }

  /**
   * returns the cell's rectangle for the given position
   *
   * @since 4.0-RC1
   */
  protected abstract Rectangle getCellRectangle(Point p);

  /**
   * returns the component inside the container for the given position
   *
   * @since 4.0-RC1
   */
  protected abstract Component getComponent(Point p);

  /**
   * Calculates the area which can really be used inside the cellRectangle
   *
   * @since 4.0-RC1
   */
  protected Rectangle calculateContentSize(JComponent c, Rectangle cellRectangle) {
    Insets cInsets = calculateInsets(c);
    Rectangle contentSize = new Rectangle(0, 0, cellRectangle.width - (cInsets.left + cInsets.right), cellRectangle.height - (cInsets.top + cInsets.bottom));
    contentSize.x += cInsets.left;
    contentSize.y += cInsets.top;
    return contentSize;
  }

  /**
   * Calculates the insets of the component.
   *
   * @since 4.0-RC1
   */
  protected Insets calculateInsets(JComponent c) {
    Insets i = c.getInsets();
    if (c instanceof JLabel) {
      JLabel label = (JLabel) c;
      if (label.getIcon() != null) {
        i.left += label.getIcon().getIconWidth() + label.getIconTextGap();
      }
    }
    return i;
  }

  /**
   * @return URL
   *         If the url is not a valid external url (for example a local url "test/abc") then the prefix http://local/
   *         is used.
   */
  public URL getHyperlink() {
    return m_hyperlink;
  }

  /**
   * returns the container (e.g. JTree or JTable)
   */
  public T getContainer() {
    return m_container;
  }
}
