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
package org.eclipse.scout.svg.client.internal;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

public class ExtractImages {
  private static final String FOLDER = "D:\\dev\\svg";

  public void run() throws Exception {
    File f = new File(FOLDER, "test.svg");
    SimpleXmlElement xml = new SimpleXmlElement();
    xml.parseStream(new FileInputStream(f));
    visitSubtree(xml);
  }

  protected void visitSubtree(SimpleXmlElement parent) throws Exception {
    for (SimpleXmlElement e : parent.getChildren()) {
      visitNode(e);
      visitSubtree(e);
    }
  }

  private int imgIndex = 0;

  protected void visitNode(SimpleXmlElement e) throws Exception {
    if (e.getName().equalsIgnoreCase("image")) {
      int width = e.getIntAttribute("width");
      int height = e.getIntAttribute("height");
      double opacity = e.getDoubleAttribute("opacity", 1.0);
      String xlink = e.getStringAttribute("xlink:href");
      Matcher m = Pattern.compile("data:image/([a-z]+);base64,(.+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(xlink);
      if (!m.matches()) throw new IllegalArgumentException(xlink);
      String format = m.group(1);
      byte[] content = Base64Utility.decode(m.group(2));
      System.out.println("IMAGE " + width + "," + height + "," + opacity + "," + format + "," + content.length);
      File f = new File(FOLDER + "\\svgout", "img" + imgIndex + "." + format);
      f.getParentFile().mkdirs();
      imgIndex++;
      IOUtility.writeContent(f.getAbsolutePath(), content);
    }
  }

}
