package org.eclipse.scout.rt.server.session;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.server.TestHttpSession;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Test for {@link ServerSessionCache}
 */
@RunWith(PlatformTestRunner.class)
public class ServerSessionCacheTest {

  private List<IBean<?>> m_registrations = new ArrayList<>();

  private String testSessionId = "testId";

  private IServerSession m_testScoutSession = new AbstractServerSession(true) {
    private static final long serialVersionUID = 1L;
  };

  /**
   * Register empty instance of {@link ServerSessionCache} for each test
   */
  @Before
  public void before() {
    m_registrations.add(TestingUtility.registerBean(
        new BeanMetaData(ServerSessionCache.class)
            .withInitialInstance(new ServerSessionCache())
            .withApplicationScoped(true)));
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_registrations);
  }

  /**
   * Tests the lookup, if a server session already exists on the {@link HttpSession}.
   */
  @Test
  public void testGetExisting() {
    HttpSession httpSession = mock(HttpSession.class);
    when(httpSession.getAttribute(IServerSession.class.getName())).thenReturn(m_testScoutSession);
    IServerSession session = BEANS.get(ServerSessionCache.class)
        .getOrCreate(new FixedServerSessionLifecycleHandler(), httpSession);
    assertSame(m_testScoutSession, session);
  }

  /**
   * Tests the lookup: If no server session exists on the {@link HttpSession}, a new scout session should be created.
   */
  @Test
  public void testNewSessionCreated() {
    HttpSession httpSession = spy(HttpSession.class);
    IServerSession session = BEANS.get(ServerSessionCache.class).getOrCreate(new FixedServerSessionLifecycleHandler(), httpSession);
    assertNotNull(session);
    //new server session should be stored on httpsession
    verify(httpSession).setAttribute(IServerSession.class.getName(), m_testScoutSession);
  }

  /**
   * When the {@link HttpSession} is invalidated and unbound, the destructionCallback should be called.
   */
  @Test
  public void testDestroyCalledOnUnbind() {
    TestHttpSession httpSession = new TestHttpSession();
    IServerSessionLifecycleHandler lifecycleSpy = spy(new FixedServerSessionLifecycleHandler());
    BEANS.get(ServerSessionCache.class).getOrCreate(lifecycleSpy, httpSession);
    httpSession.invalidate();
    verify(lifecycleSpy).destroy(Mockito.any(IServerSession.class));
  }

  /**
   * If there are multiple HTTP sessions (can happen, if many requests are created in parallel for the same sessionId),
   * but a single sessionId, a new server session should only be created once.
   */
  @Test
  public void testMultipleHttpSessions() {
    TestHttpSession httpSession1 = new TestHttpSession();
    TestHttpSession httpSession2 = new TestHttpSession();
    IServerSessionLifecycleHandler handler = new TestServerSessionLifecycleHandler();
    IServerSession session1 = BEANS.get(ServerSessionCache.class).getOrCreate(handler, httpSession1);
    IServerSession session2 = BEANS.get(ServerSessionCache.class).getOrCreate(handler, httpSession2);
    assertSame(session1, session2);
  }

  /**
   * If there are multiple HTTP sessions, and multiple sessionIds, the sessions should be different.
   */
  @Test
  public void testMultipleHttpSessionsMultipleSessionIds() {
    TestHttpSession httpSession1 = new TestHttpSession();
    TestHttpSession httpSession2 = new TestHttpSession();
    IServerSessionLifecycleHandler handler1 = new TestServerSessionLifecycleHandler("id1");
    IServerSessionLifecycleHandler handler2 = new TestServerSessionLifecycleHandler("id2");
    IServerSession session1 = BEANS.get(ServerSessionCache.class).getOrCreate(handler1, httpSession1);
    IServerSession session2 = BEANS.get(ServerSessionCache.class).getOrCreate(handler2, httpSession2);
    assertNotSame(session1, session2);
  }

  /**
   * If there are multiple HTTP sessions, and one gets invalidated, the scout session should not be destroyed.
   */
  @Test
  public void testDestroyWithMultipleHttpSessions() {
    TestHttpSession httpSession1 = new TestHttpSession();
    TestHttpSession httpSession2 = new TestHttpSession();
    ServerSessionCache cache = BEANS.get(ServerSessionCache.class);
    IServerSessionLifecycleHandler lifecycleSpy = spy(new FixedServerSessionLifecycleHandler());
    cache.getOrCreate(lifecycleSpy, httpSession1);
    cache.getOrCreate(lifecycleSpy, httpSession2);
    httpSession1.invalidate();
    verify(lifecycleSpy, Mockito.times(0)).destroy(Mockito.any(IServerSession.class));
  }

  @Test
  public void testParallelRequestsWithdifferentIds() {
    final TestHttpSession httpSession1 = new TestHttpSession();
    final TestHttpSession httpSession2 = new TestHttpSession();
    final ServerSessionCache cache = BEANS.get(ServerSessionCache.class);
    final IBlockingCondition bc = Jobs.newBlockingCondition(true);

    final IServerSessionLifecycleHandler handler1 = new TestServerSessionLifecycleHandler("id1") {

      @Override
      public IServerSession create() {
        bc.waitFor(1, TimeUnit.SECONDS);
        return super.create();
      }
    };

    final IServerSessionLifecycleHandler handler2 = new TestServerSessionLifecycleHandler("id2") {

      @Override
      public IServerSession create() {
        bc.setBlocking(false);
        return super.create();
      }

    };
    IFuture<IServerSession> f1 = Jobs.schedule(new Callable<IServerSession>() {

      @Override
      public IServerSession call() throws Exception {
        return cache.getOrCreate(handler1, httpSession1);
      }
    }, Jobs.newInput());
    IFuture<IServerSession> f2 = Jobs.schedule(new Callable<IServerSession>() {

      @Override
      public IServerSession call() throws Exception {
        return cache.getOrCreate(handler2, httpSession2);
      }
    }, Jobs.newInput());

    IServerSession session2 = f2.awaitDoneAndGet();
    IServerSession session1 = f1.awaitDoneAndGet();
    assertNotNull(session2);
    assertNotNull(session1);
    assertNotSame(session1, session2);
  }

  @Test
  public void testParallelRequestsWithSameIds() {
    final TestHttpSession httpSession1 = new TestHttpSession();
    final TestHttpSession httpSession2 = new TestHttpSession();
    final ServerSessionCache cache = BEANS.get(ServerSessionCache.class);

    final IServerSessionLifecycleHandler sessionSupplier1 = new IServerSessionLifecycleHandler() {

      @Override
      public IServerSession create() {
        return new AbstractServerSession(true) {
          private static final long serialVersionUID = 1L;
        };
      }

      @Override
      public void destroy(IServerSession session) {
      }

      @Override
      public String getId() {
        return "id1";
      }
    };

    final IServerSessionLifecycleHandler sessionSupplier2 = new IServerSessionLifecycleHandler() {

      @Override
      public IServerSession create() {
        return new AbstractServerSession(true) {
          private static final long serialVersionUID = 1L;
        };
      }

      @Override
      public void destroy(IServerSession session) {
      }

      @Override
      public String getId() {
        return "id1";
      }
    };
    IFuture<IServerSession> f1 = Jobs.schedule(new Callable<IServerSession>() {

      @Override
      public IServerSession call() throws Exception {
        return cache.getOrCreate(sessionSupplier1, httpSession1);
      }
    }, Jobs.newInput());
    IFuture<IServerSession> f2 = Jobs.schedule(new Callable<IServerSession>() {

      @Override
      public IServerSession call() throws Exception {
        return cache.getOrCreate(sessionSupplier2, httpSession2);
      }
    }, Jobs.newInput());

    IServerSession session2 = f2.awaitDoneAndGet();
    IServerSession session1 = f1.awaitDoneAndGet();
    assertSame(session1, session2);
  }

  class TestServerSessionLifecycleHandler implements IServerSessionLifecycleHandler {

    private String m_id;

    public TestServerSessionLifecycleHandler() {
      this(testSessionId);
    }

    public TestServerSessionLifecycleHandler(String id) {
      m_id = id;
    }

    @Override
    public String getId() {
      return m_id;
    }

    @Override
    public IServerSession create() {
      return new AbstractServerSession(true) {
        private static final long serialVersionUID = 1L;

      };
    }

    @Override
    public void destroy(IServerSession session) {
    }

  }

  class FixedServerSessionLifecycleHandler extends TestServerSessionLifecycleHandler {
    @Override
    public IServerSession create() {
      return m_testScoutSession;
    }
  }

}
