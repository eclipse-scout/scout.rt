/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link StickySessionCacheService}
 */
@RunWith(PlatformTestRunner.class)
public class StickySessionCacheStoreServiceTest extends AbstractHttpSessionCacheServiceTest {

  @Test
  public void testTouchAttribute() {
    ICacheEntry mockEntry = mock(ICacheEntry.class);
    when(mockEntry.isActive()).thenReturn(true);
    m_testSession.setAttribute(TEST_KEY, mockEntry);
    m_cacheService.touch(TEST_KEY, m_requestMock, m_responseMock);
    verify(mockEntry, times(1)).touch();
  }

  @Override
  protected AbstractHttpSessionCacheService createCacheService() {
    return new StickySessionCacheService();
  }

}
