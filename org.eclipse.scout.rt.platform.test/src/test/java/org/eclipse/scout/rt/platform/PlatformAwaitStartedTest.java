package org.eclipse.scout.rt.platform;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.platform.internal.PlatformStarter;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that an awaitPlatformStarted called while the platform is started but not all
 */
public class PlatformAwaitStartedTest {

  @Test
  public void testAwaitPlatformStarted() {
    final FixturePlatformWithStartListener platform = new FixturePlatformWithStartListener();
    new PlatformStarter(platform).start();
    try {
      platform.m_asyncFinished.await(1, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      throw new PlatformException("interrupted waiting for asyncFinished", e);
    }
    Assert.assertArrayEquals(new Object[]{"asyncStart", "listenerFinished", "asyncFinished"}, platform.m_events.toArray());
  }

  private static class Async extends Thread {
    private final IPlatform m_platform;
    private final CountDownLatch m_asyncStarted;
    private final CountDownLatch m_asyncFinished;
    private final Queue<String> m_events;

    public Async(IPlatform source, CountDownLatch asyncStarted, CountDownLatch asyncFinished, Queue<String> events) {
      m_platform = source;
      m_asyncStarted = asyncStarted;
      m_asyncFinished = asyncFinished;
      m_events = events;
    }

    @Override
    public void run() {
      m_events.add("asyncStart");
      m_asyncStarted.countDown();
      m_platform.awaitPlatformStarted();
      m_events.add("asyncFinished");
      m_asyncFinished.countDown();
    }
  }

  private static class FirstStartupListener implements IPlatformListener {

    private final CountDownLatch m_asyncStarted;
    private final CountDownLatch m_asyncFinished;
    private final Queue<String> m_events;

    public FirstStartupListener(CountDownLatch asyncStarted, CountDownLatch asyncFinished, Queue<String> events) {
      m_asyncStarted = asyncStarted;
      m_asyncFinished = asyncFinished;
      m_events = events;
    }

    @Override
    public void stateChanged(PlatformEvent event) {
      if (event.getState() == State.PlatformStarted) {
        new Async(event.getSource(), m_asyncStarted, m_asyncFinished, m_events).start();
        try {
          m_asyncStarted.await(1, TimeUnit.MINUTES);
        }
        catch (InterruptedException e) {
          throw new PlatformException("interrupted waiting for asyncStarted", e);
        }
        SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, m_asyncFinished.getCount()); // assert the async thread has not finished. It must wait until all the listeners have been executed.
        m_events.add("listenerFinished");
      }
    }
  }

  private static class FixturePlatformWithStartListener extends DefaultPlatform {
    final CountDownLatch m_asyncStarted = new CountDownLatch(1);
    final CountDownLatch m_asyncFinished = new CountDownLatch(1);
    final Queue<String> m_events = new ConcurrentLinkedQueue<>();

    @Override
    protected BeanManagerImplementor createBeanManager() {
      BeanManagerImplementor context = new BeanManagerImplementor();
      context.registerBean(
          new BeanMetaData(FirstStartupListener.class)
              .withApplicationScoped(true)
              .withInitialInstance(new FirstStartupListener(m_asyncStarted, m_asyncFinished, m_events))
              .withOrder(100));
      return context;
    }
  }
}
