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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

public class NaturalCollatorProviderTest {

  private NaturalCollatorProvider m_collatorProvider;

  @Before
  public void before() {
    m_collatorProvider = new NaturalCollatorProvider();
  }

  /**
   * Tests <a href= "https://bugs.eclipse.org/bugs/show_bug.cgi?id=390097">bug 390097</a>
   */
  @Test
  public void testSpacesAndHyphens() {
    List<String> input = Arrays.asList(
        "abc_PT",
        "abc-mno1",
        "ABC MNO2",
        "abC NOP",
        "ABCOP",
        "abcopp",
        "ABC PQR",
        "abc PT",
        "ABC-PT");

    List<String> expectedResult = Arrays.asList(
        "abc-mno1",
        "ABC MNO2",
        "abC NOP",
        "ABC PQR",
        "abc PT",
        "ABC-PT",
        "abc_PT",
        "ABCOP",
        "abcopp");

    List<String> actualResult = new ArrayList<>(input);
    Collections.sort(actualResult, m_collatorProvider.getInstance(new Locale("en")));

    assertEquals(expectedResult, actualResult);
  }

  /**
   * Tests the example from the release notes.
   */
  @Test
  public void testDogs() {
    List<String> input = Arrays.asList(
        "The dogs bark",
        "The dog barks",
        "The dog sleeps");

    List<String> expectedResult = Arrays.asList(
        "The dog barks",
        "The dog sleeps",
        "The dogs bark");

    List<String> actualResult = new ArrayList<>(input);
    Collections.sort(actualResult, m_collatorProvider.getInstance(new Locale("en")));

    assertEquals(expectedResult, actualResult);
  }
}
