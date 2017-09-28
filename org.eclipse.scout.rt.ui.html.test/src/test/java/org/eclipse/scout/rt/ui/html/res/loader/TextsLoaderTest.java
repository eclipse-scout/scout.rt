/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res.loader;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TextsLoaderTest {

  @Test
  public void testProcessLanguageTags_addDefault() {
    List<String> tags = new ArrayList<>();
    tags.add("en");
    tags.add("de");
    tags = new TextsLoader().processLanguageTags(tags);

    Assert.assertEquals(3, tags.size());
    Assert.assertEquals(null, tags.get(0));
    Assert.assertEquals("en", tags.get(1));
    Assert.assertEquals("de", tags.get(2));
  }

  @Test
  public void testProcessLanguageTags_handleEmpty() {
    List<String> tags = new ArrayList<>();
    tags = new TextsLoader().processLanguageTags(tags);

    Assert.assertEquals(1, tags.size());
    Assert.assertEquals(null, tags.get(0));
  }

  @Test
  public void testProcessLanguageTags_addMissingLanguage() {
    List<String> tags = new ArrayList<>();
    tags.add("en");
    tags.add("de-CH");
    tags = new TextsLoader().processLanguageTags(tags);

    Assert.assertEquals(4, tags.size());
    Assert.assertEquals(null, tags.get(0));
    Assert.assertEquals("en", tags.get(1));
    Assert.assertEquals("de", tags.get(2));
    Assert.assertEquals("de-CH", tags.get(3));
  }

  @Test
  public void testProcessLanguageTags_removeDuplicates() {
    List<String> tags = new ArrayList<>();
    tags.add("en");
    tags.add("en");
    tags.add("de-CH");
    tags.add("de-DE");
    tags.add("de-CH");
    tags = new TextsLoader().processLanguageTags(tags);

    Assert.assertEquals(5, tags.size());
    Assert.assertEquals(null, tags.get(0));
    Assert.assertEquals("en", tags.get(1));
    Assert.assertEquals("de", tags.get(2));
    Assert.assertEquals("de-CH", tags.get(3));
    Assert.assertEquals("de-DE", tags.get(4));
  }
}
