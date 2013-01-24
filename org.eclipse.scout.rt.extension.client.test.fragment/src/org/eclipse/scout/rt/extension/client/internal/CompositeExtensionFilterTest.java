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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @since 3.9.0
 */
public class CompositeExtensionFilterTest {

  @Test
  public void testEmpty() {
    P_StringCompositeExtensionFilter filter = new P_StringCompositeExtensionFilter();
    assertTrue(filter.isEmpty());
    assertEquals(0, filter.size());
    assertArrayEquals(new String[0], filter.getFilters());
  }

  @Test
  public void testModifications() {
    P_StringCompositeExtensionFilter filter = new P_StringCompositeExtensionFilter();
    assertTrue(filter.addFilter("test"));
    assertFalse(filter.isEmpty());
    assertEquals(1, filter.size());
    //
    assertFalse(filter.addFilter(null));
    assertFalse(filter.isEmpty());
    assertEquals(1, filter.size());
    assertArrayEquals(new String[]{"test"}, filter.getFilters());
    //
    assertTrue(filter.addFilterAtBegin("other"));
    assertFalse(filter.isEmpty());
    assertEquals(2, filter.size());
    assertArrayEquals(new String[]{"other", "test"}, filter.getFilters());
    //
    assertTrue(filter.removeFilter("test"));
    assertFalse(filter.isEmpty());
    assertEquals(1, filter.size());
    assertArrayEquals(new String[]{"other"}, filter.getFilters());
  }

  private static class P_StringCompositeExtensionFilter extends AbstractCompositeExtensionFilter<String> {
  }
}
