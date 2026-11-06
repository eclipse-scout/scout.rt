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
import jakarta.servlet.http.HttpSessionBindingListener;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.mock.MockConfigPropertyRule;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.OrphanedUiSessionTimeoutProperty;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class SessionStoreHousekeeperTest {

  protected static final long MAX_SILENT_MILLIS = TimeUnit.MINUTES.toMillis(30);

  @Rule
  public MockConfigPropertyRule<Long> m_orphanedUiSessionTimeoutPropertyRule = new MockConfigPropertyRule<>(OrphanedUiSessionTimeoutProperty.class, TimeUnit.MILLISECONDS.toSeconds(MAX_SILENT_MILLIS));

  // --- SessionStore#disposeOrphanedUiSessions ---

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

  // --- SessionStore#disposeOrphanedUiSession ---

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

  // --- SessionStoreHousekeeper#sweep ---

  @Test
  public void testSweepDisposesOrphanedUiSessionsOfValidStore() {
    ISessionStore store = mockStore();
    SessionStoreHousekeeper housekeeper = new SessionStoreHousekeeper();
    housekeeper.m_stores.add(store);

    housekeeper.sweep();

    verify(store).disposeOrphanedUiSessions();
    assertTrue(housekeeper.m_stores.contains(store));
  }

  @Test
  public void testSweepDisposesSessionWithoutRequestLongerThanOrphanedUiSessionTimeout() {
    SessionStore store = new SessionStore(mock(HttpSession.class));
    IUiSession session = mockSession(MAX_SILENT_MILLIS + 10_000);
    store.m_uiSessionMap.put("test", session);
    SessionStoreHousekeeper housekeeper = new SessionStoreHousekeeper();
    housekeeper.m_stores.add(store);

    housekeeper.sweep();

    verify(session).dispose();
  }

  @Test
  public void testSweepRemovesStoreWithInvalidHttpSessionFlag() {
    ISessionStore store = mock(ISessionStore.class);
    when(store.isHttpSessionValid()).thenReturn(false);
    SessionStoreHousekeeper housekeeper = new SessionStoreHousekeeper();
    housekeeper.m_stores.add(store);

    housekeeper.sweep();

    assertFalse(housekeeper.m_stores.contains(store));
    verify(store, never()).disposeOrphanedUiSessions();
  }

  @Test
  public void testSweepRemovesStoreOfInvalidatedHttpSession() {
    ISessionStore store = mockStore();
    when(store.getHttpSession().getCreationTime()).thenThrow(new IllegalStateException("invalidated"));
    SessionStoreHousekeeper housekeeper = new SessionStoreHousekeeper();
    housekeeper.m_stores.add(store);

    housekeeper.sweep();

    assertFalse(housekeeper.m_stores.contains(store));
    verify(store, never()).disposeOrphanedUiSessions();
  }

  @Test
  public void testSweepContinuesAfterFailingStore() {
    ISessionStore failingStore = mock(ISessionStore.class);
    when(failingStore.isHttpSessionValid()).thenThrow(new RuntimeException("expected"));
    ISessionStore store = mockStore();
    SessionStoreHousekeeper housekeeper = new SessionStoreHousekeeper();
    housekeeper.m_stores.add(failingStore);
    housekeeper.m_stores.add(store);

    housekeeper.sweep();

    verify(store).disposeOrphanedUiSessions();
  }

  // --- registration ---

  @Test
  public void testRegisterDoesNothingIfDisabled() {
    SessionStoreHousekeeper housekeeper = new SessionStoreHousekeeper() {
      @Override
      protected long getInterval() {
        return 0;
      }
    };
    ISessionStore store = mock(ISessionStore.class);

    housekeeper.register(store);

    assertFalse(housekeeper.m_stores.contains(store));
    assertNull(housekeeper.m_job);
  }

  @Test
  public void testHttpSessionHelperRegistersAndValueUnboundUnregistersStore() {
    SessionStoreHousekeeper housekeeper = BEANS.get(SessionStoreHousekeeper.class);
    ISessionStore store = BEANS.get(HttpSessionHelper.class).getSessionStore(mock(HttpSession.class));
    try {
      assertTrue(housekeeper.m_stores.contains(store));

      ((HttpSessionBindingListener) store).valueUnbound(null);

      assertFalse(housekeeper.m_stores.contains(store));
    }
    finally {
      housekeeper.unregister(store);
    }
  }

  // --- helpers ---

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
