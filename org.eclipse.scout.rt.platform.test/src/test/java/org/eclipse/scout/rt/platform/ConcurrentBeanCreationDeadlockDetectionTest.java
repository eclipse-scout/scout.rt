package org.eclipse.scout.rt.platform;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.exception.BeanCreationException;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.internal.DefaultBeanInstanceProducer;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests deadlock detection during concurrent access on yet not created application-scoped beans.
 *
 * @since 6.1
 */
@RunWith(PlatformTestRunner.class)
public class ConcurrentBeanCreationDeadlockDetectionTest {

  private static CountDownLatch s_delayLatch = new CountDownLatch(1);
  private static CountDownLatch s_beanInitializingLatch = new CountDownLatch(1);

  private List<IBean<?>> m_beans;

  @Before
  public void before() {
    s_delayLatch = new CountDownLatch(1);
    s_beanInitializingLatch = new CountDownLatch(1);

    m_beans = new ArrayList<>();
    m_beans.add(registerBean(ConcurrentConstructorTestBean.class));
    m_beans.add(registerBean(ConcurrentPostConstructTestBean.class));
    m_beans.add(registerBean(ConcurrentConstructorSelfReferencingTestBean.class));
    m_beans.add(registerBean(ConcurrentPostConstructSelfReferencingTestBean.class));
  }

  protected <T> IBean<T> registerBean(Class<T> beanClass) {
    return Platform.get().getBeanManager().registerBean(
        new BeanMetaData(beanClass)
            .withApplicationScoped(true)
            .withProducer(new DefaultBeanInstanceProducer<T>() {
              @Override
              protected int getDeadlockDetectionMaxWaitTimeSeconds() {
                return 1;
              }
            }));
  }

  @After
  public void after() {
    for (IBean<?> bean : m_beans) {
      Platform.get().getBeanManager().unregisterBean(bean);
    }
    m_beans = null;
  }

  @Test(timeout = 2500)
  public void testConcurrentConstructorSingleThread() throws Exception {
    testConcurrentTestBeanSingleThread(ConcurrentConstructorTestBean.class);
  }

  @Test(timeout = 2500)
  public void testConcurrentPostConstructSingleThread() throws Exception {
    testConcurrentTestBeanSingleThread(ConcurrentPostConstructTestBean.class);
  }

  @Test(timeout = 2500)
  public void testConcurrentConstructorWithTwoIndependentThreads() throws Exception {
    testConcurrentTestBeanWithTwoIndependentThreads(ConcurrentConstructorTestBean.class);
  }

  @Test(timeout = 2500)
  public void testConcurrentPostConstructWithTwoIndependentThreads() throws Exception {
    testConcurrentTestBeanWithTwoIndependentThreads(ConcurrentPostConstructTestBean.class);
  }

  @Test(timeout = 2500)
  public void testConcurrentConstructorWithTwoDependingThreads() throws Exception {
    testConcurrentSelfReferencingTestBean(ConcurrentConstructorSelfReferencingTestBean.class);
  }

  @Test(timeout = 2500)
  public void testConcurrentPostConstructWithTwoDependingThreads() throws Exception {
    testConcurrentSelfReferencingTestBean(ConcurrentPostConstructSelfReferencingTestBean.class);
  }

  protected <T extends AbstractConcurrentTestBean> void testConcurrentTestBeanSingleThread(final Class<T> type) throws Exception {
    IFuture<T> future = Jobs.schedule(new Callable<T>() {
      @Override
      public T call() throws Exception {
        return BEANS.get(type);
      }
    }, Jobs.newInput());

    s_beanInitializingLatch.await();
    s_delayLatch.countDown();

    T bean = future.awaitDoneAndGet();
    assertNotNull(bean);
    assertTrue(bean.isInitialized());
  }

