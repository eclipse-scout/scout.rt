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

import org.eclipse.scout.rt.spec.client.MediawikiPostProcessor.P_LinkProcessor;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link MediawikiPostProcessor}
 */
public class MediawikiPostProcessorTest {

  private Properties m_links;

  @Before
  public void setup() {
    m_links = new Properties();
    m_links.setProperty("target1", "targetFile.mediawiki#target1");
    m_links.setProperty("target2", "targetFile.mediawiki#target2");
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinkProcessor() {
    String line = "gugus [[target1|Target One]] middle  [[target2|Target Two]]  end";
    String expected = "gugus [[targetFile.mediawiki#target1|Target One]] middle  [[targetFile.mediawiki#target2|Target Two]]  end";
    assertEquals("Links not processed as expected.", expected, new MediawikiPostProcessor.P_LinkProcessor(m_links).processLine(line));
    testCorruptLinksOnSameLine(line, expected);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLineWithJustOneLink() {
    String line = "[[target1|Target One]]";
    String expected = "[[targetFile.mediawiki#target1|Target One]]";
    assertEquals("Link not processed as expected.", expected, new MediawikiPostProcessor.P_LinkProcessor(m_links).processLine(line));
    testCorruptLinksOnSameLine(line, expected);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinkProcessorMissingTargetProperty() {
    String line = "gugus [[anotherLink|Target One]] middle  [[target2|Target Two]]  end";
    String expected = "gugus Target One middle  [[targetFile.mediawiki#target2|Target Two]]  end";
    assertEquals("Expected plaintext replacement for first and normal replacement for second link.", expected, new MediawikiPostProcessor.P_LinkProcessor(m_links).processLine(line));
    testCorruptLinksOnSameLine(line, expected);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinkProcessorInputQualifiedLink() {
    String line = "gugus [[anotherTarget.mediawiki#anotherLink|Target One]] middle  [[anotherTarget.mediawiki#target2|Target Two]]  end";
    assertEquals("No replacement expected: If link is qualified in input, processing should have no impact.", line, new MediawikiPostProcessor.P_LinkProcessor(m_links).processLine(line));
    testCorruptLinksOnSameLine(line, line);
  }

  /**
   * Test for {@link P_LinkProcessor}
   */
  @Test
  public void testLinksWithIllegalCharsInTarget() {
    String line = "a [[target:1|Target One]] [[1target|Target One]] [[target!1|Target One]] [[Aü1|Target One]]";
    assertEquals("No replacement expected: TargetIds may contain letters A-Z/a-z, digits, periods and hyphes and must start with a letter.", line, new MediawikiPostProcessor.P_LinkProcessor(m_links).processLine(line));
    testCorruptLinksOnSameLine(line, line);
  }

  /**
   * @param line
   * @param expected
   */
  private void testCorruptLinksOnSameLine(String line, String expected) {
    String prefix = "[[a|";
    String postfix = " *ç% [[g [[missingDisplayName]]";
    String lineWithCorruptLinks = prefix + line + postfix;
    assertEquals("Corrupt links on same line should not have any impact.", prefix + expected + postfix, new MediawikiPostProcessor.P_LinkProcessor(m_links).processLine(lineWithCorruptLinks));
  }
}
