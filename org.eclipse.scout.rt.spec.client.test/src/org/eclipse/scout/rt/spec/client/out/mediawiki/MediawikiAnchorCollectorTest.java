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
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.junit.Test;

/**
 * Test for {@link MediawikiAnchorCollector}
 */
public class MediawikiAnchorCollectorTest {
  private static final String TEST_FILE = "CC_Einleitung.mediawiki";

  @Test
  public void testCollectAnchors() throws ProcessingException {
    SpecFileConfig config = new SpecFileConfig("org.eclipse.scout.rt.spec.client.test");
    File inFile = new File(config.getMediawikiInDir(), TEST_FILE);

    MediawikiAnchorCollector ac = new MediawikiAnchorCollector();
    List<String> anchors = ac.collectAnchors(inFile);
    assertTrue("CC:New should be extracted", anchors.contains("CC:New"));
    assertTrue("CC:New should be extracted", anchors.contains("CC:Intro"));
    assertEquals("File should contain exactly 2 tags", 2, anchors.size());
  }

  /**
   * readAnchorTags
   */
  @Test
  public void testSimpleAnchor() {
    MediawikiAnchorCollector ac = new MediawikiAnchorCollector();
    String testTag = "CC:Wizard";
    String testTitle = "== Wizard {{CC:Wizard}} ==";
    String result = ac.readAnchorTag(testTitle);
    assertEquals(testTag, result);
  }
}
