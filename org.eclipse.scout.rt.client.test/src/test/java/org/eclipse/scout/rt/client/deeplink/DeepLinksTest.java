/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.deeplink;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DeepLinksTest {

  IDeepLinks m_deepLinks = BEANS.get(IDeepLinks.class);

  @BeforeClass
  public static void setUpBeforeClass() {
    BEANS.getBeanManager().registerClass(FooBarDeepLinkHandler.class);
  }

  @AfterClass
  public static void tearDownAfterClass() {
    BEANS.getBeanManager().unregisterClass(FooBarDeepLinkHandler.class);
  }

  @Test
  public void testCanHandleDeepLink() {
    assertFalse(m_deepLinks.canHandleDeepLink("ticket-1234567"));
    assertTrue(m_deepLinks.canHandleDeepLink("foobar-123"));
  }

  @Test
  public void testHandleDeepLink() throws DeepLinkException {
    m_deepLinks.handleDeepLink("foobar-123");
    FooBarDeepLinkHandler handler = BEANS.get(FooBarDeepLinkHandler.class);
    assertEquals("123", handler.getLastMatch());
  }

  @Test(expected = DeepLinkException.class)
  public void testHandleDeepLink_Exception() throws Exception {
    m_deepLinks.handleDeepLink("foobar-321");
  }

  /**
   * The outline deep link handler should be registered by the Scout framework by default.
   */
  @Test
  public void testOutlineDeepLinkHandler() {
    assertTrue(m_deepLinks.canHandleDeepLink("outline-123"));
  }

}