  protected <T extends AbstractConcurrentTestBean> void testConcurrentTestBeanWithTwoIndependentThreads(final Class<T> type) throws Exception {
    IFuture<T> future1 = Jobs.schedule(new Callable<T>() {
      @Override
      public T call() throws Exception {
        return BEANS.get(type);
      }
    }, Jobs.newInput());

    s_beanInitializingLatch.await();

    IFuture<T> future2 = Jobs.schedule(new Callable<T>() {
      @Override
      public T call() throws Exception {
        return BEANS.get(type);
      }
    }, Jobs.newInput());

    T bean2;
    try {
      bean2 = future2.awaitDoneAndGet(50, TimeUnit.MILLISECONDS);
      fail();
    }
    catch (TimedOutError e) {
      // expected
    }

    s_delayLatch.countDown();

    T bean1 = future1.awaitDoneAndGet();
    bean2 = future2.awaitDoneAndGet(50, TimeUnit.MILLISECONDS);
    assertNotNull(bean1);
    assertNotNull(bean2);
    assertSame(bean1, bean2);
    assertTrue(bean1.isInitialized());
  }

  protected <T extends AbstractConcurrentSelfReferencingTestBean> void testConcurrentSelfReferencingTestBean(final Class<T> type) throws Exception {
    IFuture<T> future1 = Jobs.schedule(new Callable<T>() {
      @Override
      public T call() throws Exception {
        return BEANS.get(type);
      }
    }, Jobs.newInput());

    s_beanInitializingLatch.await();
    s_delayLatch.countDown();

    T bean = future1.awaitDoneAndGet();

    assertNotNull(bean);
    assertTrue(bean.isInitialized());
    AbstractConcurrentSelfReferencingTestBean otherBean = bean.getOtherBean();
    assertNull(otherBean);
  }

  /* ==============================================================================
   * Test beans
   * ==============================================================================
   */
  public static abstract class AbstractConcurrentTestBean {
    private final AtomicBoolean m_initialized = new AtomicBoolean();

    protected void initialize() {
      try {
        s_beanInitializingLatch.countDown();
        s_delayLatch.await();
        m_initialized.set(true);
      }
      catch (InterruptedException e) {
        throw new PlatformException("interrupted while waiting on latch", e);
      }
    }

    public boolean isInitialized() {
      return m_initialized.get();
    }
  }

  public static class ConcurrentConstructorTestBean extends AbstractConcurrentTestBean {
    public ConcurrentConstructorTestBean() {
      initialize();
    }
  }

  public static class ConcurrentPostConstructTestBean extends AbstractConcurrentTestBean {
    @PostConstruct
    private void postConstruct() {
      initialize();
    }
  }

  public static abstract class AbstractConcurrentSelfReferencingTestBean {
    private final AtomicBoolean m_initialized = new AtomicBoolean();
    private AbstractConcurrentSelfReferencingTestBean m_otherBean;

    protected void initialize() {
      try {
        s_beanInitializingLatch.countDown();
        m_otherBean = Jobs.schedule(new Callable<AbstractConcurrentSelfReferencingTestBean>() {
          @Override
          public AbstractConcurrentSelfReferencingTestBean call() throws Exception {
            return BEANS.get(AbstractConcurrentSelfReferencingTestBean.this.getClass());
          }
        }, Jobs
            .newInput()
            .withExceptionHandling(new ExceptionHandler(), false))
            .awaitDoneAndGet();
        fail("job is not expected to complete");
      }
      catch (BeanCreationException e) {
        // expected
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      assertNull(m_otherBean);
      m_initialized.set(true);
    }

    public boolean isInitialized() {
      return m_initialized.get();
    }

    public AbstractConcurrentSelfReferencingTestBean getOtherBean() {
      return m_otherBean;
    }
  }

  public static class ConcurrentConstructorSelfReferencingTestBean extends AbstractConcurrentSelfReferencingTestBean {
    public ConcurrentConstructorSelfReferencingTestBean() {
      initialize();
    }
  }

  public static class ConcurrentPostConstructSelfReferencingTestBean extends AbstractConcurrentSelfReferencingTestBean {
    @PostConstruct
    private void postConstruct() {
      initialize();
    }
  }
}
