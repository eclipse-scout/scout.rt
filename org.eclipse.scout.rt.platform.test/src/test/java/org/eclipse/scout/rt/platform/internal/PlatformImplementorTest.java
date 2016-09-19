package org.eclipse.scout.rt.platform.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @since 5.2
 */
public class PlatformImplementorTest {

  private static ExecutorService s_executor;

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newSingleThreadExecutor();
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdownNow();
  }

  @Test
  public void testAwaitPlatformStartedFailsInBeanManagerPrepared() throws Exception {
    doTestAwaitPlatformStartedFailsInState(State.BeanManagerPrepared);
  }

  @Test
  public void testAwaitPlatformStartedFailsInBeanManagerValid() throws Exception {
    doTestAwaitPlatformStartedFailsInState(State.BeanManagerValid);
  }

  @Test
  public void testAwaitPlatformStartedFailsInPlatformStarted() throws Exception {
    doTestAwaitPlatformStartedFailsInState(State.PlatformStarted);
  }

  @Test
  public void testAwaitPlatformStarted() throws Exception {
    doTestAwaitPlatformStartedFailsInState(null);
  }

  protected void doTestAwaitPlatformStartedFailsInState(final State platformStartFailsInState) throws Exception {
    // create platform listener that fails in given state
    BeanMetaData bean = new BeanMetaData(IPlatformListener.class)
        .withApplicationScoped(true)
        .withInitialInstance(new IPlatformListener() {
          @Override
          public void stateChanged(PlatformEvent event) {
            if (event.getState() == platformStartFailsInState) {
              throw new TestingPlatformStartupException();
            }
          }
        });

    // create platform instance and start it in another thread
    final TestingPlatformImplementor platform = new TestingPlatformImplementor(bean);
    Future<?> platformStartFuture = startPlatformInAnotherThread(platform);

    // current thread is expected to be suspended
    assertAwaitPlatformStarted(platform, platformStartFailsInState == null);

    // wait until platform starter has been finished and either expect no failures or the intended startup exception
    if (platformStartFailsInState == null) {
      platformStartFuture.get();
    }
    else {
      try {
        platformStartFuture.get();
        fail("Platform is not expected to be started");
      }
      catch (ExecutionException e) {
        assertSame(TestingPlatformStartupException.class, e.getCause().getClass());
      }
    }

    // again awaitPlatformStartedFails
    assertAwaitPlatformStarted(platform, platformStartFailsInState == null);
  }

  @Test
  public void testAwaitPlatformStartedFailsInPlatformStopping() throws Exception {
    final CountDownLatch stoppingLatch = new CountDownLatch(1);
    final CountDownLatch continueStoppingLatch = new CountDownLatch(1);

    // crate platform listener that signals stopping state
    BeanMetaData bean = new BeanMetaData(IPlatformListener.class)
        .withApplicationScoped(true)
        .withInitialInstance(new IPlatformListener() {
          @Override
          public void stateChanged(PlatformEvent event) {
            if (event.getState() == State.PlatformStopping) {
              stoppingLatch.countDown();
              try {
                continueStoppingLatch.await();
              }
              catch (InterruptedException e) {
                // nop
              }
            }
          }
        });

    final TestingPlatformImplementor platform = new TestingPlatformImplementor(bean);
    platform.start();

    // expect platform started
    platform.awaitPlatformStarted();

    Future<?> platformStopFuture = s_executor.submit(new Runnable() {
      @Override
      public void run() {
        platform.stop();
      }
    });

    stoppingLatch.await();

    try {
      platform.awaitPlatformStarted();
    }
    catch (PlatformException e) {
      assertEquals("The platform is stopping.", e.getMessage());
    }

    continueStoppingLatch.countDown();
    platformStopFuture.get();
  }

  protected void assertAwaitPlatformStarted(final TestingPlatformImplementor platform, boolean expectingValidPlatform) {
    if (expectingValidPlatform) {
      platform.awaitPlatformStarted();
    }
    else {
      try {
        platform.awaitPlatformStarted();
        fail("Platform is not expected to be started");
      }
      catch (PlatformException e) {
        assertEquals("The platform is in an invalid state.", e.getMessage());
      }
    }
  }

  protected Future<?> startPlatformInAnotherThread(final TestingPlatformImplementor platform) {
    return s_executor.submit(new Runnable() {
      @Override
      public void run() {
        platform.start();
      }
    });
  }

  private static class TestingPlatformImplementor extends PlatformImplementor {

    private final BeanMetaData[] m_initialBeans;

    public TestingPlatformImplementor(BeanMetaData... initialBeans) {
      m_initialBeans = initialBeans;
    }

    @Override
    protected BeanManagerImplementor createBeanManager() {
      BeanManagerImplementor beanManager = new BeanManagerImplementor();
      for (BeanMetaData bean : m_initialBeans) {
        beanManager.registerBean(bean);
      }
      return beanManager;
    }

    @Override
    protected void validateConfiguration() {
      // do not validate
    }
  }

  private static class TestingPlatformStartupException extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }
}
