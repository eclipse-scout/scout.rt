/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res.loader;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class TextsLoaderTest {

  @Test
  public void testProcessLanguageTags_addDefault() {
    List<Locale> locales = new TextsLoader().processLanguageTags(Arrays.asList("en", "de"));

    Assert.assertEquals(3, locales.size());
    Assert.assertEquals(Locale.ROOT, locales.get(0));
    Assert.assertEquals(Locale.forLanguageTag("en"), locales.get(1));
    Assert.assertEquals(Locale.forLanguageTag("de"), locales.get(2));
  }

  @Test
  public void testProcessLanguageTags_handleEmpty() {
    List<Locale> locales = new TextsLoader().processLanguageTags(Arrays.asList());

    Assert.assertEquals(1, locales.size());
    Assert.assertEquals(Locale.ROOT, locales.get(0));
  }

  @Test
  public void testProcessLanguageTags_addMissingLanguage() {
    List<Locale> locales = new TextsLoader().processLanguageTags(Arrays.asList("en", "de-CH"));

    Assert.assertEquals(4, locales.size());
    Assert.assertEquals(Locale.ROOT, locales.get(0));
    Assert.assertEquals(Locale.forLanguageTag("en"), locales.get(1));
    Assert.assertEquals(Locale.forLanguageTag("de"), locales.get(2));
    Assert.assertEquals(Locale.forLanguageTag("de-CH"), locales.get(3));
  }

  @Test
  public void testProcessLanguageTags_removeDuplicates() {
    List<Locale> locales = new TextsLoader().processLanguageTags(Arrays.asList("en", "en", "de-CH", "de-DE", "de-CH"));

    Assert.assertEquals(5, locales.size());
    Assert.assertEquals(Locale.ROOT, locales.get(0));
    Assert.assertEquals(Locale.forLanguageTag("en"), locales.get(1));
    Assert.assertEquals(Locale.forLanguageTag("de"), locales.get(2));
    Assert.assertEquals(Locale.forLanguageTag("de-CH"), locales.get(3));
    Assert.assertEquals(Locale.forLanguageTag("de-DE"), locales.get(4));
  }
}
