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
package org.eclipse.scout.rt.shared.data.form.fields.svgfield;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

/**
 * A closed polygon
 * <p>
 * The xml representation is
 * 
 * <pre>
 * <xmp>
 *  <polygon id="" x="" y="" color="argb" border-color="argb">
 *   <point x="0" y="0"/>
 *   <point x="0" y="0"/>
 *   <point x="0" y="0"/>
 *   <point x="0" y="0"/>
 *  </polygon>
 * </xmp>
 * </pre>
 * 
 * The inner point coordinates of the polygon are relative to the polygon coordinates.
 */
public class PolygonElement extends AbstractGraphicsElement {
  private static final long serialVersionUID = 1L;

  public double x;
  public double y;
  private List<PointElement> m_points;

  public PolygonElement() {
    m_points = new ArrayList<PointElement>();
  }

  /**
   * @return the life list of all points. Changes to this list are directly changed in the owner model.
   */
  public List<PointElement> getPoints() {
    return m_points;
  }

  /**
   * Set the life list of all points.
   */
  public void setPoints(List<PointElement> points) {
    if (points == null) {
      points = new ArrayList<PointElement>();
    }
    m_points = points;
  }

  public void addPoint(PointElement p) {
    m_points.add(p);
  }

  @Override
  public void read(SimpleXmlElement node) throws ProcessingException {
    super.read(node);
    x = node.getDoubleAttribute("x");
    y = node.getDoubleAttribute("y");
    for (SimpleXmlElement child : node.getChildren("point")) {
      PointElement p = new PointElement();
      p.read(child);
      addPoint(p);
    }
  }

  @Override
  public void write(SimpleXmlElement node) throws ProcessingException {
    super.write(node);
    node.setName("polygon");
    node.setAttribute("x", x);
    node.setAttribute("y", y);
    for (PointElement p : getPoints()) {
      SimpleXmlElement child = new SimpleXmlElement();
      p.write(child);
      node.addChild(child);
    }
  }

}
