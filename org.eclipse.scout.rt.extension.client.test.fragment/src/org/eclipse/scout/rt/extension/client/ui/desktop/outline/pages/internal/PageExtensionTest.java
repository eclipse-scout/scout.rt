/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageExtensionFilter;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link AbstractPageExtension}.
 * <p/>
 * <b>Note</b>: This class must not be called <em>Abstract</em>PageExtensionTest because the JUnit test case browser
 * ignores classes starting with <em>Abstract</em>.
 * 
 * @since 3.9.0
 */
public class PageExtensionTest {

  private P_Outline m_outline;
  private P_OtherOutline m_otherOutline;
  private P_Page m_page;
  private P_OtherPage m_otherPage;

  @Before
  public void setup() throws Exception {
    m_outline = new P_Outline();
    m_otherOutline = new P_OtherOutline();
    m_page = new P_Page();
    m_otherPage = new P_OtherPage();
  }

  @Test
  public void testAcceptContextNoContextFilter() throws Exception {
    P_PageExtension pageExtension = new P_PageExtension(null);
    assertTrue(pageExtension.accept(null, null, null));
    assertTrue(pageExtension.accept(m_outline, null, null));
    assertTrue(pageExtension.accept(m_outline, m_page, null));
    assertTrue(pageExtension.accept(m_outline, m_otherPage, null));
    assertTrue(pageExtension.accept(m_otherOutline, m_page, null));
    assertTrue(pageExtension.accept(m_otherOutline, m_otherPage, null));
  }

  @Test
  public void testAcceptContextOutlineContextFilter() throws Exception {
    P_PageExtension pageExtension = new P_PageExtension(new ParentAndOutlinePageFilter(P_Outline.class, null));
    assertFalse(pageExtension.accept(null, null, null));
    assertTrue(pageExtension.accept(m_outline, null, null));
    assertFalse(pageExtension.accept(m_otherOutline, null, null));

    assertFalse(pageExtension.accept(m_outline, m_page, null));
    assertFalse(pageExtension.accept(m_outline, m_otherPage, null));

    assertFalse(pageExtension.accept(m_otherOutline, m_page, null));
    assertFalse(pageExtension.accept(m_otherOutline, m_otherPage, null));
  }

  @Test
  public void testAcceptContextPageContextFilter() throws Exception {
    P_PageExtension m_pageExtension = new P_PageExtension(new ParentAndOutlinePageFilter(null, P_Page.class));
    assertFalse(m_pageExtension.accept(null, null, null));
    assertFalse(m_pageExtension.accept(m_outline, null, null));
    assertFalse(m_pageExtension.accept(m_otherOutline, null, null));

    assertTrue(m_pageExtension.accept(m_outline, m_page, null));
    assertFalse(m_pageExtension.accept(m_outline, m_otherPage, null));

    assertTrue(m_pageExtension.accept(m_otherOutline, m_page, null));
    assertFalse(m_pageExtension.accept(m_otherOutline, m_otherPage, null));
  }

  private static class P_PageExtension extends AbstractPageExtension {
    public P_PageExtension(IPageExtensionFilter pageFilter) {
      super(pageFilter);
    }
  }

  private static class P_Outline extends AbstractOutline {
  }

  private static class P_OtherOutline extends AbstractOutline {
  }

  private static class P_Page extends AbstractPageWithNodes {
  }

  private static class P_OtherPage extends AbstractPageWithNodes {
  }
}
