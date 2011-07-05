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
 * The base class for all scout svg items
 */
public abstract class AbstractGraphicsElement implements IScoutSVGElement {
  private static final long serialVersionUID = 1L;

  public String id;

  /**
   * true if the element is interactive (hyper link like appearance in ui)
   */
  boolean interactive;
  /**
   * color in 6-character hex format RGB or 8-character hex format ARGB where A is the opacity/transparency 0xff=opaque,
   * 0x00=trasparent
   */
  public String color;
  /**
   * color in 6-character hex format RGB or 8-character hex format ARGB where A is the opacity/transparency 0xff=opaque,
   * 0x00=trasparent
   */
  public String borderColor;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean isInteractive() {
    return interactive;
  }

  @Override
  public void read(SimpleXmlElement node) throws ProcessingException {
    id = node.getStringAttribute("id");
    interactive = node.getBooleanAttribute("interactive", "true", "false", false);
    color = node.getStringAttribute("color");
    if (color != null && color.startsWith("#")) {
      color = color.substring(1);
    }
    borderColor = node.getStringAttribute("border-color");
    if (borderColor != null && borderColor.startsWith("#")) {
      borderColor = borderColor.substring(1);
    }
  }

  @Override
  public void write(SimpleXmlElement node) throws ProcessingException {
    if (id != null) {
      node.setAttribute("id", id);
    }
    if (interactive) {
      node.setAttribute("interactive", true);
    }
    if (color != null) {
      node.setAttribute("color", "#" + color);
    }
    if (borderColor != null) {
      node.setAttribute("border-color", "#" + borderColor);
    }
  }

  protected void checkRequired(SimpleXmlElement node, String attribute) throws ProcessingException {
    if (!node.hasAttribute(attribute)) {
      throw new ProcessingException("missing attribute '" + attribute + "'");
    }
  }
}
