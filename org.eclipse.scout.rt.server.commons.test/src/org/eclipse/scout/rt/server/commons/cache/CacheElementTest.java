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
 * Test for {@link CacheElement}
 */
public class CacheElementTest {

  private final String m_testValue = "testValue";
  private final Integer testExpiration = Integer.valueOf(10000);
  private final String m_testKey = "testKey";

  @Test
  public void testCacheElement() {
    ICacheElement cacheElement = new CacheElement(m_testValue, testExpiration);
    assertTrue(cacheElement.isActive());
    assertEquals(m_testValue, cacheElement.getValue());
  }

}
