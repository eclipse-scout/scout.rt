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
 * A text
 * <p>
 * The xml representation is
 * 
 * <pre>
 * <xmp>
 *  <text id="" x="0" y="0" align="center" font-size="8" font-weight="bold" color="argb" scale-up="true|false">Hello World</text>
 * </xmp>
 * </pre>
 * 
 * The attribute scale-up="false" specifies that the text size is not scaled up (larger than scale 1.0), the text is
 * only scaled down (smaller than scale 1.0).
 */
public class TextElement extends AbstractGraphicsElement {
  private static final long serialVersionUID = 1L;

  public double x;
  public double y;
  public Boolean scaleUp;
  public String align;
  public Integer fontSize;
  public String fontWeight;
  public String text;

  public TextElement() {
  }

  @Override
  public void read(SimpleXmlElement node) throws ProcessingException {
    super.read(node);
    checkRequired(node, "x");
    x = node.getDoubleAttribute("x");
    checkRequired(node, "y");
    y = node.getDoubleAttribute("y");
    if (node.hasAttribute("scale-up")) {
      scaleUp = node.getBooleanAttribute("scale-up", "true", "false", false);
    }
    align = node.getStringAttribute("align");
    if (node.hasAttribute("font-size")) {
      fontSize = node.getIntAttribute("font-size");
    }
    fontWeight = node.getStringAttribute("font-weight");
    text = node.getContent();
  }

  @Override
  public void write(SimpleXmlElement node) throws ProcessingException {
    super.write(node);
    node.setName("text");
    node.setAttribute("x", x);
    node.setAttribute("y", y);
    if (scaleUp != null) {
      node.setAttribute("scale-up", "" + scaleUp);
    }
    if (align != null) {
      node.setAttribute("align", align);
    }
    if (fontSize != null) {
      node.setAttribute("font-size", fontSize.intValue());
    }
    if (fontWeight != null) {
      node.setAttribute("font-weight", fontWeight);
    }
    node.setContent(text);
  }
}
