/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.clientnotification;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link ClientSessionRegistry}
 */
@RunWith(PlatformTestRunner.class)
public class ClientSessionRegistryTest {

  @Mock
  private IClientNotificationService m_mockService;

  @Mock
  private IClientSession m_clientSession;

  private List<IBean<?>> m_regs;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    m_regs = TestingUtility.registerBeans(createBeanMetaDataWithoutTunnel(IClientNotificationService.class, m_mockService));
    when(m_clientSession.getId()).thenReturn("testSessionId");
    when(m_clientSession.getUserId()).thenReturn("testUserId");
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_regs);
  }

  /**
   * Tests that the client session is registered on the Back-End, when the session is started.
   */
  @Test
  public void testRegisteredOnBackendWhenSessionStarted() {
    ClientSessionRegistry reg = new TestClientSessionRegistry();
    reg.sessionStarted(m_clientSession);
    verify(m_mockService).registerSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  public void testUnRegisteredWhenSessionStopped() {
    ClientSessionRegistry reg = new TestClientSessionRegistry();
    reg.sessionStopped(m_clientSession);
    verify(m_mockService).unregisterSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
  }

  private <T> BeanMetaData createBeanMetaDataWithoutTunnel(Class<T> beanClass, T initialInstance) {
    BeanMetaData beanData = new BeanMetaData(beanClass).withInitialInstance(initialInstance).withApplicationScoped(true);
    Map<Class<? extends Annotation>, Annotation> annotations = beanData.getBeanAnnotations();
    annotations.remove(TunnelToServer.class);
    beanData.setBeanAnnotations(annotations);
    return beanData;
  }

  class TestClientSessionRegistry extends ClientSessionRegistry {
    @Override
    protected void ensureUserIdAvailable(IClientSession session) {
      //nop
    }
  }

}
