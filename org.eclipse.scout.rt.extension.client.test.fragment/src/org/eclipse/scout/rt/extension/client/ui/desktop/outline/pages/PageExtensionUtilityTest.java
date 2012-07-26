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
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageContributionExtension;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageModificationExtension;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageRemoveExtension;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.9.0
 */
@RunWith(ScoutClientTestRunner.class)
public class PageExtensionUtilityTest {

  private List<IPage> m_pageList;
  private P_ConfiguredPageA m_configuredPageA;
  private P_ConfiguredPageB m_configuredPageB;
  private P_ConfiguredPageC m_configuredPageC;
  private P_Outline m_outline;

  @Before
  public void before() {
    P_ConfiguredPageAModifier.s_counter = 0;
    P_ConfiguredPageCModifier.s_counter = 0;

    m_pageList = new ArrayList<IPage>();
    m_configuredPageA = new P_ConfiguredPageA();
    m_configuredPageB = new P_ConfiguredPageB();
    m_configuredPageC = new P_ConfiguredPageC();
    m_outline = new P_Outline();
  }

  /////////////////////////////////////////////////////////
  // contribute                                          //
  /////////////////////////////////////////////////////////

  @Test
  public void testContributePagesNullAndEmpty() throws Exception {
    PageExtensionUtility.contributePages(null, null, null, m_pageList);
    assertTrue(m_pageList.isEmpty());
    PageExtensionUtility.contributePages(null, null, Collections.<PageContributionExtension> emptyList(), m_pageList);
    assertTrue(m_pageList.isEmpty());
  }

  @Test
  public void testContributePagesWithoutFilter() throws Exception {
    m_pageList.add(m_configuredPageA);
    m_pageList.add(m_configuredPageB);
    m_pageList.add(m_configuredPageC);

    List<PageContributionExtension> extensions = Arrays.asList(
        new PageContributionExtension(P_ContribPageA.class, 10d),
        new PageContributionExtension(P_ContribPageB.class, 0d),
        new PageContributionExtension(P_ContribPageC.class, 10d));

    PageExtensionUtility.contributePages(m_outline, null, extensions, m_pageList);

    assertEquals(6, m_pageList.size());
    assertSame(P_ContribPageB.class, m_pageList.get(0).getClass());
    assertSame(m_configuredPageA, m_pageList.get(1));
    assertSame(P_ContribPageA.class, m_pageList.get(2).getClass());
    assertSame(P_ContribPageC.class, m_pageList.get(3).getClass());
    assertSame(m_configuredPageB, m_pageList.get(4));
    assertSame(m_configuredPageC, m_pageList.get(5));
  }

  @Test
  public void testContributePagesWithFilter() throws Exception {
    m_pageList.add(m_configuredPageA);
    m_pageList.add(m_configuredPageB);
    m_pageList.add(m_configuredPageC);

    IPageExtensionFilter pageFilter = new IPageExtensionFilter() {
      @Override
      public boolean accept(IOutline outline, IPage parentPage, IPage affectedPage) {
        return false;
      }
    };

    List<PageContributionExtension> extensions = Arrays.asList(
        new PageContributionExtension(pageFilter, P_ContribPageA.class, 10d),
        new PageContributionExtension(P_ContribPageB.class, 0d),
        new PageContributionExtension(P_ContribPageC.class, 10d));

    PageExtensionUtility.contributePages(m_outline, null, extensions, m_pageList);

    assertEquals(5, m_pageList.size());
    assertSame(P_ContribPageB.class, m_pageList.get(0).getClass());
    assertSame(m_configuredPageA, m_pageList.get(1));
    assertSame(P_ContribPageC.class, m_pageList.get(2).getClass());
    assertSame(m_configuredPageB, m_pageList.get(3));
    assertSame(m_configuredPageC, m_pageList.get(4));
  }

  @Test
  public void testContributePagesThrowingException() throws Exception {
    m_pageList.add(m_configuredPageA);
    m_pageList.add(m_configuredPageB);
    m_pageList.add(m_configuredPageC);

    List<PageContributionExtension> extensions = Arrays.asList(
        new PageContributionExtension(P_ContribPageA.class, 10d),
        new PageContributionExtension(P_ContribPageB.class, 0d) {
          @Override
          public IPage createContribution(IOutline outline, IPageWithNodes parentPage) throws ProcessingException {
            throw new UnsupportedOperationException("for testing purposes only");
          }
        },
        new PageContributionExtension(P_ContribPageC.class, 10d));

    PageExtensionUtility.contributePages(m_outline, null, extensions, m_pageList);

    assertEquals(5, m_pageList.size());
    assertSame(m_configuredPageA, m_pageList.get(0));
    assertSame(P_ContribPageA.class, m_pageList.get(1).getClass());
    assertSame(P_ContribPageC.class, m_pageList.get(2).getClass());
    assertSame(m_configuredPageB, m_pageList.get(3));
    assertSame(m_configuredPageC, m_pageList.get(4));
  }

  /////////////////////////////////////////////////////////
  // remove                                              //
  /////////////////////////////////////////////////////////

  @Test
  public void testRemovePagesNullAndEmpty() throws Exception {
    PageExtensionUtility.removePages(null, null, null, m_pageList);
    assertTrue(m_pageList.isEmpty());
    PageExtensionUtility.removePages(null, null, Collections.<PageRemoveExtension> emptyList(), m_pageList);
    assertTrue(m_pageList.isEmpty());
  }

