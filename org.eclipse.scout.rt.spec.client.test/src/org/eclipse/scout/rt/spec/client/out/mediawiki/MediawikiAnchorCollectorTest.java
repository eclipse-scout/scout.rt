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
package org.eclipse.scout.rt.spec.client.out.mediawiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link MediawikiAnchorCollector}
 */
public class MediawikiAnchorCollectorTest {
  private static final String TEST_FILE = "CC_Einleitung.mediawiki";
  private static final String TEST_INTRO = "CC.mediawiki";
  private static final String TEST_LINKS_FILE = "links.properties";
  private final SpecFileConfig m_config = new SpecFileConfig("org.eclipse.scout.rt.spec.client.test");
  private File m_testFile;

  @Before
  public void setUp() throws ProcessingException {
    m_testFile = new File(m_config.getMediawikiInDir(), TEST_FILE);
  }

  @Test
  public void testCollectAnchors() throws ProcessingException {
    m_testFile = new File(m_config.getMediawikiInDir(), TEST_FILE);

    MediawikiAnchorCollector ac = new MediawikiAnchorCollector(m_testFile);
    List<String> anchors = ac.collectAnchors(m_testFile);
    assertTrue("CC:New should be extracted", anchors.contains("CC:New"));
    assertTrue("CC:New should be extracted", anchors.contains("CC:Intro"));
    assertEquals("File should contain exactly 2 tags", 2, anchors.size());
  }

  /**
   * readAnchorTags
   */
  @Test
  public void testSimpleAnchor() {
    MediawikiAnchorCollector ac = new MediawikiAnchorCollector(m_testFile);
    String testTag = "CC:Wizard";
    String testTitle = "== Wizard {{CC:Wizard}} ==";
    String result = ac.readAnchorTag(testTitle);
    assertEquals(testTag, result);
  }

  @Test
  public void testGetProperties() throws ProcessingException {
    //TODO jgu
    MediawikiAnchorCollector ac = new MediawikiAnchorCollector(m_testFile);
//    ac.collectAnchors(TEST_FILE);
    File linksFile = new File(m_config.getSpecDir(), TEST_LINKS_FILE);
    FileWriter writer = null;
    try {
      writer = new FileWriter(linksFile);
      Properties p = new Properties();
      p.setProperty("test", "bla");
      p.store(writer, "");
    }
    catch (IOException e) {
      try {
        if (writer != null) {
          writer.close();
        }
      }
      catch (IOException e1) {
        //nop
      }
    }
  }

  @Test
  public void testReplaceAnchor() {
    String testString = "==[[CC:Einleitung|Introduction]]==";
    String expected = "==[[CC_Einleitung#CC:Einleitung|Introduction]]==";
    MediawikiAnchorCollector m = new MediawikiAnchorCollector(m_testFile);
    Properties p = new Properties();
    p.setProperty("CC:Einleitung", "CC_Einleitung#CC:Einleitung");
    String replacedLine = m.replaceText(testString, p);
    Assert.assertEquals(expected, replacedLine);
  }

  @Test
  public void testReplaceAnchorNegative() {
    String testString = "[[CC:Einleitung|Introduction]]";
    String expected = "[[CC:Einleitung|Introduction]]";
    MediawikiAnchorCollector m = new MediawikiAnchorCollector(m_testFile);
    Properties p = new Properties();
    p.setProperty("CC", "xy");
    String replacedLine = m.replaceText(testString, p);
    Assert.assertEquals(expected, replacedLine);
  }

  @Test
  public void testReplaceLinks() throws ProcessingException {
    File linksFile = new File(m_config.getSpecInDir(), TEST_LINKS_FILE);
    File introFile = new File(m_config.getMediawikiInDir(), TEST_INTRO);
    MediawikiAnchorCollector m = new MediawikiAnchorCollector(m_testFile);
    m.replaceLinks(introFile, linksFile);
  }
}
