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

import java.io.File;
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
    m_testFile = new File(m_config.getBundleRoot(), m_config.getRelativeMediawikiSourceDirPath() + File.separator + TEST_FILE);
  }

  @Test
  public void testReplaceAnchor() {
    String testString = "==[[CC:Einleitung|Introduction]]==";
    String expected = "==[[CC_Einleitung#CC:Einleitung|Introduction]]==";
    MediawikiAnchorCollector m = new MediawikiAnchorCollector(m_testFile);
    Properties p = new Properties();
    p.setProperty("CC:Einleitung", "CC_Einleitung#CC:Einleitung");
    String replacedLine = m.replaceLink(testString, p);
    Assert.assertEquals(expected, replacedLine);
  }

  @Test
  // TODO ASA ask jgu: why is link starting at pos 0 not allowed?
  public void testReplaceAnchorNegative() {
    String testString = "[[CC:Einleitung|Introduction]]";
    String expected = "[[CC:Einleitung|Introduction]]";
    MediawikiAnchorCollector m = new MediawikiAnchorCollector(m_testFile);
    Properties p = new Properties();
    p.setProperty("CC", "xy");
    String replacedLine = m.replaceLink(testString, p);
    Assert.assertEquals(expected, replacedLine);
  }

}
