/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.text.ITextProviderService;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link HTML} with Texts containing links.
 */
@RunWith(PlatformTestRunner.class)
public class HTMLTextTest {

  private List<IBean<?>> m_testTextService;

  @Before
  public void before() {
    m_testTextService = TestingUtility.registerBeans(
        new BeanMetaData(TestTextProviderService.class)
            .withApplicationScoped(true));
    ScoutTexts.CURRENT.set(new ScoutTexts(BEANS.all(ITextProviderService.class)));
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_testTextService);
    ScoutTexts.CURRENT.remove();
  }

  @Test
  public void testBoldHtmlText() {
    IHtmlContent boldText = HTML.bold(HTML.plain(TEXTS.get("key6", HTML.appLink("REF", "text").toEncodedHtml())));
    assertEquals("<b>value <span class=\"app-link\" data-ref=\"REF\">text</span></b>", boldText.toEncodedHtml());
  }
}
