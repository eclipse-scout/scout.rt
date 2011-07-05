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

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

/**
 * A local resource (for an image). The content type is ignored and assumed to be base64
 * <p>
 * The xml representation is
 * 
 * <pre>
 * <xmp>
 *  <resource id="flower.png" content-type="binary/base64">.......</resource>
 * </xmp>
 * </pre>
 */
public class ResourceElement implements IScoutSVGElement {
  private static final long serialVersionUID = 1L;

  public String id;
  public byte[] content;

  public ResourceElement() {
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean isInteractive() {
    return false;
  }

  @Override
  public void read(SimpleXmlElement node) {
    id = node.getStringAttribute("id");
    content = Base64Utility.decode(node.getContent());
  }

  @Override
  public void write(SimpleXmlElement node) {
    node.setName("resource");
    if (id != null) {
      node.setAttribute("id", id);
    }
    node.setAttribute("content-type", "binary/base64");
    node.setContent(Base64Utility.encode(content));
  }

}
