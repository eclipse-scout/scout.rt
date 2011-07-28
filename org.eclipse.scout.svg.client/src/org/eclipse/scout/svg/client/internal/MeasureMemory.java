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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.svg.client.SVGUtility;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

public class MeasureMemory {
  private static final String FOLDER = "D:\\dev\\svg";

  static long m0 = 0;
  static long m1;
  static Runtime rt = Runtime.getRuntime();

  private static long memused() {
    return rt.totalMemory() - rt.freeMemory();
  }

  private static void sample(String name) throws Exception {
    int i = 0;
    long delta = -1;
    long a, b;
    while (i < 5 || delta != 0) {
      a = memused();
      System.gc();
      Thread.sleep(50L);
      i++;
      b = memused();
      delta = b - a;
    }
    m1 = memused();
    System.out.println(name + ": " + (m1 - m0));
    m0 = m1;
  }

  public void run() throws Exception {
    System.out.println("memused: " + memused());
    run0();
    System.out.println("memused: " + memused());
    run0();
    System.out.println("memused: " + memused());
    run0();
    System.out.println("memused: " + memused());
  }

  private void run0() throws Exception {

    sample("init");

    //load document
    File f = new File(FOLDER, "test.svg");
    byte[] b1 = IOUtility.getContent(new FileInputStream(f));
    SVGDocument doc = SVGUtility.readSVGDocument(new ByteArrayInputStream(b1));
    ByteArrayOutputStream b2 = new ByteArrayOutputStream();
    SVGUtility.writeSVGDocument(doc, b2);
    doc = SVGUtility.readSVGDocument(new ByteArrayInputStream(b2.toByteArray()));
    f = null;
    b1 = null;
    b2 = null;

    sample("doc");

    //remove image binary data
    NodeList nl = doc.getRootElement().getElementsByTagName("image");
    if (nl != null) {
      for (int i = 0; i < nl.getLength(); i++) {
        Element e = (Element) nl.item(i);
        String href = e.getAttributeNS("http://www.w3.org/1999/xlink", "href");
        if (href != null && href.startsWith("data:")) {
          e.removeAttributeNS("http://www.w3.org/1999/xlink", "href");
        }
      }
    }
    nl = null;

    sample("post");

    doc = null;

    sample("null");
  }
}
