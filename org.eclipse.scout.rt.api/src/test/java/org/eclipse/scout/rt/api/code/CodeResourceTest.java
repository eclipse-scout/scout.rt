/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.code;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.api.data.code.CodeDo;
import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public class CodeResourceTest {
  @Test
  public void testConvertToMap() {
    CodeTypeDo codeType1 = BEANS.get(CodeTypeDo.class)
        .withId("1");
    CodeTypeDo codeType2 = BEANS.get(CodeTypeDo.class)
        .withId("2");
    CodeTypeDo codeType3 = BEANS.get(CodeTypeDo.class); // without ID
    CodeTypeDo codeType4 = BEANS.get(CodeTypeDo.class)
        .withId("1"); // duplicate ID is skipped

    Map<String, CodeTypeDo> merged = BEANS.get(CodeResource.class).convertToMap(Arrays.asList(codeType1, codeType2, null, codeType3, codeType4));
    assertEquals(Set.of("1", "2"), merged.keySet());
    assertSame(merged.get("1"), codeType4); // codeType4 overwrites codeType1
    assertSame(merged.get("2"), codeType2);
  }

  @Test
  public void testMergeCodeTypeTexts() {
    CodeTypeDo codeType1 = BEANS.get(CodeTypeDo.class)
        .withId("1")
        .withText("en", "t1")
        .withTextPlural("en", "p1")
        .withCodes(BEANS.get(CodeDo.class)
            .withId("1.1")
            .withText("en", "t1.1"),
            BEANS.get(CodeDo.class)
                .withId("1.2")
                .withText("en", "t1.2"));
    CodeTypeDo codeType2 = BEANS.get(CodeTypeDo.class)
        .withId("2");

    CodeTypeDo german = BEANS.get(CodeTypeDo.class)
        .withId("1")
        .withText("en", "t1-updated")
        .withTextPlural("de", "p1-de")
        .withTextPlural("en-US", "p1")
        .withTextPlural("en-UK", "p1-uk")
        .withCodes(BEANS.get(CodeDo.class)
            .withId("1.1")
                .withText("de", "t1.1-de")
                .withText("en-US", "t1.1-us")
                .withText("en-UK", "t1.1"),
            BEANS.get(CodeDo.class)
                .withId("1.2")
                .withText("en", "t1.2-updated"));

    Map<String, CodeTypeDo> map = BEANS.get(CodeResource.class).convertToMap(Arrays.asList(codeType1, codeType2));
    BEANS.get(CodeResource.class).mergeCodeTypeTexts(german, map);

    CodeTypeDo merged = map.get("1");
    Map<String, String> texts = merged.getTexts();
    assertEquals(Map.of("en", "t1-updated"), texts);
    Map<String, String> textsPlural = merged.getTextsPlural();
    assertEquals(Map.of("en", "p1", "de", "p1-de", "en-UK", "p1-uk"), textsPlural);
    Map<String, String> code1Texts = merged.getCodes().get(0).getTexts();
    assertEquals(Map.of("en", "t1.1", "de", "t1.1-de", "en-US", "t1.1-us"), code1Texts);
    Map<String, String> code2Texts = merged.getCodes().get(1).getTexts();
    assertEquals(Map.of("en", "t1.2-updated"), code2Texts);
  }
}
