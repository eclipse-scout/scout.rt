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
 * Test for {@link SerializedCacheStoreService}
 */
public class SerializedCacheStoreServiceTest extends AbstractCacheStoreServiceTest {

  @Test
  public void testTouchAttribute() throws ProcessingException {
    CacheElement cacheElemSpy = new CacheElement(m_testValue, testExpiration, -1);
    assertEquals(-1, cacheElemSpy.getCreationTime());
    m_testSession.setAttribute(m_testKey, m_cacheService.serializedString(cacheElemSpy));
    m_cacheService.touchClientAttribute(m_requestMock, m_responseMock, m_testKey);
    assertEquals(-1, cacheElemSpy.getCreationTime());
  }

  @Override
  protected AbstractCacheStoreService createCacheService() {
    return new SerializedCacheStoreService();
  }

}
