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
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.junit.Test;

/**
 * @since 3.9.0
 */
public class CompositePageFilterTest {

  @Test
  public void testNullAndEmpty() {
    assertAccept(new CompositePageFilter());
    assertAccept(new CompositePageFilter((IPageExtensionFilter[]) null));
    assertAccept(new CompositePageFilter((IPageExtensionFilter) null));
    assertAccept(new CompositePageFilter(null, null));
  }

  @Test
  public void testAllAccepting() {
    assertAccept(new CompositePageFilter(new P_AcceptingFilter()));
    assertAccept(new CompositePageFilter(new P_AcceptingFilter(), new P_AcceptingFilter()));
    assertAccept(new CompositePageFilter(new P_AcceptingFilter(), null));
    assertAccept(new CompositePageFilter(new P_AcceptingFilter(), null, new P_AcceptingFilter()));
  }

  @Test
  public void testReject() {
    assertReject(new CompositePageFilter(new P_RejectingFilter()));
    assertReject(new CompositePageFilter(new P_AcceptingFilter(), new P_RejectingFilter()));
    assertReject(new CompositePageFilter(new P_AcceptingFilter(), null, new P_RejectingFilter()));
    assertReject(new CompositePageFilter(new P_RejectingFilter(), new P_RejectingFilter()));
  }

  @Test
  public void testAddRemoveFilter() {
    CompositePageFilter filter = new CompositePageFilter();
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
  private static void assertAccept(CompositePageFilter filter) {
    assertTrue(filter.accept(null, null, null));
  }

  private static void assertReject(CompositePageFilter filter) {
    assertFalse(filter.accept(null, null, null));
  }

  public static class P_AcceptingFilter implements IPageExtensionFilter {
    @Override
    public boolean accept(IOutline outline, IPage parentPage, IPage affectedPage) {
      return true;
    }
  }

  public static class P_RejectingFilter implements IPageExtensionFilter {
    @Override
    public boolean accept(IOutline outline, IPage parentPage, IPage affectedPage) {
      return false;
    }
  }
}
