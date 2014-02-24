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

import static org.eclipse.scout.rt.testing.commons.ScoutAssert.assertListEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.eclipse.scout.rt.spec.client.MediawikiAnchorPostProcessor.P_ScoutAnchorProcessor;
import org.junit.Test;

/**
 * Tests for {@link MediawikiAnchorPostProcessor}
 */
public class MediawikiAnchorPostProcessorTest {

  /**
   * Test for {@link P_ScoutAnchorProcessor}
   */
  @Test
  public void testAnchorProcessor() {
    String line1 = "ezezeueu {{a:a_721c3f5f-bd28-41e4-a5f0-d78891034485}} middle  {{a:a_999c3f5f-bd28-41e4-a5f0-d78891034485}} end";
    P_ScoutAnchorProcessor anchorProcessor = new MediawikiAnchorPostProcessor.P_ScoutAnchorProcessor();
    assertEquals("processing scout-anchors failed", "ezezeueu <span id=\"a_721c3f5f-bd28-41e4-a5f0-d78891034485\"/> middle  <span id=\"a_999c3f5f-bd28-41e4-a5f0-d78891034485\"/> end", anchorProcessor.processLine(line1));

    ArrayList<String> expectedAnchors = new ArrayList<String>();
    expectedAnchors.add("a_721c3f5f-bd28-41e4-a5f0-d78891034485");
    expectedAnchors.add("a_999c3f5f-bd28-41e4-a5f0-d78891034485");
    assertListEquals(expectedAnchors, anchorProcessor.getAnchorsIds());

    String line2 = "line 2 {{a:AnyThingUnique.1}} middle  {{a:AnyThingUnique.2}} end";
    assertEquals("processing scout-anchors failed", "line 2 <span id=\"AnyThingUnique.1\"/> middle  <span id=\"AnyThingUnique.2\"/> end", anchorProcessor.processLine(line2));
    expectedAnchors.add("AnyThingUnique.1");
    expectedAnchors.add("AnyThingUnique.2");
    assertListEquals(expectedAnchors, anchorProcessor.getAnchorsIds());

  }
}
