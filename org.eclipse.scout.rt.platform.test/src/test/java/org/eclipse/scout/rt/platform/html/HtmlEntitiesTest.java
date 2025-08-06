/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings({"ConcatenationWithEmptyString", "SpellCheckingInspection", "TextBlockMigration"})
@RunWith(PlatformTestRunner.class)
public class HtmlEntitiesTest {

  @Test
  public void testUnescapeAll_Empty() {
    final HtmlEntities entities = BEANS.get(HtmlEntities.class);
    assertNull(entities.unescapeAll(null));
    assertEquals("", entities.unescapeAll(""));
    assertEquals("  ", entities.unescapeAll("  "));
  }

  @Test
  public void testUnescapeAll_Named() {
    final HtmlEntities entities = BEANS.get(HtmlEntities.class);
    assertEquals("ß", entities.unescapeAll("&szlig;"));
    assertEquals("Ü", entities.unescapeAll("&Uuml;"));
    assertEquals("&", entities.unescapeAll("&amp;"));
    assertEquals("A&&Z", entities.unescapeAll("A&amp;&amp;Z"));
    assertEquals("&auml;", entities.unescapeAll("&amp;auml;"));
    assertEquals("auml;", entities.unescapeAll("auml;"));
    assertEquals("drag&drop", entities.unescapeAll("drag&drop"));
    assertEquals("&ä", entities.unescapeAll("&&auml;"));
  }

  @Test
  public void testUnescapeAll_NumericDecimal() {
    final HtmlEntities entities = BEANS.get(HtmlEntities.class);
    assertEquals("'", entities.unescapeAll("&#39;"));
    assertEquals("€", entities.unescapeAll("&#8364;"));
    assertEquals("A€€Z", entities.unescapeAll("A&#8364;&#8364;Z"));
  }

  @Test
  public void testUnescapeAll_NumericHex() {
    final HtmlEntities entities = BEANS.get(HtmlEntities.class);
    assertEquals("B", entities.unescapeAll("&#x42;"));
    assertEquals("B", entities.unescapeAll("&#X42;"));
    assertEquals("?", entities.unescapeAll("&#x3F;"));
    assertEquals("?", entities.unescapeAll("&#X3f;"));
    assertEquals("🦕", entities.unescapeAll("&#x1F995;"));
    assertEquals("A🦕🦕Z", entities.unescapeAll("A&#x1F995;&#x1F995;Z"));
  }

  @Test
  public void testUnescapeAll_ComplexStrings() {
    final HtmlEntities entities = BEANS.get(HtmlEntities.class);
    assertEquals("text'text", entities.unescapeAll("text&#39;text"));
    assertEquals("'€", entities.unescapeAll("&#39;&#8364;"));
    assertEquals("/ˈʊmlaʊt/", entities.unescapeAll("/&#x02C8;&#x028A;mla&#x028A;t/"));
    assertEquals("Kühlflüssigkeitsüberlaufbehälter", entities.unescapeAll("K&uuml;hlfl&uuml;ssigkeits&uuml;berlaufbeh&auml;lter"));
    assertEquals(" [Viele Kühe machen Mühe!] ", entities.unescapeAll(" &#X5B;&#86;ie&#x6c;e Kühe machen M&uuml;he&#x21;&#93; "));
    assertEquals("🤸🏾‍♀️", entities.unescapeAll("&#x1F938;&#x1F3FE;&zwj;&#x2640;&#xFE0F;"));
    assertEquals("🏴‍☠️", entities.unescapeAll("&#x1F3F4;&#x200D;&#x2620;&#xFE0F;"));
    assertEquals(""
            + "Face with Tears of Joy Emoji: \uD83D\uDE02\n"
            + "Party Popper Emoji: \uD83C\uDF89\n"
            + "Man Technologist: Medium-light Skin Tone: \uD83D\uDC68\uD83C\uDFFC\u200D\uD83D\uDCBB",
        entities.unescapeAll(""
            + "Face with Tears of Joy Emoji: &#128514;\n"
            + "Party Popper Emoji: &#127881;\n"
            + "Man Technologist: Medium-light Skin Tone: &#128104;&#127996;&zwj;&#128187;"));
    assertEquals("<body><div data-value=\"¡Hola!\">Zürich</div><hr></body>", entities.unescapeAll("<body><div data-value=\"&#161;Hola!\">Z&uuml;rich</div><hr></body>"));
    assertEquals("Qu’est-ce que c’est?", entities.unescapeAll("Qu&#8217;est-ce que c&#8217;est?"));
  }

  @Test
  public void testUnescapeAll_Invalid() {
    final HtmlEntities entities = BEANS.get(HtmlEntities.class);
    assertEquals("ä &doesNotExist; é", entities.unescapeAll("&auml; &doesNotExist; &eacute;")); // invalid entity name
    assertEquals("a &nbsp b", entities.unescapeAll("a &nbsp b")); // not terminated by ';'
    assertEquals("drag&drop", entities.unescapeAll("drag&drop")); // not an entity name
    assertEquals("drag & drop", entities.unescapeAll("drag & drop")); // not an entity name
    assertEquals("& auml;", entities.unescapeAll("& auml;")); // space after '&'
    assertEquals("&auml ;", entities.unescapeAll("&auml ;")); // space before ';'
    assertEquals("&auml,", entities.unescapeAll("&auml,")); // not terminated by ';'
    assertEquals("&#1234567890;", entities.unescapeAll("&#1234567890;")); // not a valid code point
    assertEquals("&39;", entities.unescapeAll("&39;")); // missing '#'
    assertEquals("&#a0;", entities.unescapeAll("&#a0;")); // missing 'x'
  }

  @Test
  public void testDecodeNamedCharacterReference() {
    final HtmlEntities entities = BEANS.get(HtmlEntities.class);

    assertEquals("ä", entities.decodeNamedCharacterReference("&auml;"));
    assertEquals("'", entities.decodeNamedCharacterReference("&apos;"));

    assertNull(entities.decodeNamedCharacterReference(null));
    assertNull(entities.decodeNamedCharacterReference(""));
    assertNull(entities.decodeNamedCharacterReference("&doesNotExist;"));
    assertNull(entities.decodeNamedCharacterReference("&auml;&uuml;"));
    assertNull(entities.decodeNamedCharacterReference("auml"));
    assertNull(entities.decodeNamedCharacterReference("&#39;"));
  }

  @Test
  public void testDecodeNumericCharacterReference() {
    final HtmlEntities entities = BEANS.get(HtmlEntities.class);

    assertEquals("ä", entities.decodeNumericCharacterReference("&#228;"));
    assertEquals("ä", entities.decodeNumericCharacterReference("&#xE4;"));
    assertEquals("'", entities.decodeNumericCharacterReference("&#39;"));
    assertEquals("'", entities.decodeNumericCharacterReference("&#x27;"));

    assertNull(entities.decodeNumericCharacterReference(null));
    assertNull(entities.decodeNumericCharacterReference(""));
    assertNull(entities.decodeNumericCharacterReference("&doesNotExist;"));
    assertNull(entities.decodeNumericCharacterReference("&#228;&#39;"));
    assertNull(entities.decodeNumericCharacterReference("#39;"));
    assertNull(entities.decodeNumericCharacterReference("&#E4;"));
    assertNull(entities.decodeNumericCharacterReference("&#hello;"));
    assertNull(entities.decodeNumericCharacterReference("&#1234567890;"));
  }
}
