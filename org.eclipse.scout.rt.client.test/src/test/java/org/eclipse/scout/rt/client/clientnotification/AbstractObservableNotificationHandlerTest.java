package org.eclipse.scout.rt.client.clientnotification;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.session.SessionEvent;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link AbstractObservableNotificationHandler}
 */
@RunWith(PlatformTestRunner.class)
public class AbstractObservableNotificationHandlerTest {
  private final String m_testNotification = "testNotification";
  private final IClientSession m_testSession1 = new AbstractClientSession(false) {
  };
  private final IClientSession m_testSession2 = new AbstractClientSession(false) {
  };

  @Test
  public void testAddListener_SingleSession() {
    AbstractObservableNotificationHandler<Serializable> testHandler = createGlobalHandler();
    final CountCondition cc = new CountCondition(1);
    testHandler.addListener(m_testSession1, createVerifyingListener(cc, m_testSession1));
    testHandler.notifyListenersOfAllSessions(m_testNotification);
    cc.waitFor();
  }

  @Test
  public void testMultipleListeners_SingleSession() {
    AbstractObservableNotificationHandler<Serializable> testHandler = createGlobalHandler();

    final CountCondition cc = new CountCondition(2);
    testHandler.addListener(m_testSession1, createVerifyingListener(cc, m_testSession1));
    testHandler.addListener(m_testSession1, createVerifyingListener(cc, m_testSession1));
    testHandler.notifyListenersOfAllSessions(m_testNotification);
    cc.waitFor();
  }

  @Test
  public void testMultipleSessions() {
    AbstractObservableNotificationHandler<Serializable> testHandler = createGlobalHandler();

    final CountCondition cc = new CountCondition(2);
    testHandler.addListener(m_testSession1, createVerifyingListener(cc, m_testSession1));
    testHandler.addListener(m_testSession2, createVerifyingListener(cc, m_testSession2));
    testHandler.notifyListenersOfAllSessions(m_testNotification);
    cc.waitFor();
  }

  @Test
  public void testListenerInModelJob_SingleSession() {
    final AbstractObservableNotificationHandler<Serializable> testHandler = createGlobalHandler();

    ModelJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final CountCondition cc = new CountCondition(1);
        testHandler.addListener(createVerifyingListener(cc, m_testSession1));
        testHandler.notifyListenersOfAllSessions(m_testNotification);
        cc.waitFor();
      }
    }, ModelJobs
        .newInput(ClientRunContexts.empty().withSession(m_testSession1, true)))
        .awaitDone();
  }

  @Test
  public void testListenerInModelJob_MultipleSessions() {
    final AbstractObservableNotificationHandler<Serializable> testHandler = createGlobalHandler();

    ModelJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final CountCondition cc = new CountCondition(2);
        testHandler.addListener(m_testSession1, createVerifyingListener(cc, m_testSession1));
        testHandler.addListener(m_testSession2, createVerifyingListener(cc, m_testSession2));
        testHandler.notifyListenersOfAllSessions(m_testNotification);
        assertEquals(m_testSession1, IClientSession.CURRENT.get());
        cc.waitFor();
      }
    }, ModelJobs
        .newInput(ClientRunContexts.empty().withSession(m_testSession1, true)))
        .awaitDone();
  }

  private INotificationListener<Serializable> createVerifyingListener(final CountCondition cc, final IClientSession session) {
    return new INotificationListener<Serializable>() {

      @Override
      public void handleNotification(Serializable notification) {
        assertEquals(session, IClientSession.CURRENT.get());
        assertEquals(m_testNotification, notification);
        cc.countDown();
      }
    };
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAddingListeners() {
    final AbstractObservableNotificationHandler<Serializable> testHandler = createGlobalHandler();
    testHandler.addListener(m_testSession1, mock(INotificationListener.class));
    testHandler.addListener(m_testSession1, mock(INotificationListener.class));
    testHandler.addListener(m_testSession2, mock(INotificationListener.class));
    assertEquals(2, testHandler.getListeners(m_testSession1).size());
    assertEquals(1, testHandler.getListeners(m_testSession2).size());
  }

  @Test
  public void testRemove_SingleListener() {
    final AbstractObservableNotificationHandler<Serializable> testHandler = createGlobalHandler();
    @SuppressWarnings("unchecked")
    INotificationListener<Serializable> l1 = mock(INotificationListener.class);
    testHandler.addListener(m_testSession1, l1);
    testHandler.removeListener(m_testSession1, l1);
    assertEquals(0, testHandler.getListeners(m_testSession1).size());
  }

  @Test
  public void testRemoveNonExisting() {
    final AbstractObservableNotificationHandler<Serializable> testHandler = createGlobalHandler();
    @SuppressWarnings("unchecked")
    INotificationListener<Serializable> l1 = mock(INotificationListener.class);
    testHandler.removeListener(m_testSession1, l1);
    assertEquals(0, testHandler.getListeners(m_testSession1).size());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRemoveListeners() {
    final AbstractObservableNotificationHandler<Serializable> testHandler = createGlobalHandler();
    INotificationListener l1 = mock(INotificationListener.class);
    INotificationListener l2 = mock(INotificationListener.class);
    testHandler.addListener(m_testSession1, l1);
    testHandler.addListener(m_testSession1, l2);
    testHandler.addListener(m_testSession2, l1);

    testHandler.removeListener(m_testSession1, l1);
    assertEquals(1, testHandler.getListeners(m_testSession1).size());
    assertEquals(1, testHandler.getListeners(m_testSession2).size());
  }

  @Test
  public void testSessionStopped() {
    final AbstractObservableNotificationHandler<Serializable> testHandler = createGlobalHandler();
    @SuppressWarnings("unchecked")
    INotificationListener<Serializable> l1 = mock(INotificationListener.class);
    testHandler.addListener(m_testSession1, l1);
    SessionEvent sessionStopEvent = new SessionEvent(m_testSession1, SessionEvent.TYPE_STOPPED);
    testHandler.sessionChanged(sessionStopEvent);
    assertEquals(0, testHandler.getListeners(m_testSession1).size());
  }

  class CountCondition {

    private final IBlockingCondition bc;
    private final AtomicInteger counter;

    public CountCondition(int maxCount) {
      bc = Jobs.newBlockingCondition(true);
      counter = new AtomicInteger(maxCount);
    }

    public void countDown() {
      bc.setBlocking(counter.decrementAndGet() > 0);
    }

    void waitFor() {
      bc.waitFor(1, TimeUnit.SECONDS);
    }

  }

  private AbstractObservableNotificationHandler<Serializable> createGlobalHandler() {
    return new AbstractObservableNotificationHandler<Serializable>() {
    };
  }

}
