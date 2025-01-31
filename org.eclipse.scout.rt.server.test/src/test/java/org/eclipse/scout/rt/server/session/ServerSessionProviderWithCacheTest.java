/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
@RunWithSubject("sessionTestUser")
public class ServerSessionProviderWithCacheTest {

  private static List<IBean<?>> s_beans;
  private IBean<ServerSessionProviderWithCache> m_sessionProviderBean;

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

  @After
  public void after() {
    FixtureServerSessionProviderWithCache optBean = BEANS.opt(FixtureServerSessionProviderWithCache.class);
    if (optBean != null) {
      ConcurrentExpiringMap<CompositeObject, IServerSession> cache = optBean.m_cache;
      if (cache != null) {
        cache.forEach((c, s) -> s.stop());
        cache.clear();
      }
    }

    FixtureServerSession.s_startedCount.set(0);
    FixtureServerSession.s_stoppedCount.set(0);

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
        + FixtureServerSessionProviderWithCache.s_fixedCacheTimeToLiveMillis
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

  @Test
  public void testParallelProvideSession() throws InterruptedException {
    createAndRegisterFixtureSessionProviderWithCache(30000);

    ExecutorService executorService = Executors.newFixedThreadPool(10);
    try {
      assertEquals(
          1, // distinct count should be 1 = all 20 sessions are the same
          executorService.invokeAll(IntStream.range(0, 20).mapToObj(s -> (Callable<FixtureServerSession>) () -> provideSession(null, "foo")).collect(Collectors.toList())) // request 20 sessions in parallel in multiple threads
              .stream()
              .map(f -> {
                try {
                  return f.get();
                }
                catch (InterruptedException | ExecutionException e) {
                  throw new RuntimeException(e);
                }
              })
              .distinct()
              .limit(2)
              .count());
    }
    finally {
      executorService.shutdown();
      assertTrue(executorService.awaitTermination(30, TimeUnit.MILLISECONDS)); // everything should be terminated immediately (actually already with get calls above)
    }

    // during parallel calls multiple sessions may have been started
    // however all sessions except one must also be stopped (the ones which were not used)
    assertEquals(FixtureServerSession.s_startedCount.get() - 1, FixtureServerSession.s_stoppedCount.get());
  }

  // --- test support ---------------------------------------------------------

  private ServerSessionProviderWithCache createAndRegisterDefaultSessionProviderWithCache() {
    return registerSessionProviderWithCache(new ServerSessionProviderWithCache());
  }

  private ServerSessionProviderWithCache createAndRegisterFixtureSessionProviderWithCache() {
    return createAndRegisterFixtureSessionProviderWithCache(10);
  }

  private ServerSessionProviderWithCache createAndRegisterFixtureSessionProviderWithCache(long cacheTtlMillis) {
    FixtureServerSessionProviderWithCache.s_fixedCacheTimeToLiveMillis = cacheTtlMillis;
    return registerSessionProviderWithCache(new FixtureServerSessionProviderWithCache());
  }

  private ServerSessionProviderWithCache registerSessionProviderWithCache(ServerSessionProviderWithCache sessionProvider) {
    m_sessionProviderBean = BeanTestingHelper.get().registerBean(new BeanMetaData(sessionProvider.getClass()).withInitialInstance(sessionProvider).withReplace(true));
    return sessionProvider;
  }

  private FixtureServerSession provideSession(String sessionId, String userId) {
    IServerSession s = m_sessionProviderBean.getInstance().provide(sessionId, ServerRunContexts.empty().withSubject(createSubject(userId)));
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
    protected static volatile AtomicInteger s_startedCount = new AtomicInteger();
    protected static volatile AtomicInteger s_stoppedCount = new AtomicInteger();

    public FixtureServerSession() {
      super(true);
    }

    @Override
    protected void execLoadSession() {
      super.execLoadSession();
      m_started = true;
      s_startedCount.incrementAndGet();
    }

    @Override
    public void stop() {
      super.stop();
      m_stopped = true;
      s_stoppedCount.incrementAndGet();
    }

    public boolean isStarted() {
      return m_started;
    }

    public boolean isStopped() {
      return m_stopped;
    }
  }

  private static class FixtureServerSessionProviderWithCache extends ServerSessionProviderWithCache {

    protected static long s_fixedCacheTimeToLiveMillis = 10;

    protected ConcurrentExpiringMap<CompositeObject, IServerSession> m_cache;

    @Override
    protected ConcurrentExpiringMap<CompositeObject, IServerSession> createSessionCache(long ttl) {
      m_cache = super.createSessionCache(s_fixedCacheTimeToLiveMillis);
      return m_cache;
    }
  }
}