  @Test
  public void testRemovePagesWithoutFilter() throws Exception {
    m_pageList.add(m_configuredPageA);
    m_pageList.add(m_configuredPageB);
    m_pageList.add(m_configuredPageC);

    List<PageRemoveExtension> extensions = Arrays.asList(
        new PageRemoveExtension(P_ContribPageA.class),
        new PageRemoveExtension(P_ConfiguredPageB.class));

    PageExtensionUtility.removePages(null, null, extensions, m_pageList);

    assertEquals(2, m_pageList.size());
    assertSame(m_configuredPageA, m_pageList.get(0));
    assertSame(m_configuredPageC, m_pageList.get(1));
  }

  @Test
  public void testRemovePagesWithFilter() throws Exception {
    m_pageList.add(m_configuredPageA);
    m_pageList.add(m_configuredPageB);
    m_pageList.add(m_configuredPageC);

    IPageExtensionFilter pageFilter = new IPageExtensionFilter() {
      @Override
      public boolean accept(IOutline outline, IPage parentPage, IPage affectedPage) {
        assertNull(outline);
        assertNull(parentPage);
        assertSame(m_configuredPageC, affectedPage);
        return false;
      }
    };

    List<PageRemoveExtension> extensions = Arrays.asList(
        new PageRemoveExtension(P_ContribPageA.class),
        new PageRemoveExtension(P_ConfiguredPageB.class),
        new PageRemoveExtension(pageFilter, P_ConfiguredPageC.class));

    PageExtensionUtility.removePages(null, null, extensions, m_pageList);

    assertEquals(2, m_pageList.size());
    assertSame(m_configuredPageA, m_pageList.get(0));
    assertSame(m_configuredPageC, m_pageList.get(1));
  }

  /////////////////////////////////////////////////////////
  // modify                                              //
  /////////////////////////////////////////////////////////
  public void testModifyPagesNullAndEmpty() throws Exception {
    PageExtensionUtility.modifyPages(null, null, null, m_pageList);
    assertTrue(m_pageList.isEmpty());
    PageExtensionUtility.modifyPages(null, null, Collections.<PageModificationExtension> emptyList(), m_pageList);
    assertTrue(m_pageList.isEmpty());
  }

  @Test
  public void testModifyPagesWithoutFilter() throws Exception {
    m_pageList.add(m_configuredPageA);
    m_pageList.add(m_configuredPageB);
    m_pageList.add(m_configuredPageC);

    List<PageModificationExtension> extensions = Arrays.asList(
        new PageModificationExtension(P_ConfiguredPageA.class, P_ConfiguredPageAModifier.class),
        new PageModificationExtension(P_ConfiguredPageC.class, P_ConfiguredPageCModifier.class));

    PageExtensionUtility.modifyPages(null, null, extensions, m_pageList);

    assertEquals(3, m_pageList.size());
    assertSame(m_configuredPageA, m_pageList.get(0));
    assertSame(m_configuredPageB, m_pageList.get(1));
    assertSame(m_configuredPageC, m_pageList.get(2));

    assertEquals(1, P_ConfiguredPageAModifier.s_counter);
    assertEquals(1, P_ConfiguredPageCModifier.s_counter);
  }

  @Test
  public void testModifyPagesWithFilter() throws Exception {
    m_pageList.add(m_configuredPageA);
    m_pageList.add(m_configuredPageB);
    m_pageList.add(m_configuredPageC);

    IPageExtensionFilter pageFilter = new IPageExtensionFilter() {
      @Override
      public boolean accept(IOutline outline, IPage parentPage, IPage affectedPage) {
        assertNull(outline);
        assertNull(parentPage);
        assertSame(m_configuredPageC, affectedPage);
        return false;
      }
    };

    List<PageModificationExtension> extensions = Arrays.asList(
        new PageModificationExtension(P_ConfiguredPageA.class, P_ConfiguredPageAModifier.class),
        new PageModificationExtension(pageFilter, P_ConfiguredPageC.class, P_ConfiguredPageCModifier.class));

    PageExtensionUtility.modifyPages(null, null, extensions, m_pageList);

    assertEquals(3, m_pageList.size());
    assertSame(m_configuredPageA, m_pageList.get(0));
    assertSame(m_configuredPageB, m_pageList.get(1));
    assertSame(m_configuredPageC, m_pageList.get(2));

    assertEquals(1, P_ConfiguredPageAModifier.s_counter);
    assertEquals(0, P_ConfiguredPageCModifier.s_counter);
  }

  /////////////////////////////////////////////////////////
  // test resources                                      //
  /////////////////////////////////////////////////////////

  public static class P_Outline extends AbstractOutline {
  }

  public static class P_ConfiguredPageA extends AbstractPageWithNodes {
  }

  public static class P_ConfiguredPageB extends AbstractPageWithNodes {
  }

  public static class P_ConfiguredPageC extends AbstractPageWithNodes {
  }

  public static class P_ContribPageA extends AbstractPageWithNodes {
  }

  public static class P_ContribPageB extends AbstractPageWithNodes {
  }

  public static class P_ContribPageC extends AbstractPageWithNodes {
  }

  public static class P_ConfiguredPageAModifier implements IPageModifier<IPage> {
    static int s_counter = 0;

    @Override
    public void modify(IOutline outline, IPage parentPage, IPage page) throws ProcessingException {
      s_counter++;
      assertNull(outline);
      assertNull(parentPage);
      assertNotNull(page);
      assertSame(P_ConfiguredPageA.class, page.getClass());
    }
  }

  public static class P_ConfiguredPageCModifier implements IPageModifier<IPage> {
    static int s_counter = 0;

    @Override
    public void modify(IOutline outline, IPage parentPage, IPage page) throws ProcessingException {
      s_counter++;
      assertNull(outline);
      assertNull(parentPage);
      assertNotNull(page);
      assertSame(P_ConfiguredPageC.class, page.getClass());
    }
  }
}
