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
import static org.junit.Assert.assertNotNull;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.9.0
 */
@RunWith(ScoutClientTestRunner.class)
public class PageContributionExtensionCreateInstanceTest {

  private P_Outline m_outline;
  private P_OtherOutline m_otherOutline;
  private P_ParentPage m_parentPage;
  private P_OtherParentPage m_otherParentPage;

  @Before
  public void before() {
    m_outline = new P_Outline();
    m_otherOutline = new P_OtherOutline();
    m_parentPage = new P_ParentPage();
    m_otherParentPage = new P_OtherParentPage();
  }

  @Test
  public void testCreateContributionDefaultConstructor() throws Exception {
    assertCreateContribution(Constructor.Default, DefaultConstructorTestPage.class, m_outline, null);
    assertCreateContribution(Constructor.Default, DefaultConstructorTestPage.class, m_outline, m_parentPage);
    assertCreateContribution(Constructor.Default, DefaultConstructorTestPage.class, m_otherOutline, m_otherParentPage);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateContributionMissingParameterOutline() throws Exception {
    assertCreateContribution(null, DefaultConstructorTestPage.class, null, null);
  }

  /////////////////////////////////////////////////////////
  // test constructor with parent page                   //
  /////////////////////////////////////////////////////////

  @Test
  public void testCreateContributionParentPageConstructor() throws Exception {
    assertCreateContribution(Constructor.Default, PageConstructorTestPage.class, m_outline, null);
    assertCreateContribution(Constructor.ParentPage, PageConstructorTestPage.class, m_outline, m_parentPage);
    assertCreateContribution(Constructor.Default, PageConstructorTestPage.class, m_otherOutline, m_otherParentPage);
  }

  @Test(expected = ProcessingException.class)
  public void testCreateContributionParentPageMissingConstructor() throws Exception {
    assertCreateContribution(null, OtherPageConstructorTestPage.class, m_outline, m_parentPage);
  }

  @Test
  public void testCreateContributionOtherParentPageConstructor() throws Exception {
    assertCreateContribution(Constructor.OtherParentPage, OtherPageConstructorTestPage.class, m_outline, m_otherParentPage);
    assertCreateContribution(Constructor.OtherParentPage, OtherPageConstructorTestPage.class, m_outline, null);
  }

  @Test
  public void testCreateContributionOthrParentPageConstructorMissingConstructor() throws Exception {
    // here the outline constructor is used because the page's type cannot be determined
    assertCreateContribution(Constructor.OtherOutline, OtherOutlineConstructorTestPage.class, m_outline, null);
  }

  @Test
  public void testCreateContributionParentPageAndOtherParentPageConstructor() throws Exception {
    assertCreateContribution(Constructor.ParentPage, PageAndOtherPageConstructorTestPage.class, m_outline, m_parentPage);
    assertCreateContribution(Constructor.OtherParentPage, PageAndOtherPageConstructorTestPage.class, m_outline, m_otherParentPage);
  }

  /////////////////////////////////////////////////////////
  // test constructor with outline                       //
  /////////////////////////////////////////////////////////

  @Test
  public void testCreateContributionOutlineConstructor() throws Exception {
    assertCreateContribution(Constructor.Outline, OutlineConstructorTestPage.class, m_outline, null);
    assertCreateContribution(Constructor.Outline, OutlineConstructorTestPage.class, m_outline, m_parentPage);
    assertCreateContribution(Constructor.Default, OutlineConstructorTestPage.class, m_otherOutline, m_otherParentPage);
  }

  @Test(expected = ProcessingException.class)
  public void testCreateContributionOutlineMissingConstructor() throws Exception {
    assertCreateContribution(null, OtherOutlineConstructorTestPage.class, m_outline, m_parentPage);
  }

  @Test
  public void testCreateContributionOtherOutlineConstructor() throws Exception {
    assertCreateContribution(Constructor.OtherOutline, OtherOutlineConstructorTestPage.class, m_otherOutline, m_otherParentPage);
  }

  /////////////////////////////////////////////////////////
  // test constructor with parent page and outline       //
  /////////////////////////////////////////////////////////

  @Test
  public void testCreateContributionParentPageAndOutlineConstructor() throws Exception {
    assertCreateContribution(Constructor.ParentPage, ParentPageAndOutlineConstructorTestPage.class, m_outline, m_parentPage);
    assertCreateContribution(Constructor.ParentPageOutline, ParentPageAndOutlineConstructorTestPage.class, m_outline, null);
    assertCreateContribution(Constructor.ParentPage, ParentPageAndOutlineConstructorTestPage.class, m_otherOutline, m_parentPage);
    assertCreateContribution(Constructor.Default, ParentPageAndOutlineConstructorTestPage.class, m_otherOutline, null);
    assertCreateContribution(Constructor.Default, ParentPageAndOutlineConstructorTestPage.class, m_otherOutline, m_otherParentPage);
  }

  @Test
  public void testCreateContributionParentPageAndOtherOutlineConstructor() throws Exception {
    assertCreateContribution(Constructor.ParentPage, ParentPageAndOtherOutlineConstructorTestPage.class, m_otherOutline, m_parentPage);
    assertCreateContribution(Constructor.OtherOutlineParentPage, ParentPageAndOtherOutlineConstructorTestPage.class, m_otherOutline, null);
    assertCreateContribution(Constructor.ParentPage, ParentPageAndOtherOutlineConstructorTestPage.class, m_outline, m_parentPage);
  }

  @Test(expected = ProcessingException.class)
  public void testCreateContributionParentPageAndOtherOutlineMissingConstructor() throws Exception {
    assertCreateContribution(null, ParentPageAndOtherOutlineConstructorTestPage.class, m_otherOutline, m_otherParentPage);
  }

  private static void assertCreateContribution(Constructor expectedConstructor, Class<? extends AbstractConstructorTestPage> pageClass, IOutline outline, IPageWithNodes parentPage) throws ProcessingException {
    PageContributionExtension ext = new PageContributionExtension(pageClass, 10);
    IPage page = ext.createContribution(outline, parentPage);
    assertNotNull(page);
    assertEquals(ext.getPageClass(), page.getClass());
    assertEquals(expectedConstructor, ((AbstractConstructorTestPage) page).m_constructedBy);
  }

  public static class P_Outline extends AbstractOutline {
  }

  public static class P_OtherOutline extends AbstractOutline {
  }

  public static class P_ParentPage extends AbstractPageWithNodes {
  }

  public static class P_OtherParentPage extends AbstractPageWithNodes {
  }

  public enum Constructor {
    Default,
    Outline,
    OtherOutline,
    ParentPage,
    OtherParentPage,
    OutlineParentPage,
    ParentPageOutline,
    OtherOutlineParentPage
  }

  public abstract static class AbstractConstructorTestPage extends AbstractPageWithNodes {
    public Constructor m_constructedBy;
  }

  public static class DefaultConstructorTestPage extends AbstractConstructorTestPage {

    public DefaultConstructorTestPage() {
      m_constructedBy = Constructor.Default;
    }
  }

  /////////////////////////////////////////////////////////
  // parent constructor page test classes                //
  /////////////////////////////////////////////////////////

  public static class PageConstructorTestPage extends AbstractConstructorTestPage {

    public PageConstructorTestPage() {
      m_constructedBy = Constructor.Default;
    }

    public PageConstructorTestPage(P_ParentPage parentPage) {
      m_constructedBy = Constructor.ParentPage;
    }
  }

  public static class OtherPageConstructorTestPage extends AbstractConstructorTestPage {

    public OtherPageConstructorTestPage(P_OtherParentPage parentPage) {
      m_constructedBy = Constructor.OtherParentPage;
    }
  }

  public static class PageAndOtherPageConstructorTestPage extends AbstractConstructorTestPage {

    public PageAndOtherPageConstructorTestPage(P_ParentPage parentPage) {
      m_constructedBy = Constructor.ParentPage;
    }

    public PageAndOtherPageConstructorTestPage(P_OtherParentPage parentPage) {
      m_constructedBy = Constructor.OtherParentPage;
    }
  }

  /////////////////////////////////////////////////////////
  // outline constructor test classes                    //
  /////////////////////////////////////////////////////////

  public static class OutlineConstructorTestPage extends AbstractConstructorTestPage {

    public OutlineConstructorTestPage() {
      m_constructedBy = Constructor.Default;
    }

    public OutlineConstructorTestPage(P_Outline outline) {
      m_constructedBy = Constructor.Outline;
    }
  }

  public static class OtherOutlineConstructorTestPage extends AbstractConstructorTestPage {

    public OtherOutlineConstructorTestPage(P_OtherOutline outline) {
      m_constructedBy = Constructor.OtherOutline;
    }
  }

  /////////////////////////////////////////////////////////
  // page and outline constructor test classes           //
  /////////////////////////////////////////////////////////

  public static class ParentPageAndOutlineConstructorTestPage extends AbstractConstructorTestPage {

    public ParentPageAndOutlineConstructorTestPage() {
      m_constructedBy = Constructor.Default;
    }

    public ParentPageAndOutlineConstructorTestPage(P_ParentPage parentPage) {
      m_constructedBy = Constructor.ParentPage;
    }

    public ParentPageAndOutlineConstructorTestPage(P_ParentPage parentPage, P_Outline outline) {
      m_constructedBy = Constructor.ParentPageOutline;
    }
  }

  public static class ParentPageAndOtherOutlineConstructorTestPage extends AbstractConstructorTestPage {

    public ParentPageAndOtherOutlineConstructorTestPage(P_ParentPage parentPage) {
      m_constructedBy = Constructor.ParentPage;
    }

    public ParentPageAndOtherOutlineConstructorTestPage(P_OtherOutline outline, P_ParentPage parentPage) {
      m_constructedBy = Constructor.OtherOutlineParentPage;
    }
  }

}
