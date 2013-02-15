/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.svg.client.svgfield;

import java.net.URL;
import java.util.EventObject;

import org.apache.batik.dom.svg.SVGOMPoint;
import org.w3c.dom.svg.SVGPoint;

public class SvgFieldEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private final int m_type;
  private final float m_pointX;
  private final float m_pointY;
  private final URL m_url;

  /**
   * A hyperlink was activated
   */
  public static final int TYPE_HYPERLINK = 10;
  /**
   * A element was clicked
   */
  public static final int TYPE_CLICKED = 20;

  SvgFieldEvent(ISvgField source, int type, SVGPoint point, URL url) {
    super(source);
    m_type = type;
    m_pointX = point.getX();
    m_pointY = point.getY();
    m_url = url;
  }

  public int getType() {
    return m_type;
  }

  public ISvgField getSvgField() {
    return (ISvgField) getSource();
  }

  public SVGPoint getPoint() {
    return new SVGOMPoint(m_pointX, m_pointY);
  }

  public URL getURL() {
    return m_url;
  }
}
