/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.text;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.shared.TEXTS;
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
