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
package org.eclipse.scout.commons.xmlparser.internal;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlParser;

public final class TestScoutXml {
  private ScoutXmlDocument testDocument1;
  private ScoutXmlDocument testDocument2;

  private TestScoutXml() {
    testDocument1 = new ScoutXmlDocument(
        "<library>" +
            "  <name>Book Library</name>" +
            "  <books>" +
            "    <book>" +
            "      <title>XML in a Nutshell</title>" +
            "      <year>2004</year>" +
            "      <author>" +
            "        <forename>Elliotte Rusty, W.Scott Means</forename>" +
            "        <lastname>Harold</lastname>" +
            "      </author>" +
            "      <author>" +
            "        <forename>W.Scott</forename>" +
            "        <lastname>Means</lastname>" +
            "      </author>" +
            "      <publisher>O'Reilly</publisher>" +
            "    </book>" +
            "  </books>" +
            "</library>"
        );
    testDocument2 = new ScoutXmlDocument(
        "<root a1='text' a2='7' a3='7.1' a4='true' a5='2006.01.13' a6=''/>"
        );
  }

  public void testErrorHandling() {
    // System.out.println("\n--- Test error handling --------------------------------------------------------------------\n");

    try {
      testDocument2.getRoot().getAttribute("a0");
    }
    catch (Exception exception) {
      System.out.println("Error handling test: " + exception);
    }

    try {
      testDocument2.getRoot().getAttributeAsInt("a0");
    }
    catch (Exception exception) {
      System.out.println("Error handling test: " + exception);
    }

    try {
      testDocument2.getRoot().getAttributeAsInt("a1");
    }
    catch (Exception exception) {
      System.out.println("Error handling test: " + exception);
    }
  }

  private void testFileParsingWriting(String readDirectory, String writeDirectory) throws Exception {
    File[] files = new File(readDirectory).listFiles();

    for (int i = 0; (i < files.length); i++) {
      ScoutXmlParser parser = new ScoutXmlParser();
      parser.setValidating(false);
      parser.setIgnoreSaxErrors(false);
      parser.setIgnoreExternalEntities(true);
      ScoutXmlDocument document = null;

      if (files[i].getName().endsWith("dtd")) {
        continue;
      }
      if (readDirectory != null) {
        System.out.println();
        System.out.println("- Reading '" + files[i].getName() + "'...");

        document = parser.parse(new FileInputStream(files[i]), readDirectory);
        // document = parser.parse(files[i].getAbsolutePath());
        // System.out.println(" " + (document.getRoot().countDescendants() +
        // 1) + " element(s) read in " + (System.currentTimeMillis() -
        // timeR)/1000 + " second(s).");
      }

      if (writeDirectory != null && document != null) {
        System.out.println();
        long timeW = System.currentTimeMillis();
        System.out.println("- Writing '" + files[i].getName() + "'...");
        document.write(new File(writeDirectory + "\\" + files[i].getName()));
        System.out.println("  Done in " + (System.currentTimeMillis() - timeW) / 1000 + " second(s).");
      }

      // System.out.println();
      // System.out.println(document.toPrettyString());
      // System.out.println();
    }
  }

  public void testMixedContent() {
    // System.out.println("\n--- Test mixed content ---------------------------------------------------------------------\n");

    String test = "<a>t1<b>t21<c>t4</c>t22</b>t3</a>";

    ScoutXmlDocument document = new ScoutXmlDocument(test);
    document.setPrettyPrint(false);

    System.out.println("Mixed content test successful:\t" + document.equalsSemantically(test));
  }

  private void testModifying() {
    // System.out.println("\n--- Test document modifying ----------------------------------------------------------------\n");

    new ScoutXmlDocument("<data><table/></data>").getChild("data").addChild("row").addText("2006.10");

    ScoutXmlDocument doc1 = new ScoutXmlDocument("<A>x</A>");
    ScoutXmlDocument doc2 = new ScoutXmlDocument("<B>y</B>");
    doc1.getRoot().addChild(doc2.getRoot());
    doc1.getRoot().addText("z");
    doc1.getRoot().addChild("C1");
    doc1.getRoot().addChild("C2");
    doc1.getRoot().removeChild("C1");

    System.out.println("Document modifying successful:\t" + doc1.equalsSemantically("<A>x<B>y</B>z<C2/></A>"));
  }

  private void testVarious() {
    // System.out.println("\n--- Test Various ---------------------------------------------------------------------------\n");

    new ScoutXmlDocument().toString();
    new ScoutXmlDocument("").toString();

    System.out.println("Various testing successful:\t" + new ScoutXmlDocument("<x/>").equalsSemantically("<x/>"));
  }

  public static void main(String[] args) throws Exception {
    TestScoutXml xmlTest = new TestScoutXml();

    xmlTest.testMixedContent();
    xmlTest.testModifying();
    xmlTest.testVarious();

    // xmlTest.testErrorHandling();

    // xmlTest.testFileParsingWriting("C:\\Development\\Projects\\ScoutXml\\Test\\Originals",
    // "C:\\Development\\Projects\\ScoutXml\\Test\\Written by ScoutXml");
  }
}
