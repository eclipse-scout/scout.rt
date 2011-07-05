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
 * A single point
 * <p>
 * The xml representation is
 * 
 * <pre>
 * <xmp>
 *  <point id="" x="0" y="0" color="argb">
 * </xmp>
 * </pre>
 */
public class PointElement extends AbstractGraphicsElement {
  private static final long serialVersionUID = 1L;

  public double x;
  public double y;

  public PointElement() {
  }

  @Override
  public void read(SimpleXmlElement node) throws ProcessingException {
    super.read(node);
    checkRequired(node, "x");
    x = node.getDoubleAttribute("x");
    checkRequired(node, "y");
    y = node.getDoubleAttribute("y");
  }

  @Override
  public void write(SimpleXmlElement node) throws ProcessingException {
    super.write(node);
    node.setName("point");
    node.setAttribute("x", x);
    node.setAttribute("y", y);
  }
}
