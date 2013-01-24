package org.eclipse.scout.rt.extension.client.ui.action.menu;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
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
    assertTrue(filter.isEmpty());
    //
    P_AcceptingFilter acceptFilter = new P_AcceptingFilter();
    filter.addFilter(acceptFilter);
    assertAccept(filter);
    assertFalse(filter.isEmpty());
    //
    P_RejectingFilter rejectFilter = new P_RejectingFilter();
    filter.addFilter(rejectFilter);
    assertReject(filter);
    assertFalse(filter.isEmpty());
    //
    filter.removeFilter(rejectFilter);
    assertAccept(filter);
    assertFalse(filter.isEmpty());
    //
    filter.addFilterAtBegin(rejectFilter);
    assertReject(filter);
    assertFalse(filter.isEmpty());
    //
    filter.removeFilter(rejectFilter);
    assertAccept(filter);
    assertFalse(filter.isEmpty());
    //
    filter.removeFilter(acceptFilter);
    assertAccept(filter);
    assertTrue(filter.isEmpty());
  }

  /*
   * test support
   */
  private void assertAccept(CompositeMenuFilter filter) {
    assertTrue(filter.accept(null, null, null));
  }

  private void assertReject(CompositeMenuFilter filter) {
    assertFalse(filter.accept(null, null, null));
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
