package org.eclipse.scout.rt.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.exception.BeanCreationException;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests bean creation with constructor and / or {@link PostConstruct} methods throwing exceptions.
 *
 * @since 6.1
 */
public class BeanCreationWithExceptionsTest {

  private IBean<TestBean> m_testBean;

  @Before
  public void before() {
    m_testBean = Platform.get().getBeanManager().registerClass(TestBean.class);
  }

  @After
  public void after() {
    Platform.get().getBeanManager().unregisterBean(m_testBean);
    m_testBean = null;
  }

  @Test
  public void testSuccessfulConstruction() {
    TestBean.reset(false, false);
    BEANS.get(TestBean.class);
    TestBean.assertInvocations(1, 1);
  }

  @Test
  public void testExceptionInConstructor() {
    TestBean.reset(true, false);
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(true, e);
    }
    TestBean.assertInvocations(0, 0);
  }

  @Test
  public void testExceptionInPostConstruct() {
    TestBean.reset(false, true);
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(false, e);
    }
    TestBean.assertInvocations(1, 0);
  }

  @Test
  public void testExceptionInConstructorThenOk() {
    TestBean.reset(true, false);
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(true, e);
    }
    TestBean.assertInvocations(0, 0);

    // disable exceptions, try again
    TestBean.s_throwExceptionInConstructor = false;
    BEANS.get(TestBean.class);
    TestBean.assertInvocations(1, 1);
  }

  @Test
  public void testExceptionInPostConstructThenOk() {
    TestBean.reset(false, true);
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(false, e);
    }
    TestBean.assertInvocations(1, 0);

    // disable exceptions, try again
    TestBean.s_throwExceptionInPostConstruct = false;
    BEANS.get(TestBean.class);
    TestBean.assertInvocations(2, 1);
  }

  @Test
  public void testExceptionInConstructorThenInPostConstructThenOk() {
    TestBean.reset(true, false);
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(true, e);
    }
    TestBean.assertInvocations(0, 0);

    // disable constructor exception, enable postConstruct exception
    TestBean.s_throwExceptionInConstructor = false;
    TestBean.s_throwExceptionInPostConstruct = true;
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(false, e);
    }
    TestBean.assertInvocations(1, 0);

    // disable exceptions, try again
    TestBean.s_throwExceptionInPostConstruct = false;
    BEANS.get(TestBean.class);
    TestBean.assertInvocations(2, 1);
  }

  @Test
  public void testConcurrentSuccessfulConstruction() {
    TestBean.reset(false, false);
    TestBean.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    TestBean.s_constructorLatch.countDown();
    f1.awaitDoneAndGet();
    f2.awaitDoneAndGet();
    TestBean.assertInvocations(1, 1);
  }

  @Test
  public void testConcurrentExceptionInConstructor() {
    TestBean.reset(true, false);
    TestBean.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    TestBean.s_constructorLatch.countDown();
    try {
      f1.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(true, e);
    }
    try {
      f2.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      // expected
    }
    TestBean.assertInvocations(0, 0);
  }

  @Test
  public void testConcurrentExceptionInPostConstruct() {
    TestBean.reset(false, true);
    TestBean.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    TestBean.s_constructorLatch.countDown();
    try {
      f1.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(false, e);
    }
    try {
      f2.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      // expected
    }
    TestBean.assertInvocations(1, 0);
  }

  @Test
  public void testConcurrentExceptionInConstructorThenOk() {
    TestBean.reset(true, false);
    TestBean.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    TestBean.s_constructorLatch.countDown();
    try {
      f1.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(true, e);
    }
    try {
      f2.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      // expected
    }
    TestBean.assertInvocations(0, 0);

    // disable exceptions, try again
    TestBean.s_throwExceptionInConstructor = false;
    BEANS.get(TestBean.class);
    TestBean.assertInvocations(1, 1);
  }

  @Test
  public void testConcurrentExceptionInPostConstructThenOk() {
    TestBean.reset(false, true);
    TestBean.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    TestBean.s_constructorLatch.countDown();
    try {
      f1.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(false, e);
    }
    try {
      f2.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      // expected
    }
    TestBean.assertInvocations(1, 0);

    // disable exceptions, try again
    TestBean.s_throwExceptionInPostConstruct = false;
    BEANS.get(TestBean.class);
    TestBean.assertInvocations(2, 1);
  }

  @Test
  public void testConcurrentExceptionInConstructorThenInPostConstructThenOk() {
    TestBean.reset(true, false);
    TestBean.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    TestBean.s_constructorLatch.countDown();
    try {
      f1.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(true, e);
    }
    try {
      f2.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      // expected
    }
    TestBean.assertInvocations(0, 0);

    // disable constructor exception, enable postConstruct exception
    TestBean.s_throwExceptionInConstructor = false;
    TestBean.s_throwExceptionInPostConstruct = true;
    TestBean.armLatch(true);
    f1 = scheduleGetBean();
    f2 = scheduleGetBean();
    TestBean.s_constructorLatch.countDown();
    try {
      f1.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(false, e);
    }
    try {
      f2.awaitDoneAndGet();
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      // expected
    }
    TestBean.assertInvocations(1, 0);

    // disable exceptions, try again
    TestBean.s_throwExceptionInPostConstruct = false;
    BEANS.get(TestBean.class);
    TestBean.assertInvocations(2, 1);
  }

  private static IFuture<TestBean> scheduleGetBean() {
    final CountDownLatch runningLatch = new CountDownLatch(1);
    IFuture<TestBean> future = Jobs.schedule(new Callable<TestBean>() {
      @Override
      public TestBean call() throws Exception {
        runningLatch.countDown();
        return BEANS.get(TestBean.class);
      }
    }, Jobs.newInput());
    await(runningLatch);
    return future;
  }

  private static void assertTestBeanException(boolean expectedFromConstructor, BeanCreationException e) {
    assertNotNull(e.getCause());
    assertSame(TestBeanException.class, e.getCause().getClass());
    TestBeanException tbe = (TestBeanException) e.getCause();
    if (expectedFromConstructor) {
      assertTrue(tbe.isFromConstructor());
    }
    else {
      assertTrue(tbe.isFromPostConstruct());
    }
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await();
    }
    catch (InterruptedException e) {
      throw new ThreadInterruptedError("interrupted");
    }
  }

  @IgnoreBean
  @ApplicationScoped
  public static class TestBean {
    static CountDownLatch s_constructorLatch;
    static boolean s_throwExceptionInConstructor;
    static boolean s_throwExceptionInPostConstruct;
    static int s_numConstructorInvocations;
    static int s_numPostConstructInvocations;

    static void reset(boolean throwInConstructor, boolean throwInPostConstruct) {
      s_constructorLatch = new CountDownLatch(0);
      s_throwExceptionInConstructor = throwInConstructor;
      s_throwExceptionInPostConstruct = throwInPostConstruct;
      s_numConstructorInvocations = 0;
      s_numPostConstructInvocations = 0;
    }

    static void armLatch(boolean constructorLatch) {
      if (constructorLatch) {
        s_constructorLatch = new CountDownLatch(1);
      }
    }

    static void assertInvocations(int expectedConstructorInvocations, int expectedPostConstructInvocations) {
      assertEquals(expectedConstructorInvocations, s_numConstructorInvocations);
      assertEquals(expectedPostConstructInvocations, s_numPostConstructInvocations);
    }

    public TestBean() {
      await(s_constructorLatch);
      if (s_throwExceptionInConstructor) {
        throw new TestBeanException(true);
      }
      s_numConstructorInvocations++;
    }

    @PostConstruct
    private void postConstruct() {
      if (s_throwExceptionInPostConstruct) {
        throw new TestBeanException(false);
      }
      s_numPostConstructInvocations++;
    }
  }

  private static class TestBeanException extends PlatformException {

    private static final long serialVersionUID = 1L;
    private final boolean m_fromConstructor;

    public TestBeanException(boolean fromConstructor) {
      super("expected exception from {}", fromConstructor ? "constructor" : "@PostConstruct");
      m_fromConstructor = fromConstructor;
    }

    public boolean isFromConstructor() {
      return m_fromConstructor;
    }

    public boolean isFromPostConstruct() {
      return !m_fromConstructor;
    }
  }
}
