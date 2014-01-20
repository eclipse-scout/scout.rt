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

import org.junit.Test;

/**
 * Tests for {@link MediawikiUtility}
 */
public class MediawikiUtilityTest {

  @Test
  public void excapeWikiCharsTest() {
    String testLabel = "Unit [kg]";
    String escapedLabel = "Unit <nowiki>[</nowiki>kg<nowiki>]</nowiki>";
    String newLabel = MediawikiUtility.transformToWiki(testLabel);
    assertEquals(escapedLabel, newLabel);
  }
}
