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
import java.io.FileOutputStream;

import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

public class XmlProcessor {
  private static final String FOLDER = "D:\\dev\\svg";

  public void run() throws Exception {
    File fIn = new File(FOLDER, "test.svg");
    File fOut = new File(FOLDER, "out.svg");
    SimpleXmlElement xml = new SimpleXmlElement();
    xml.parseStream(new FileInputStream(fIn));
    visitSubtree(xml, 0);
    xml.writeDocument(new FileOutputStream(fOut), null, "utf-8");
  }

  protected void visitSubtree(SimpleXmlElement parent, int level) throws Exception {
    for (SimpleXmlElement e : parent.getChildren()) {
      visitNode(e, level);
      visitSubtree(e, level + 1);
    }
  }

  private int textIndex = 0;

  protected void visitNode(SimpleXmlElement e, int level) throws Exception {
    //replace texts
    if (e.getName().equalsIgnoreCase("text") || e.getName().equalsIgnoreCase("tspan")) {
//      SimpleXmlElement textNode=e.getName().equalsIgnoreCase("text")?e: e.getParent();
      String text = e.getContent();
      if (text.trim().length() > 0) {
//        System.out.println("CockpitSvg" + text + "=" + text);
        e.setContent("" + textIndex + "_" + text);
        textIndex++;
      }
      //remove data texts
    }
    //remove circle bullets
    if (e.getName().equals("circle") && e.getStringAttribute("fill").equals("#464646")) {
      e.getParent().removeChild(e);
      System.out.println("remove circle");
      return;
    }
  }
}
