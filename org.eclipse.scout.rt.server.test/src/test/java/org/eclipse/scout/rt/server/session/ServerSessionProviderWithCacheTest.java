/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.JUnitServerSessionProviderWithCache;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
@RunWithSubject("sessionTestUser")
public class ServerSessionProviderWithCacheTest {

  private static List<IBean<?>> s_beans;
  private IBean<ServerSessionProviderWithCache> m_sessionProviderBean;
  private List<IServerSession> m_sessions;

  @BeforeClass
  public static void beforeClass() {
    // register fixture session
    s_beans = BeanTestingHelper.get().registerBeans(new BeanMetaData(FixtureServerSession.class));

    // unregister testing session provider
    BEANS.getBeanManager().unregisterClass(JUnitServerSessionProviderWithCache.class);
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBeans(s_beans);
    s_beans = null;

    // re-register testing session provider
    BEANS.getBeanManager().registerClass(JUnitServerSessionProviderWithCache.class);
  }

  @Before
  public void before() {
    m_sessions = new ArrayList<>();
  }

  @After
  public void after() {
    m_sessions.forEach(IServerSession::stop);
    m_sessions = null;

    if (m_sessionProviderBean != null) {
      BeanTestingHelper.get().unregisterBean(m_sessionProviderBean);
      m_sessionProviderBean = null;
    }
  }

  @Test
  public void testNewSessionCacheKey() {
    ServerSessionProviderWithCache sessionProvider = createAndRegisterDefaultSessionProviderWithCache();
    Subject subject = createSubject("anna");
    assertNull(sessionProvider.newSessionCacheKey(null, null));
    assertEquals(new CompositeObject("sessionId"), sessionProvider.newSessionCacheKey("sessionId", null));
    assertEquals(new CompositeObject("anna"), sessionProvider.newSessionCacheKey(null, subject));
    assertEquals(new CompositeObject("sessionId"), sessionProvider.newSessionCacheKey("sessionId", subject));
  }

  @Test
  public void testProvideSession() {
    createAndRegisterDefaultSessionProviderWithCache();
    FixtureServerSession session = provideSession(null, "anna");
    assertTrue(session.isStarted());
    assertTrue(session.isActive());
    assertFalse(session.isStopping());
    assertFalse(session.isStopped());
    assertEquals("anna", session.getUserId());

    // invoke provide again results in the same instance
    assertSame(session, provideSession(null, "anna"));
  }

  @Test
  public void testProvideSessionAndStop() {
    createAndRegisterDefaultSessionProviderWithCache();
    FixtureServerSession session = provideSession(null, "anna");
    session.stop();
    assertTrue(session.isStarted());
    assertFalse(session.isActive());
    assertFalse(session.isStopping());
    assertTrue(session.isStopped());

    // invoke provide again results in another instance
    FixtureServerSession otherSession = provideSession(null, "anna");
    assertNotNull(otherSession);
    assertNotSame(session, otherSession);
  }

  @Test
  public void testCacheEvictionStopsSession() {
    createAndRegisterFixtureSessionProviderWithCache();
    FixtureServerSession session = provideSession(null, "anna");
    final long sessionCreatedMillis = System.currentTimeMillis();

    assertTrue(session.isStarted());
    assertTrue(session.isActive());
    assertFalse(session.isStopping());
    assertFalse(session.isStopped());
    assertEquals("anna", session.getUserId());

    long sleepMillis = System.currentTimeMillis() - sessionCreatedMillis // time elapsed since session was created
        + FixtureServerSessionProviderWithCache.CACHE_TTL_MILLIS
        + 5; // add some more millis
    if (sleepMillis >= 0) {
      SleepUtil.sleepSafe(sleepMillis, TimeUnit.MILLISECONDS);
    }

    // providing again should evict the current and return a new one
    FixtureServerSession otherSession = provideSession(null, "anna");
    assertNotSame(session, otherSession);

    // new session is active
    assertTrue(otherSession.isStarted());
    assertTrue(otherSession.isActive());
    assertFalse(otherSession.isStopping());
    assertFalse(otherSession.isStopped());
    assertEquals("anna", otherSession.getUserId());

    // old session is stopped
    assertTrue(session.isStarted());
    assertFalse(session.isActive());
    assertFalse(session.isStopping());
    assertTrue(session.isStopped());
  }

  // --- test support ---------------------------------------------------------

  private ServerSessionProviderWithCache createAndRegisterDefaultSessionProviderWithCache() {
    return registerSessionProviderWithCache(new ServerSessionProviderWithCache());
  }

  private ServerSessionProviderWithCache createAndRegisterFixtureSessionProviderWithCache() {
    return registerSessionProviderWithCache(new FixtureServerSessionProviderWithCache());
  }

  private ServerSessionProviderWithCache registerSessionProviderWithCache(ServerSessionProviderWithCache sessionProvider) {
    m_sessionProviderBean = BeanTestingHelper.get().registerBean(new BeanMetaData(ServerSessionProviderWithCache.class).withInitialInstance(sessionProvider));
    return sessionProvider;
  }

  private FixtureServerSession provideSession(String sessionId, String userId) {
    IServerSession s = m_sessionProviderBean.getInstance().provide(sessionId, ServerRunContexts.empty().withSubject(createSubject(userId)));
    m_sessions.add(s);
    return Assertions.assertInstance(s, FixtureServerSession.class);
  }

  private Subject createSubject(String userId) {
    if (userId == null) {
      return null;
    }
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(userId));
    subject.setReadOnly();
    return subject;
  }

  private static class FixtureServerSession extends AbstractServerSession {
    private static final long serialVersionUID = 1L;
    private volatile boolean m_started;
    private volatile boolean m_stopped;

    public FixtureServerSession() {
      super(true);
    }

    @Override
    protected void execLoadSession() {
      super.execLoadSession();
      m_started = true;
    }

    @Override
    public void stop() {
      super.stop();
      m_stopped = true;
    }

    public boolean isStarted() {
      return m_started;
    }

    public boolean isStopped() {
      return m_stopped;
    }
  }

  private static class FixtureServerSessionProviderWithCache extends ServerSessionProviderWithCache {

    static final long CACHE_TTL_MILLIS = 10;

    @Override
    protected ConcurrentExpiringMap<CompositeObject, IServerSession> createSessionCache(long ttl) {
      return super.createSessionCache(CACHE_TTL_MILLIS);
    }
  }
}
