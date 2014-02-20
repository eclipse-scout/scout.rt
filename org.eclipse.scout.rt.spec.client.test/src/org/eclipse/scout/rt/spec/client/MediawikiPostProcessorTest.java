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
package org.eclipse.scout.rt.spec.client;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.spec.client.MediawikiPostProcessor.P_LinkProcessor;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link MediawikiPostProcessor}
 */
public class MediawikiPostProcessorTest {

  private static Properties s_links;
  private static MediawikiPostProcessor.P_LinkProcessor s_linkProcessor;

  @BeforeClass
  public static void setup() throws ProcessingException {
    s_links = new Properties();
    s_links.setProperty("target1", "targetFile.mediawiki#target1");
    s_links.setProperty("target2", "targetFile.mediawiki#target2");
    s_links.setProperty("c_776bff1c-6cfa-41d8-b01d-c2240103180c", "testFormTargetFile.mediawiki#c_776bff1c-6cfa-41d8-b01d-c2240103180c");
    s_links.setProperty("lo_c_776bff1c-6cfa-41d8-b01d-c2240103180c", "testFormTargetFile.mediawiki#lo_c_776bff1c-6cfa-41d8-b01d-c2240103180c");
    s_linkProcessor = new MediawikiPostProcessor.P_LinkProcessor(s_links, new MediawikiPostProcessor().m_classIdTargets);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinkProcessor() {
    String line = "gugus [[target1|Target One]] middle  [[target2|Target Two]]  end";
    String expected = "gugus [[targetFile.mediawiki#target1|Target One]] middle  [[targetFile.mediawiki#target2|Target Two]]  end";
    assertEquals("Links not processed as expected.", expected, s_linkProcessor.processLine(line));
    testCorruptLinksOnSameLine(line, expected);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLineWithJustOneLink() {
    String line = "[[target1|Target One]]";
    String expected = "[[targetFile.mediawiki#target1|Target One]]";
    assertEquals("Link not processed as expected.", expected, s_linkProcessor.processLine(line));
    testCorruptLinksOnSameLine(line, expected);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinkProcessorMissingTargetProperty() {
    String line = "gugus [[anotherLink|Target One]] middle  [[target2|Target Two]]  end";
    String expected = "gugus Target One middle  [[targetFile.mediawiki#target2|Target Two]]  end";
    assertEquals("Expected plaintext replacement for first and normal replacement for second link.", expected, s_linkProcessor.processLine(line));
    testCorruptLinksOnSameLine(line, expected);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinkProcessorInputQualifiedLink() {
    String line = "gugus [[anotherTarget.mediawiki#anotherLink|Target One]] middle  [[anotherTarget.mediawiki#target2|Target Two]]  end";
    assertEquals("No replacement expected: If link is qualified in input, processing should have no impact.", line, s_linkProcessor.processLine(line));
    testCorruptLinksOnSameLine(line, line);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinksWithIllegalCharsInTarget() {
    String line = "a [[target:1|Target One]] [[1target|Target One]] [[target!1|Target One]] [[Aü1|Target One]]";
    assertEquals("No replacement expected: TargetIds may contain letters A-Z/a-z, digits, periods and hyphes and must start with a letter.", line, s_linkProcessor.processLine(line));
    testCorruptLinksOnSameLine(line, line);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinkwithSimpleClassName() {
    String line = "a [[MediawikiLinkProcessingTestForm|Test Form]]";
    String expected = "a [[testFormTargetFile.mediawiki#c_776bff1c-6cfa-41d8-b01d-c2240103180c|Test Form]]";
    assertEquals("Link with simple class name not processed as expected.", expected, s_linkProcessor.processLine(line));
    testCorruptLinksOnSameLine(line, expected);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinkwithSimpleClassNamePrefixedForLinearOutput() {
    String prefix = LinearOutputPostProcessor.ANCHOR_PREFIX;
    String line = "a [[" + prefix + "MediawikiLinkProcessingTestForm|Test Form]]";
    String expected = "a [[testFormTargetFile.mediawiki#" + prefix + "c_776bff1c-6cfa-41d8-b01d-c2240103180c|Test Form]]";
    assertEquals("Prefixed link with simple class name not processed as expected.", expected, s_linkProcessor.processLine(line));
    testCorruptLinksOnSameLine(line, expected);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinkwithQualifiedClassName() {
    String line = "a [[org.eclipse.scout.rt.spec.client.MediawikiPostProcessorTest$MediawikiLinkProcessingTestForm|Test Form]]";
    String expected = "a [[testFormTargetFile.mediawiki#c_776bff1c-6cfa-41d8-b01d-c2240103180c|Test Form]]";
    assertEquals("Link with simple class name not processed as expected.", expected, s_linkProcessor.processLine(line));
    testCorruptLinksOnSameLine(line, expected);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinkwithQualifiedClassNamePrefixedForLinearOutput() {
    String prefix = LinearOutputPostProcessor.ANCHOR_PREFIX;
    String line = "a [[" + prefix + "org.eclipse.scout.rt.spec.client.MediawikiPostProcessorTest$MediawikiLinkProcessingTestForm|Test Form]]";
    String expected = "a [[testFormTargetFile.mediawiki#" + prefix + "c_776bff1c-6cfa-41d8-b01d-c2240103180c|Test Form]]";
    assertEquals("Prefixed link with simple class name not processed as expected.", expected, s_linkProcessor.processLine(line));
    testCorruptLinksOnSameLine(line, expected);
  }

  /**
   * @param line
   * @param expected
   */
  private void testCorruptLinksOnSameLine(String line, String expected) {
    String prefix = "[[a|";
    String postfix = " *ç% [[g [[missingDisplayName]]";
    String lineWithCorruptLinks = prefix + line + postfix;
    assertEquals("Corrupt links on same line should not have any impact.", prefix + expected + postfix, s_linkProcessor.processLine(lineWithCorruptLinks));
  }

  @ClassId("776bff1c-6cfa-41d8-b01d-c2240103180c")
  public static class MediawikiLinkProcessingTestForm extends AbstractForm {
    /**
     * @throws ProcessingException
     */
    public MediawikiLinkProcessingTestForm() throws ProcessingException {
      super();
    }

  }

}
