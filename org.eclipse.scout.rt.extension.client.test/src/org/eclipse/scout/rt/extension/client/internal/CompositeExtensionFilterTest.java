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
package org.eclipse.scout.rt.extension.client.internal;

import org.junit.Assert;
import org.junit.Test;

/**
 * @since 3.9.0
 */
public class CompositeExtensionFilterTest {

  @Test
  public void testEmpty() {
    P_StringCompositeExtensionFilter filter = new P_StringCompositeExtensionFilter();
    Assert.assertTrue(filter.isEmpty());
    Assert.assertEquals(0, filter.size());
    Assert.assertArrayEquals(new String[0], filter.getFilters());
  }

  @Test
  public void testModifications() {
    P_StringCompositeExtensionFilter filter = new P_StringCompositeExtensionFilter();
    Assert.assertTrue(filter.addFilter("test"));
    Assert.assertFalse(filter.isEmpty());
    Assert.assertEquals(1, filter.size());
    //
    Assert.assertFalse(filter.addFilter(null));
    Assert.assertFalse(filter.isEmpty());
    Assert.assertEquals(1, filter.size());
    Assert.assertArrayEquals(new String[]{"test"}, filter.getFilters());
    //
    Assert.assertTrue(filter.addFilterAtBegin("other"));
    Assert.assertFalse(filter.isEmpty());
    Assert.assertEquals(2, filter.size());
    Assert.assertArrayEquals(new String[]{"other", "test"}, filter.getFilters());
    //
    Assert.assertTrue(filter.removeFilter("test"));
    Assert.assertFalse(filter.isEmpty());
    Assert.assertEquals(1, filter.size());
    Assert.assertArrayEquals(new String[]{"other"}, filter.getFilters());
  }

  private static class P_StringCompositeExtensionFilter extends AbstractCompositeExtensionFilter<String> {
  }
}
