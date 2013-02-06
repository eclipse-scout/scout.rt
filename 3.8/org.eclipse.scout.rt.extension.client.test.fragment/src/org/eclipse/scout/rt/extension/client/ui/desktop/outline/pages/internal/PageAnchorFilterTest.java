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
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.9.0
 */
@RunWith(ScoutClientTestRunner.class)
public class PageAnchorFilterTest {

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
  public void testAcceptEmpty() {
    PageAnchorFilter filter = new PageAnchorFilter(null, null);
    assertTrue(filter.accept(null, null, null));
    assertTrue(filter.accept(m_outline, null, null));
    assertTrue(filter.accept(null, m_page, null));
    assertTrue(filter.accept(m_outline, m_page, null));
  }

  @Test
  public void testAcceptOutlineFilterClass() {
    PageAnchorFilter filter = new PageAnchorFilter(P_Outline.class, null);
    assertFalse(filter.accept(null, null, null));
    assertTrue(filter.accept(m_outline, null, null));
    assertFalse(filter.accept(m_otherOutline, null, null));
    assertFalse(filter.accept(null, m_page, null));
    assertFalse(filter.accept(m_outline, m_page, null));
  }

  @Test
  public void testAcceptPageFilterClass() {
    PageAnchorFilter filter = new PageAnchorFilter(null, P_Page.class);
    assertFalse(filter.accept(null, null, null));
    assertFalse(filter.accept(m_outline, null, null));
    assertTrue(filter.accept(null, m_page, null));
    assertFalse(filter.accept(null, m_otherPage, null));
    assertTrue(filter.accept(m_outline, m_page, null));
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
