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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Test;

/**
 * Test for {@link SerializedCacheService}
 */
public class SerializedCacheStoreServiceTest extends AbstractHttpSessionCacheServiceTest {

  @Test
  public void testTouchAttribute() throws ProcessingException {
    CacheEntry cacheElemSpy = new CacheEntry<Object>(TEST_VALUE, TEST_EXPIRATION, -1);
    assertEquals(-1, cacheElemSpy.getCreationTime());
    m_testSession.setAttribute(TEST_KEY, m_cacheService.serializedString(cacheElemSpy));
    m_cacheService.touch(TEST_KEY, m_requestMock, m_responseMock);
    assertEquals(-1, cacheElemSpy.getCreationTime());
  }

  @Override
  protected AbstractHttpSessionCacheService createCacheService() {
    return new SerializedCacheService();
  }

}
