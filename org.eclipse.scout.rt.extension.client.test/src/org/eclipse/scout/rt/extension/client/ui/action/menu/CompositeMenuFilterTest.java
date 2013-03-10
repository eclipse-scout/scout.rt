package org.eclipse.scout.rt.extension.client.ui.action.menu;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since 3.9.0
 */
public class CompositeMenuFilterTest {

  @Test
  public void testAcceptNullAndEmpty() {
    assertAccept(new CompositeMenuFilter());
    assertAccept(new CompositeMenuFilter((IMenuExtensionFilter[]) null));
    assertAccept(new CompositeMenuFilter((IMenuExtensionFilter) null));
    assertAccept(new CompositeMenuFilter(null, null));
  }

  @Test
  public void testAllAccepting() {
    assertAccept(new CompositeMenuFilter(new P_AcceptingFilter()));
    assertAccept(new CompositeMenuFilter(new P_AcceptingFilter(), new P_AcceptingFilter()));
    assertAccept(new CompositeMenuFilter(new P_AcceptingFilter(), null));
    assertAccept(new CompositeMenuFilter(new P_AcceptingFilter(), null, new P_AcceptingFilter()));
  }

  @Test
  public void testReject() {
    assertReject(new CompositeMenuFilter(new P_RejectingFilter()));
    assertReject(new CompositeMenuFilter(new P_AcceptingFilter(), new P_RejectingFilter()));
    assertReject(new CompositeMenuFilter(new P_AcceptingFilter(), null, new P_RejectingFilter()));
    assertReject(new CompositeMenuFilter(new P_RejectingFilter(), new P_RejectingFilter()));
  }

  @Test
  public void testAddRemoveFilter() {
    CompositeMenuFilter filter = new CompositeMenuFilter();
    assertAccept(filter);
    Assert.assertTrue(filter.isEmpty());
    //
    P_AcceptingFilter acceptFilter = new P_AcceptingFilter();
    filter.addFilter(acceptFilter);
    assertAccept(filter);
    Assert.assertFalse(filter.isEmpty());
    //
    P_RejectingFilter rejectFilter = new P_RejectingFilter();
    filter.addFilter(rejectFilter);
    assertReject(filter);
    Assert.assertFalse(filter.isEmpty());
    //
    filter.removeFilter(rejectFilter);
    assertAccept(filter);
    Assert.assertFalse(filter.isEmpty());
    //
    filter.addFilterAtBegin(rejectFilter);
    assertReject(filter);
    Assert.assertFalse(filter.isEmpty());
    //
    filter.removeFilter(rejectFilter);
    assertAccept(filter);
    Assert.assertFalse(filter.isEmpty());
    //
    filter.removeFilter(acceptFilter);
    assertAccept(filter);
    Assert.assertTrue(filter.isEmpty());
  }

  /*
   * test support
   */
  private static void assertAccept(CompositeMenuFilter filter) {
    Assert.assertTrue(filter.accept(null, null, null));
  }

  private static void assertReject(CompositeMenuFilter filter) {
    Assert.assertFalse(filter.accept(null, null, null));
  }

  public static class P_AcceptingFilter implements IMenuExtensionFilter {
    @Override
    public boolean accept(Object anchor, Object container, IMenu menu) {
      return true;
    }
  }

  public static class P_RejectingFilter implements IMenuExtensionFilter {
    @Override
    public boolean accept(Object anchor, Object container, IMenu menu) {
      return false;
    }
  }
}
