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
package org.eclipse.scout.rt.spec.client.link;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiLinkPostProcessor;
import org.junit.Test;

/**
 * Tests for {@link MediawikiLinkPostProcessor}
 */
public class LinkPostProcessorTest {

  private static final String TEST_ID = "org.eclipse.scout.rt.spec.example.ui.swing.form.TestPersonForm$MainBox$DetailBox$TabBox$SocialMediaBox$SocialMediaField";
  private static final String TEST_NAME = "SocialMediaField";
  private static final String TEST_REFERENCE = "SocialMediaField_.28Social_Media.29";

  private static final String TEST_WIKI_LINK = ""
      + "blubbr"
      + "<replace>"
      + "<link><id>" + TEST_ID + "</id><name>" + TEST_NAME + "</name></link>"
      + "<link><id>ID2</id><name>NAME2</name></link>"
      + "</replace>"
      + "BLUBBREND";

  @Test
  public void testTagReplacement() {
    Properties testProperties = new Properties();
    testProperties.put(TEST_ID, TEST_NAME);

    String replacedTags = StringUtility.replaceTags(TEST_WIKI_LINK, "replace", "bla");
    assertEquals("blubbrblaBLUBBREND", replacedTags);
  }
}
