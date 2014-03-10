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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link SingleNodeCacheStoreService}
 */
public class SingleNodeCacheStoreServiceTest {

  private HttpServletResponse m_responseMock;
  private HttpServletRequest m_requestMock;
  private String m_testValue;

  @Before
  public void setup() {
    m_requestMock = mock(HttpServletRequest.class);
    m_responseMock = mock(HttpServletResponse.class);
    when(m_requestMock.getSession()).thenReturn(new TestHttpSession());
    m_testValue = "testValue";
  }

  @Test
  public void testSetClientAttribute() {
    SingleNodeCacheStoreService s = new SingleNodeCacheStoreService();
    String testKey = "testKey";
    s.setClientAttribute(m_requestMock, m_responseMock, testKey, m_testValue);
    s.getClientAttribute(m_requestMock, m_responseMock, testKey);
    assertEquals(m_testValue, s.getClientAttribute(m_requestMock, m_responseMock, testKey));
  }

  @Test
  public void testCacheElement() {
    SingleNodeCacheStoreService s = new SingleNodeCacheStoreService();
    ICacheElement cacheElement = s.new CacheElement(m_testValue, 10000);
    assertTrue(cacheElement.isActive());
    assertEquals(m_testValue, cacheElement.getValue());
  }
}
