/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

/**
 * Junit test for {@link ClientIdentificationService}
 * 
 * @since 4.0.0
 */
public class ClientIdentificationServiceTest {

  private HttpServletResponse m_responseMock;
  private HttpServletRequest m_requestMock;

  /**
   *
   */
  @Before
  public void setup() {
    m_requestMock = mock(HttpServletRequest.class);
    m_responseMock = mock(HttpServletResponse.class);
  }

  @Test
  public void testNewIdCreated() {
    ClientIdentificationService s = new ClientIdentificationService();
    String clientId = s.getClientId(m_requestMock, m_responseMock);
    assertNotNull("A new id should be created", clientId);
  }

  @Test
  public void testExistingCookie() {
    String testClientId = "testId";
    Cookie[] cookies = new Cookie[]{
        new Cookie(ClientIdentificationService.SCOUT_CLIENT_ID_KEY, testClientId)
    };
    when(m_requestMock.getCookies()).thenReturn(cookies);
    ClientIdentificationService s = new ClientIdentificationService();
    String clientId = s.getClientId(m_requestMock, m_responseMock);
    assertEquals(testClientId, clientId);
  }

}
