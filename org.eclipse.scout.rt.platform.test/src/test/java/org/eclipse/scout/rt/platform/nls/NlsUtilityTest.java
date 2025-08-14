/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class NlsUtilityTest {

  @Test
  public void testMergeTexts_null() {
    assertEquals(
        Map.of(),
        NlsUtility.mergeTexts(null, null));
    assertEquals(
        Map.of("en", "foo"),
        NlsUtility.mergeTexts(Map.of("en", "foo"), null));
    assertEquals(
        Map.of("en", "foo"),
        NlsUtility.mergeTexts(null, Map.of("en", "foo")));
  }

  @Test
  public void testMergeTexts_simple() {
    assertEquals(
        Map.of("en", "foo"),
        NlsUtility.mergeTexts(
            Map.of("en", "foo"),
            Map.of("en", "foo")
        ));
    // 'from' map wins
    assertEquals(
        Map.of("en", "foo"),
        NlsUtility.mergeTexts(
            Map.of("en", "foo"),
            Map.of("en", "bar")
        ));
    assertEquals(
        Map.of("en", "bar"),
        NlsUtility.mergeTexts(
            Map.of("en", "bar"),
            Map.of("en", "foo")
        ));
    // maps are merged
    assertEquals(
        Map.of("en", "foo", "de", "bar"),
        NlsUtility.mergeTexts(
            Map.of("de", "bar"),
            Map.of("en", "foo")
        ));
    assertEquals(
        Map.of("en", "foo", "de", "bar"),
        NlsUtility.mergeTexts(
            Map.of("en", "foo"),
            Map.of("de", "bar")
        ));
  }

  @Test
  public void testMergeTexts_languageTags() {
    // en-US is skipped because the value is the same as en, but en-UK is not skipped
    assertEquals(
        Map.of("en", "foo", "de", "bar", "en-UK", "baz"),
        NlsUtility.mergeTexts(
            Map.of("de", "bar", "en-US", "foo", "en-UK", "baz"),
            Map.of("en", "foo")
        ));
    assertEquals(
        Map.of("en", "foo", "de", "bar", "en-UK", "baz"),
        NlsUtility.mergeTexts(
            Map.of("en", "foo"),
            Map.of("de", "bar", "en-US", "foo", "en-UK", "baz")
        ));
    assertEquals(
        Map.of("en", "foo", "de", "bar", "en-UK", "baz"),
        NlsUtility.mergeTexts(
            Map.of(),
            Map.of("de", "bar", "en", "foo", "en-US", "foo", "en-UK", "baz")
        ));

    // works with language tags with more than two components
    assertEquals(
        Map.of("en", "foo", "de", "bar"),
        NlsUtility.mergeTexts(
            Map.of("de", "bar", "en", "foo", "en-US", "foo", "en-US-xy", "foo"),
            Map.of("en-US", "foo")
        ));
    assertEquals(
        Map.of("en", "foo"),
        NlsUtility.mergeTexts(
            Map.of("en", "foo"),
            Map.of("en-US-xy-z", "foo")
        ));

    // does not ignore entry when it has the same value as a shorter language tag but there is a language tag "in between" with a different value
    assertEquals(
        Map.of("en", "foo", "en-US", "bar", "en-US-xy", "foo"),
        NlsUtility.mergeTexts(
            Map.of("en-US-xy", "foo"),
            Map.of("en", "foo", "en-US", "bar")
        ));

    // splits language tags at '-' character
    assertEquals(
        Map.of("en-US", "foo", "en-U", "bar"),
        NlsUtility.mergeTexts(
            Map.of("en-US", "foo"),
            Map.of("en-U", "bar")
        ));
    assertEquals(
        Map.of("en_US", "foo", "en", "bar"),
        NlsUtility.mergeTexts(
            Map.of("en_US", "foo"),
            Map.of("en", "bar")
        ));
  }

  @Test
  public void testMergeTexts_javaDoc() {
    // assert cases from javadoc
    assertEquals(
        Map.of("de", "groß"),
        NlsUtility.mergeTexts(
            Map.of("de-DE", "groß"),
            Map.of("de", "groß")
        ));
    assertEquals(
        Map.of("de", "groß", "de-CH", "gross"),
        NlsUtility.mergeTexts(
            Map.of("de-CH", "gross"),
            Map.of("de", "groß")
        ));
    assertEquals(
        Map.of("de", "ok", "en", "ok"),
        NlsUtility.mergeTexts(
            Map.of("de", "ok"),
            Map.of("en", "ok")
        ));
  }
}
