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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

/**
 * Test for {@link StickySessionCacheStoreService}
 */
public class StickySessionCacheStoreServiceTest extends AbstractCacheStoreServiceTest {

  @Test
  public void testTouchAttribute() {
    ICacheElement mockCacheElement = mock(ICacheElement.class);
    when(mockCacheElement.isActive()).thenReturn(true);
    m_testSession.setAttribute(m_testKey, mockCacheElement);
    m_cacheService.touchClientAttribute(m_requestMock, m_responseMock, m_testKey);
    verify(mockCacheElement, times(1)).resetCreationTime();
  }

  @Override
  protected AbstractCacheStoreService createCacheService() {
    return new StickySessionCacheStoreService();
  }

}
