/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.nls;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NaturalCollatorProvider;
import org.junit.Test;

public class NaturalCollatorProviderTest {

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

    Locale locale = new Locale("en");
    List<String> actualResult = new ArrayList<>(input);
    Collections.sort(actualResult, BEANS.get(NaturalCollatorProvider.class).getInstance(locale));

    assertEquals(expectedResult, actualResult);
  }
}
