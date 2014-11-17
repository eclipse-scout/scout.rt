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
package org.eclipse.scout.rt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link m_backendJobService}
 */
public class ServerJobServiceTest {
  private ServerJobService m_serverJobService;

  @Before
  public void setup() {
    m_serverJobService = new ServerJobService();
  }

  /**
   * {@link ServerJobService#getServerSubject()}
   */
  @Test
  public void testServerSubject() throws Exception {
    final Subject serverSubject = m_serverJobService.getServerSubject();
    assertNotNull(serverSubject);
  }

  /**
   * {@link ServerJobService#createSubject(String)}
   */
  @Test
  public void testCreateSubject() throws Exception {
    final Subject subject = m_serverJobService.createSubject("test");
    assertEquals("test", subject.getPrincipals().iterator().next().getName());
    assertNotNull(subject);
  }

  /**
   * Test server session class for configured session.
   */
  @Test
  public void testGetServerSessionClass() throws Exception {
    m_serverJobService.setServerSessionClassName("org.eclipse.scout.rt.server.TestServerSession");
    final Class<? extends IServerSession> session = m_serverJobService.getServerSessionClass();
    assertNotNull(session);
  }

  @Test(expected = ProcessingException.class)
  public void testGetServerSessionClassWrongType() throws Exception {
    m_serverJobService.setServerSessionClassName("java.lang.Object");
    m_serverJobService.getServerSessionClass();
  }

  @Test(expected = ProcessingException.class)
  public void testGetServerSessionUnknownClass() throws Exception {
    m_serverJobService.setServerSessionClassName("unknownClass");
    m_serverJobService.getServerSessionClass();
  }

}
