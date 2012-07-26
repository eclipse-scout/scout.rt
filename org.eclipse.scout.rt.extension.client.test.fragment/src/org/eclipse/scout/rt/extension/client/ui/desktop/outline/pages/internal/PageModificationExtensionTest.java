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
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageModifier;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.9.0
 */
@RunWith(ScoutClientTestRunner.class)
public class PageModificationExtensionTest {

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

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullPageClass() {
    new PageModificationExtension(null, P_PageModifier.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullPageModifier() {
    new PageModificationExtension(P_Page.class, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorUnassinablePageClassAndGenericPageOnPageModification() {
    new PageModificationExtension(P_OtherPage.class, P_PageModifier.class);
  }

  @Test
  public void testCreatePageModifier() throws Exception {
    PageModificationExtension pageModification = new PageModificationExtension(P_Page.class, P_PageModifier.class);
    IPageModifier<? extends IPage> pageModifier = pageModification.createPageModifier();
    assertNotNull(pageModifier);
    assertEquals(P_PageModifier.class, pageModifier.getClass());
  }

  @Test
  public void testCreatePageModifierConstructorThrowingException() throws Exception {
    PageModificationExtension pageModification = new PageModificationExtension(P_Page.class, P_PageModifierConstructorThrowingException.class);
    pageModification.createPageModifier();
  }

  @Test(expected = ProcessingException.class)
  public void testCreatePageModifierConstructorWithParameter() throws Exception {
    PageModificationExtension pageModification = new PageModificationExtension(P_Page.class, P_PageModifierConstructorWithParameter.class);
    pageModification.createPageModifier();
  }

  @Test
  public void testAcceptWithoutContext() {
    PageModificationExtension pageModification = new PageModificationExtension(P_Page.class, P_PageModifier.class);

    assertFalse(pageModification.accept(null, null, null));
    assertTrue(pageModification.accept(null, null, m_page));
    assertTrue(pageModification.accept(m_outline, null, m_page));
    assertTrue(pageModification.accept(m_outline, m_otherPage, m_page));

    assertFalse(pageModification.accept(null, null, m_otherPage));
  }

  public static class P_PageModifier implements IPageModifier<P_Page> {

    private IOutline m_outline;
    private IPage m_parentPage;
    private IPage m_page;

    @Override
    public void modify(IOutline outline, IPage parentPage, P_Page page) throws ProcessingException {
      m_outline = outline;
      m_parentPage = parentPage;
      m_page = page;
    }
  }

  public static class P_PageModifierConstructorThrowingException implements IPageModifier<IPage> {

    public P_PageModifierConstructorThrowingException() throws Exception {
    }

    @Override
    public void modify(IOutline outline, IPage parentPage, IPage page) throws ProcessingException {
    }
  }

  public static class P_PageModifierConstructorWithParameter implements IPageModifier<IPage> {

    public P_PageModifierConstructorWithParameter(String s) throws Exception {
    }

    @Override
    public void modify(IOutline outline, IPage parentPage, IPage page) throws ProcessingException {
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
