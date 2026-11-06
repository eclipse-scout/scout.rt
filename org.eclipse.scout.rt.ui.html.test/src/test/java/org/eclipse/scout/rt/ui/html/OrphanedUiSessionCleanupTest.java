/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.mock.MockConfigPropertyRule;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.OrphanedUiSessionTimeoutProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class OrphanedUiSessionCleanupTest {

  protected static final long MAX_SILENT_MILLIS = TimeUnit.MINUTES.toMillis(30);

  @Rule
  public MockConfigPropertyRule<Long> m_orphanedUiSessionTimeoutPropertyRule = new MockConfigPropertyRule<>(OrphanedUiSessionTimeoutProperty.class, TimeUnit.MILLISECONDS.toSeconds(MAX_SILENT_MILLIS));

  protected OrphanedUiSessionCleanup m_cleanup;
  protected IBean<?> m_cleanupBean;

  @Before
  public void before() {
    // test-local instance: stores registered by the SessionStore constructor don't leak into the global bean
    m_cleanup = spy(new OrphanedUiSessionCleanup());
    doNothing().when(m_cleanup).ensureJobRunning();
    m_cleanupBean = BeanTestingHelper.get().registerBean(new BeanMetaData(OrphanedUiSessionCleanup.class, m_cleanup));
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBean(m_cleanupBean);
  }

  @Test
  public void testDisposeOrphanedUiSessions_DisposesSessionWithoutRequestLongerThanMaxSilent() {
    // e.g. browser tab closed during startup: no request after the startup request
    SessionStore store = new SessionStore(mock(HttpSession.class));
    IUiSession session = mockSession(MAX_SILENT_MILLIS + 10_000);
    store.m_uiSessionMap.put("test", session);

    store.disposeOrphanedUiSessions();

    verify(session).dispose();
  }

  @Test
  public void testDisposeOrphanedUiSessions_KeepsSessionWithinMaxSilent() {
    SessionStore store = new SessionStore(mock(HttpSession.class));
    IUiSession session = mockSession(MAX_SILENT_MILLIS - 10_000);
    store.m_uiSessionMap.put("test", session);

    store.disposeOrphanedUiSessions();

    verify(session, never()).dispose();
  }

  @Test
  public void testDisposeOrphanedUiSessions_DoesNotDisposeSessionWithRequestAfterCheck() {
    SessionStore store = new SessionStore(mock(HttpSession.class));
    IUiSession session = mockSession(MAX_SILENT_MILLIS + 10_000);
    // first call (candidate collection): orphaned; second call (re-check): request received in the meantime
    when(session.getLastRequestTime()).thenReturn(System.currentTimeMillis() - MAX_SILENT_MILLIS - 10_000, System.currentTimeMillis());
    store.m_uiSessionMap.put("test", session);

    store.disposeOrphanedUiSessions();

    verify(session, never()).dispose();
  }

  @Test
  public void testDisposeOrphanedUiSessions_KeepsPollingSessionOfIdleUser() {
    // user has been idle for a long time, but the tab is still polling (max user idle timeout is handled by the request handler)
    SessionStore store = new SessionStore(mock(HttpSession.class));
    IUiSession session = mockSession(0);
    when(session.getLastAccessedTime()).thenReturn(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5));
    store.m_uiSessionMap.put("test", session);

    store.disposeOrphanedUiSessions();

    verify(session, never()).dispose();
  }

  @Test
  public void testDisposeOrphanedUiSession_DoesNotDisposeAlreadyDisposedSession() {
    SessionStore store = new SessionStore(mock(HttpSession.class));
    IUiSession session = mockSession(MAX_SILENT_MILLIS + 10_000);
    when(session.isDisposed()).thenReturn(true);

    store.disposeOrphanedUiSession(session, MAX_SILENT_MILLIS);

    verify(session, never()).dispose();
  }

  @Test
  public void testDisposeOrphanedUiSession_SkipsLockedSession() throws Exception {
    SessionStore store = new SessionStore(mock(HttpSession.class));
    IUiSession session = mockSession(MAX_SILENT_MILLIS + 10_000);
    ReentrantLock lock = session.uiSessionLock();
    store.m_uiSessionMap.put("test", session);

    // lock must be held by another thread (ReentrantLock#tryLock succeeds for the owning thread)
    CountDownLatch locked = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    Thread lockHolder = new Thread(() -> {
      lock.lock();
      try {
        locked.countDown();
        release.await();
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      finally {
        lock.unlock();
      }
    });
    lockHolder.start();
    try {
      assertTrue(locked.await(5, TimeUnit.SECONDS));
      store.disposeOrphanedUiSessions();
      verify(session, never()).dispose();
    }
    finally {
      release.countDown();
      lockHolder.join(5000);
    }

    // next run: lock is free -> disposed
    store.disposeOrphanedUiSessions();
    verify(session).dispose();
  }

  @Test
  public void testSweepDisposesOrphanedUiSessionsOfValidStore() {
    ISessionStore store = mockStore();
    OrphanedUiSessionCleanup cleanup = new OrphanedUiSessionCleanup();
    cleanup.m_stores.add(store);

    cleanup.sweep();

    verify(store).disposeOrphanedUiSessions();
    assertTrue(cleanup.m_stores.contains(store));
  }

  @Test
  public void testSweepDisposesSessionWithoutRequestLongerThanOrphanedUiSessionTimeout() {
    SessionStore store = new SessionStore(mock(HttpSession.class));
    IUiSession session = mockSession(MAX_SILENT_MILLIS + 10_000);
    store.m_uiSessionMap.put("test", session);
    OrphanedUiSessionCleanup cleanup = new OrphanedUiSessionCleanup();
    cleanup.m_stores.add(store);

    cleanup.sweep();

    verify(session).dispose();
  }

  @Test
  public void testSweepRemovesStoreWithInvalidHttpSessionFlag() {
    ISessionStore store = mock(ISessionStore.class);
    when(store.isHttpSessionValid()).thenReturn(false);
    OrphanedUiSessionCleanup cleanup = new OrphanedUiSessionCleanup();
    cleanup.m_stores.add(store);

    cleanup.sweep();

    assertFalse(cleanup.m_stores.contains(store));
    verify(store, never()).disposeOrphanedUiSessions();
  }

  @Test
  public void testSweepRemovesStoreOfInvalidatedHttpSession() {
    ISessionStore store = mockStore();
    when(store.getHttpSession().getCreationTime()).thenThrow(new IllegalStateException("invalidated"));
    OrphanedUiSessionCleanup cleanup = new OrphanedUiSessionCleanup();
    cleanup.m_stores.add(store);

    cleanup.sweep();

    assertFalse(cleanup.m_stores.contains(store));
    verify(store, never()).disposeOrphanedUiSessions();
  }

  @Test
  public void testSweepContinuesAfterFailingStore() {
    ISessionStore failingStore = mock(ISessionStore.class);
    when(failingStore.isHttpSessionValid()).thenThrow(new RuntimeException("expected"));
    ISessionStore store = mockStore();
    OrphanedUiSessionCleanup cleanup = new OrphanedUiSessionCleanup();
    cleanup.m_stores.add(failingStore);
    cleanup.m_stores.add(store);

    cleanup.sweep();

    verify(store).disposeOrphanedUiSessions();
  }

  @Test
  public void testRegisterDoesNothingIfDisabled() {
    OrphanedUiSessionCleanup cleanup = new OrphanedUiSessionCleanup() {
      @Override
      protected long getInterval() {
        return 0;
      }
    };
    ISessionStore store = mock(ISessionStore.class);

    cleanup.register(store);

    assertFalse(cleanup.m_stores.contains(store));
    assertNull(cleanup.m_job);
  }

  @Test
  public void testConstructorRegistersAndValueUnboundUnregistersStore() {
    SessionStore store = new SessionStore(mock(HttpSession.class));
    assertTrue(m_cleanup.m_stores.contains(store));

    store.valueUnbound(null);

    assertFalse(m_cleanup.m_stores.contains(store));
  }

  protected IUiSession mockSession(long lastRequestAgoMillis) {
    long lastRequestTime = System.currentTimeMillis() - lastRequestAgoMillis;
    IUiSession session = mock(IUiSession.class);
    when(session.getLastRequestTime()).thenReturn(lastRequestTime);
    when(session.getLastAccessedTime()).thenReturn(lastRequestTime);
    when(session.uiSessionLock()).thenReturn(new ReentrantLock());
    return session;
  }

  protected ISessionStore mockStore() {
    HttpSession httpSession = mock(HttpSession.class);
    ISessionStore store = mock(ISessionStore.class);
    when(store.isHttpSessionValid()).thenReturn(true);
    when(store.getHttpSession()).thenReturn(httpSession);
    return store;
  }
}
