/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.text;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link HTML} with Texts containing links.
 */
@RunWith(PlatformTestRunner.class)
public class HTMLTextTest {

  @Test
  public void testBoldHtmlText() {
    IHtmlContent boldText = HTML.bold(HTML.raw(TEXTS.get("key6", HTML.appLink("REF", "text").toHtml())));
    assertEquals("<b>value <span class=\"app-link\" data-ref=\"REF\">text</span></b>", boldText.toHtml());
  }
}
