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
package org.eclipse.scout.rt.server.commons.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test for {@link CacheEntry}
 */
public class CacheElementTest {

  private final String m_testValue = "testValue";
  private final Long testExpiration = 1000L;

  @Test
  public void testCacheElement() {
    ICacheEntry cacheElement = new CacheEntry<Object>(m_testValue, testExpiration, System.currentTimeMillis());
    assertTrue(cacheElement.isActive());
    assertEquals(m_testValue, cacheElement.getValue());
  }

}
