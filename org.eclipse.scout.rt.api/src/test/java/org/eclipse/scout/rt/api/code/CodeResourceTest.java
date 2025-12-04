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
import java.util.stream.IntStream;

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

  @Test
  public void testDuplicateCodeId() {
    CodeTypeDo en = BEANS.get(CodeTypeDo.class).withId("1").withText("en", "en1").withTextPlural("enp1", "en1");
    en.getCodes().add(BEANS.get(CodeDo.class).withId("2").withText("en", "en2"));
    en.getCodes().add(BEANS.get(CodeDo.class).withId("3").withText("en", "en3"));
    en.getCodes().add(BEANS.get(CodeDo.class).withId("2").withText("en", "en2"));

    CodeTypeDo de = BEANS.get(CodeTypeDo.class).withId("1").withText("de", "de1").withTextPlural("dep1", "de1");
    de.getCodes().add(BEANS.get(CodeDo.class).withId("2").withText("de", "de2"));
    de.getCodes().add(BEANS.get(CodeDo.class).withId("3").withText("de", "de3"));
    de.getCodes().add(BEANS.get(CodeDo.class).withId("2").withText("de", "de2"));

    BEANS.get(CodeResource.class).mergeCodeTypeTexts(de, Map.of("1", en));
  }

  @Test(timeout = 10_000)
  public void testMergeCodeTypeTextsLarge() {
    CodeTypeDo en = createLargeCodeType("en");
    CodeTypeDo de = createLargeCodeType("de");
    Map<String, CodeTypeDo> map = BEANS.get(CodeResource.class).convertToMap(Arrays.asList(en));
    for (int i = 0; i < 8; i++) {
      BEANS.get(CodeResource.class).mergeCodeTypeTexts(de, map);
    }
  }

  protected CodeTypeDo createLargeCodeType(String lang) {
    CodeTypeDo largeCodeType = BEANS.get(CodeTypeDo.class)
        .withId("1")
        .withText(lang, lang + "1")
        .withText(lang + "a", lang + "1")
        .withText(lang + "b", lang + "1")
        .withText(lang + "c", lang + "1")
        .withText(lang + "d", lang + "1")
        .withText(lang + "e", lang + "1")
        .withText(lang + "f", lang + "1")
        .withTextPlural(lang, lang + "p1")
        .withTextPlural(lang + "a", lang + "1")
        .withTextPlural(lang + "b", lang + "1")
        .withTextPlural(lang + "c", lang + "1")
        .withTextPlural(lang + "d", lang + "1")
        .withTextPlural(lang + "e", lang + "1")
        .withTextPlural(lang + "f", lang + "1");
    IntStream.rangeClosed(2, 100_000)
        .mapToObj(Integer::toString)
        .map(id -> BEANS.get(CodeDo.class).withId(id).withText(lang, lang + "t" + id))
        .forEach(c -> largeCodeType.getCodes().add(c));
    return largeCodeType;
  }
}
