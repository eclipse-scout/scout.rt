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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

/**
 * A circle
 * <p>
 * The xml representation is
 * 
 * <pre>
 * <xmp>
 *  <circle id="" x="0" y="0" width="0" height="0" color="argb" border-color="argb"/>
 * </xmp>
 * </pre>
 */
public class CircleElement extends AbstractGraphicsElement {
  private static final long serialVersionUID = 1L;

  public double x;
  public double y;
  public double width;
  public double height;

  public CircleElement() {
  }

  @Override
  public void read(SimpleXmlElement node) throws ProcessingException {
    super.read(node);
    checkRequired(node, "x");
    x = node.getDoubleAttribute("x");
    checkRequired(node, "y");
    y = node.getDoubleAttribute("y");
    checkRequired(node, "width");
    width = node.getDoubleAttribute("width");
    checkRequired(node, "height");
    height = node.getDoubleAttribute("height");
  }

  @Override
  public void write(SimpleXmlElement node) throws ProcessingException {
    super.write(node);
    node.setName("circle");
    node.setAttribute("x", x);
    node.setAttribute("y", y);
    node.setAttribute("width", width);
    node.setAttribute("height", height);
  }
}
