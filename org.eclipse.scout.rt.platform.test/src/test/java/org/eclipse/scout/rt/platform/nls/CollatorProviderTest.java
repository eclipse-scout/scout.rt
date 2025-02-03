/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import static org.junit.Assert.assertArrayEquals;

import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CollatorProvider}
 */
public class CollatorProviderTest {

  private CollatorProvider m_collatorProvider;

  @Before
  public void before() {
    m_collatorProvider = new CollatorProvider();
  }

  @Test
  public void testSVLocales() {
    String[] names = new String[]{"ö", "z"};
    sort(new Locale("sv"), names);
    assertArrayEquals("Testing sort order", new String[]{"z", "ö"}, names);
  }

  @Test
  public void testLocalesCached() {
    String[] names = new String[]{"ö", "z"};
    sort(new Locale("sv"), names);
    sort(new Locale("sv"), names);
    assertArrayEquals("Testing sort order", new String[]{"z", "ö"}, names);
  }

  @Test
  public void testDifferentLocales() {
    String[] names = new String[]{"ö", "z"};
    sort(new Locale("de"), names);
    sort(new Locale("sv"), names);
    assertArrayEquals("Testing sort order", new String[]{"z", "ö"}, names);
  }

  private void sort(Locale l, String[] array) {
    sort(m_collatorProvider.getInstance(l), array);
  }

  private void sort(final Collator collator, String[] array) {
    Arrays.sort(array, 0, array.length, (a, b) -> collator.compare(a, b));
  }
}
