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
package org.eclipse.scout.commons.html.tests;

import java.io.File;
import java.io.FileReader;

import javax.swing.text.html.HTMLDocument;

import org.eclipse.scout.commons.CSSPatch;
import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.IOUtility;

/**
 *
 */
public class TestHTMLUtility {
  public static void main(String[] args) throws Exception {
    new TestHTMLUtility().run();
  }

  public void run() throws Exception {
    CSSPatch.apply();
    //
    String s1 = IOUtility.getContent(new FileReader(new File("D:/TEMP/original.html")));
    HTMLDocument doc = HTMLUtility.toHtmlDocument(s1);
    String s2 = HTMLUtility.toHtmlText(doc);
    System.out.println(s2);
  }

}
