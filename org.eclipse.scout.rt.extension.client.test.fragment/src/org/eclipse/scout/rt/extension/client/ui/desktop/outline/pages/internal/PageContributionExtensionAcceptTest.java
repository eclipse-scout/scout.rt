package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageExtensionFilter;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.9.0
 */
@RunWith(ScoutClientTestRunner.class)
public class PageContributionExtensionAcceptTest {

  private PageContributionOutline m_outline;
  private PageContributionParentPage m_parentPage;
  private PageContributionPageFilter m_filter;

  @Before
  public void before() {
    m_outline = new PageContributionOutline();
    m_parentPage = new PageContributionParentPage();
    m_filter = new PageContributionPageFilter();
  }

  @Test
  public void testAcceptNull() {
    PageContributionExtension contrib = new PageContributionExtension(null, null, 0);
    assertTrue(contrib.accept(null, null, null));
    assertTrue(contrib.accept(m_outline, null, null));
    assertTrue(contrib.accept(null, m_parentPage, null));
  }

  @Test
  public void testAcceptNullWithFilter() {
    PageContributionExtension contrib = new PageContributionExtension(m_filter, ContributedPage.class, 0);
    m_filter.setAccept(true);
    assertTrue(contrib.accept(null, null, null));
    m_filter.setAccept(false);
    assertFalse(contrib.accept(null, null, null));

    m_filter.setAccept(true);
    assertTrue(contrib.accept(m_outline, null, null));
    m_filter.setAccept(false);
    assertFalse(contrib.accept(m_outline, null, null));

    m_filter.setAccept(true);
    assertTrue(contrib.accept(null, m_parentPage, null));
    m_filter.setAccept(false);
    assertFalse(contrib.accept(null, m_parentPage, null));
  }

  @Test
  public void testAcceptOutline() {
    PageContributionExtension m_contrib = new PageContributionExtension(new ParentAndOutlinePageFilter(PageContributionOutline.class, null), ContributedPage.class, 0);
    assertFalse(m_contrib.accept(null, null, null));
    assertFalse(m_contrib.accept(null, m_parentPage, null));
    assertTrue(m_contrib.accept(m_outline, null, null));
  }

  @Test
  public void testAcceptOutlineWithFilter() {
    PageContributionExtension m_contrib = new PageContributionExtension(new ParentAndOutlinePageFilter(PageContributionOutline.class, null, m_filter), ContributedPage.class, 0);
    m_filter.setAccept(true);
    assertFalse(m_contrib.accept(null, null, null));
    m_filter.setAccept(false);
    assertFalse(m_contrib.accept(null, null, null));

    m_filter.setAccept(true);
    assertFalse(m_contrib.accept(null, m_parentPage, null));
    m_filter.setAccept(false);
    assertFalse(m_contrib.accept(null, m_parentPage, null));

    m_filter.setAccept(true);
    assertTrue(m_contrib.accept(m_outline, null, null));
    m_filter.setAccept(false);
    assertFalse(m_contrib.accept(m_outline, null, null));
  }

  @Test
  public void testAcceptParentPage() {
    PageContributionExtension m_contrib = new PageContributionExtension(new ParentAndOutlinePageFilter(null, PageContributionParentPage.class), ContributedPage.class, 0);
    assertFalse(m_contrib.accept(null, null, null));
    assertFalse(m_contrib.accept(m_outline, null, null));
    assertTrue(m_contrib.accept(null, m_parentPage, null));
  }

  @Test
  public void testAcceptParentPageWithFilter() {
    PageContributionExtension m_contrib = new PageContributionExtension(new ParentAndOutlinePageFilter(null, PageContributionParentPage.class, m_filter), ContributedPage.class, 0);
    m_filter.setAccept(true);
    assertFalse(m_contrib.accept(null, null, null));
    m_filter.setAccept(false);
    assertFalse(m_contrib.accept(null, null, null));

    m_filter.setAccept(true);
    assertFalse(m_contrib.accept(m_outline, null, null));
    m_filter.setAccept(false);
    assertFalse(m_contrib.accept(m_outline, null, null));

    m_filter.setAccept(true);
    assertTrue(m_contrib.accept(null, m_parentPage, null));
    m_filter.setAccept(false);
    assertFalse(m_contrib.accept(null, m_parentPage, null));
  }

  public static class PageContributionOutline extends AbstractOutline {
  }

  public static class PageContributionParentPage extends AbstractPageWithNodes {
  }

  public static class ContributedPage extends AbstractPage {

    private final Constructor m_constructor;
    private Object m_context;

    public enum Constructor {
      DEFAULT, OUTLINE, PAGE
    }

    public ContributedPage() {
      m_constructor = Constructor.DEFAULT;
    }

    public ContributedPage(IOutline outline) {
      m_constructor = Constructor.OUTLINE;
      m_context = outline;
    }

    public ContributedPage(IPage parentPage) {
      m_constructor = Constructor.PAGE;
      m_context = parentPage;
    }

    public Object getContext() {
      return m_context;
    }

    public Constructor getConstructor() {
      return m_constructor;
    }
  }

  public static class PageContributionPageFilter implements IPageExtensionFilter {

    private boolean m_accept;

    public boolean isAccept() {
      return m_accept;
    }

    public void setAccept(boolean accept) {
      m_accept = accept;
    }

    @Override
    public boolean accept(IOutline outline, IPage parentPage, IPage affectedPage) {
      return m_accept;
    }
  }
}
