/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.gen.extract;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link FallbackTextExtractor}
 */
public class FallbackTextExtractorTest {

  private P_TestExtractor m_extractor1 = new P_TestExtractor();
  private P_TestExtractor m_extractor2 = new P_TestExtractor();
  private FallbackTextExtractor<Object> m_fallbackExtracor = new FallbackTextExtractor<Object>(m_extractor1, m_extractor2);

  @Test
  public void testAllCombinations() {
    testFallbackTextExtractor(null, null, null, null, null, null);
    testFallbackTextExtractor(null, "h2", "h2", null, null, null);
    testFallbackTextExtractor("h1", null, "h1", null, null, null);
    testFallbackTextExtractor("h1", "h2", "h1", null, null, null);

    testFallbackTextExtractor(null, null, null, null, "v2", "v2");
    testFallbackTextExtractor(null, "h2", "h2", null, "v2", "v2");
    testFallbackTextExtractor("h1", null, "h1", null, "v2", "v2");
    testFallbackTextExtractor("h1", "h2", "h1", null, "v2", "v2");

    testFallbackTextExtractor(null, null, null, "v1", null, "v1");
    testFallbackTextExtractor(null, "h2", "h2", "v1", null, "v1");
    testFallbackTextExtractor("h1", null, "h1", "v1", null, "v1");
    testFallbackTextExtractor("h1", "h2", "h1", "v1", null, "v1");

    testFallbackTextExtractor(null, null, null, "v1", "v2", "v1");
    testFallbackTextExtractor(null, "h2", "h2", "v1", "v2", "v1");
    testFallbackTextExtractor("h1", null, "h1", "v1", "v2", "v1");
    testFallbackTextExtractor("h1", "h2", "h1", "v1", "v2", "v1");

  }

  private void testFallbackTextExtractor(String firstHeader, String secondHeader, String expectedHeader, String firstText, String secondText, String expectedText) {
    m_extractor1.m_header = firstHeader;
    m_extractor2.m_header = secondHeader;
    m_extractor1.m_text = firstText;
    m_extractor2.m_text = secondText;
    Assert.assertEquals("Return of getHeader() not as expected.", expectedHeader, m_fallbackExtracor.getHeader());
    Assert.assertEquals("Return of getText(...) not as expected.", expectedText, m_fallbackExtracor.getText(new Object()));
  }

  class P_TestExtractor implements IDocTextExtractor<Object> {

    String m_text;
    String m_header;

    @Override
    public String getHeader() {
      return m_header;
    }

    @Override
    public String getText(Object object) {
      return m_text;
    }

  }
}
