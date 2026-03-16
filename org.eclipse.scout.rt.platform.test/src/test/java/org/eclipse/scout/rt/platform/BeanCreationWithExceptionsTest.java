/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import static org.junit.Assert.*;

import java.io.Serial;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.exception.BeanCreationException;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests bean creation with constructor and / or {@link PostConstruct} methods throwing exceptions.
 *
 * @since 6.1
 */
@RunWith(PlatformTestRunner.class)
@RunWithNewPlatform
public class BeanCreationWithExceptionsTest {

  private static final Logger LOG = LoggerFactory.getLogger(BeanCreationWithExceptionsTest.class);

  @Before
  public void before() {
    Platform.get().getBeanManager().registerClass(TestBeanState.class);
    Platform.get().getBeanManager().registerClass(TestBean.class);
  }

  @After
  public void after() {
    Platform.get().getBeanManager().unregisterClass(TestBeanState.class);
    Platform.get().getBeanManager().unregisterClass(TestBean.class);
  }

  @Test
  public void testSuccessfulConstruction() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(false, false, false);
    BEANS.get(TestBean.class);
    state.assertInvocations(1, 1);
  }

  @Test
  public void testExceptionInConstructor() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(true, false, false);
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(true, e);
    }
    state.assertInvocations(0, 0);
  }

  @Test
  public void testExceptionInPostConstruct() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(false, true, false);
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(false, e);
    }
    state.assertInvocations(1, 0);
  }

  @Test
  public void testExceptionInConstructorThenOk() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(true, false, false);
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(true, e);
    }
    state.assertInvocations(0, 0);

    // disable exceptions, try again
    state.throwExceptionInConstructor = false;
    BEANS.get(TestBean.class);
    state.assertInvocations(1, 1);
  }

  @Test
  public void testExceptionInPostConstructThenOk() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(false, true, false);
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(false, e);
    }
    state.assertInvocations(1, 0);

    // disable exceptions, try again
    state.throwExceptionInPostConstruct = false;
    BEANS.get(TestBean.class);
    state.assertInvocations(2, 1);
  }

  @Test
  public void testExceptionInConstructorThenInPostConstructThenOk() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(true, false, false);
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(true, e);
    }
    state.assertInvocations(0, 0);

    // disable constructor exception, enable postConstruct exception
    state.throwExceptionInConstructor = false;
    state.throwExceptionInPostConstruct = true;
    try {
      BEANS.get(TestBean.class);
      fail("expecting exception");
    }
    catch (BeanCreationException e) {
      assertTestBeanException(false, e);
    }
    state.assertInvocations(1, 0);

    // disable exceptions, try again
    state.throwExceptionInPostConstruct = false;
    BEANS.get(TestBean.class);
    state.assertInvocations(2, 1);
  }

  @Test
  public void testConcurrentSuccessfulConstruction() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(false, false, true);
    state.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    state.constructorLatch.countDown();
    assertSame(f1.awaitDoneAndGet(), f2.awaitDoneAndGet());
    state.assertInvocations(1, 1);
  }

  @Test
  public void testConcurrentExceptionInConstructor() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(true, false, true);
    state.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    state.constructorLatch.countDown();
    assertTestBeanException(true,
        assertThrows(BeanCreationException.class, () -> f1.awaitDoneAndGet()),
        assertThrows(BeanCreationException.class, () -> f2.awaitDoneAndGet()));
    state.assertInvocations(0, 0);
  }

  @Test
  public void testConcurrentExceptionInPostConstruct() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(false, true, true);
    state.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    state.constructorLatch.countDown();
    assertTestBeanException(false,
        assertThrows(BeanCreationException.class, () -> f1.awaitDoneAndGet()),
        assertThrows(BeanCreationException.class, () -> f2.awaitDoneAndGet()));
    Assertions.assertGreaterOrEqual(state.numConstructorInvocations, 1);
    Assertions.assertLessOrEqual(state.numConstructorInvocations, 2); // in rare cases constructor may be called twice, e.g. if almost everything of f2 is only run after f1 is finished
    assertEquals(0, state.numPostConstructInvocations);
  }

  @Test
  public void testConcurrentExceptionInConstructorThenOk() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(true, false, true);
    state.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    state.constructorLatch.countDown();
    assertTestBeanException(true,
        assertThrows(BeanCreationException.class, () -> f1.awaitDoneAndGet()),
        assertThrows(BeanCreationException.class, () -> f2.awaitDoneAndGet()));
    state.assertInvocations(0, 0);

    // disable exceptions, try again
    state.throwExceptionInConstructor = false;
    BEANS.get(TestBean.class);
    state.assertInvocations(1, 1);
  }

  @Test
  public void testConcurrentExceptionInPostConstructThenOk() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(false, true, true);
    state.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    state.constructorLatch.countDown();
    assertTestBeanException(false,
        assertThrows(BeanCreationException.class, () -> f1.awaitDoneAndGet()),
        assertThrows(BeanCreationException.class, () -> f2.awaitDoneAndGet()));
    Assertions.assertGreaterOrEqual(state.numConstructorInvocations, 1);
    Assertions.assertLessOrEqual(state.numConstructorInvocations, 2); // in rare cases constructor may be called twice, e.g. if almost everything of f2 is only run after f1 is finished
    assertEquals(0, state.numPostConstructInvocations);

    // disable exceptions, try again
    state.throwExceptionInPostConstruct = false;
    BEANS.get(TestBean.class);
    Assertions.assertGreaterOrEqual(state.numConstructorInvocations, 2);
    Assertions.assertLessOrEqual(state.numConstructorInvocations, 3); // see above
    assertEquals(1, state.numPostConstructInvocations);
  }

  @Test
  public void testConcurrentExceptionInConstructorThenInPostConstructThenOk() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(true, false, true);
    state.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    state.constructorLatch.countDown();
    assertTestBeanException(true,
        assertThrows(BeanCreationException.class, () -> f1.awaitDoneAndGet()),
        assertThrows(BeanCreationException.class, () -> f2.awaitDoneAndGet()));
    state.assertInvocations(0, 0);

    // disable constructor exception, enable postConstruct exception
    state.throwExceptionInConstructor = false;
    state.throwExceptionInPostConstruct = true;
    state.armLatch(true);
    IFuture<TestBean> f3 = scheduleGetBean();
    IFuture<TestBean> f4 = scheduleGetBean();
    state.constructorLatch.countDown();
    assertTestBeanException(false,
        assertThrows(BeanCreationException.class, () -> f3.awaitDoneAndGet()),
        assertThrows(BeanCreationException.class, () -> f4.awaitDoneAndGet()));
    Assertions.assertGreaterOrEqual(state.numConstructorInvocations, 1);
    Assertions.assertLessOrEqual(state.numConstructorInvocations, 2); // in rare cases constructor may be called twice, e.g. if almost everything of f4 is only run after f3 is finished
    assertEquals(0, state.numPostConstructInvocations);

    // disable exceptions, try again
    state.throwExceptionInPostConstruct = false;
    BEANS.get(TestBean.class);
    Assertions.assertGreaterOrEqual(state.numConstructorInvocations, 2);
    Assertions.assertLessOrEqual(state.numConstructorInvocations, 3); // see above
    assertEquals(1, state.numPostConstructInvocations);
  }

  private static IFuture<TestBean> scheduleGetBean() {
    final CountDownLatch runningLatch = new CountDownLatch(1);
    IFuture<TestBean> future = Jobs.schedule(() -> {
      runningLatch.countDown();
      return BEANS.get(TestBean.class);
    }, Jobs.newInput().withExceptionHandling(new ExceptionHandler(), false));
    await(runningLatch);
    return future;
  }

  private static void assertTestBeanException(boolean expectedFromConstructor, BeanCreationException firstException, BeanCreationException secondException) {
    // at least one creation exception must have "our" TestBeanException as a cause, might be the first, the second or both of them
    // for a exception w/o a cause, it might also be a "Thread was waiting on bean instance creator thread which most likely failed" creation exception thrown in SingeltonBeanInstanceProducer.getOrCreateInstance(IBean<T>)
    // the exception w/o a cause is currently not further verified
    List<BeanCreationException> exceptionsWithCause = Stream.of(firstException, secondException)
        .filter(e -> e.getCause() != null)
        .toList();
    if (exceptionsWithCause.isEmpty()) {
      LOG.info("First exception", firstException);
      LOG.info("Second exception", secondException);
      fail("At least one exception must provide a cause, see log for further details on the two exceptions");
    }
    exceptionsWithCause.forEach(e -> assertTestBeanException(expectedFromConstructor, e));
  }

  private static void assertTestBeanException(boolean expectedFromConstructor, BeanCreationException e) {
    try {
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
    catch (AssertionError error) {
      LOG.error("Assertion error during test, unexpected exception - rethrowing assertion error", e);
      throw error;
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
  public static class TestBeanState {
    CountDownLatch constructorLatch;
    boolean throwExceptionInConstructor;
    boolean throwExceptionInPostConstruct;
    boolean concurrencyTest;
    int numConstructorInvocations;
    int numPostConstructInvocations;

    public TestBeanState() {
    }

    void reset(boolean throwInConstructor, boolean throwInPostConstruct, boolean concurrent) {
      constructorLatch = new CountDownLatch(0);
      throwExceptionInConstructor = throwInConstructor;
      throwExceptionInPostConstruct = throwInPostConstruct;
      concurrencyTest = concurrent;
      numConstructorInvocations = 0;
      numPostConstructInvocations = 0;
    }

    void armLatch(boolean constructorLatch) {
      if (constructorLatch) {
        this.constructorLatch = new CountDownLatch(1);
      }
    }

    void assertInvocations(int expectedConstructorInvocations, int expectedPostConstructInvocations) {
      assertEquals(expectedConstructorInvocations, numConstructorInvocations);
      assertEquals(expectedPostConstructInvocations, numPostConstructInvocations);
    }
  }

  @IgnoreBean
  @ApplicationScoped
  public static class TestBean {

    public TestBean() {
      TestBeanState state = BEANS.get(TestBeanState.class);
      await(state.constructorLatch);
      if (state.concurrencyTest) {
        // increase chance that bean is requested concurrently
        // unfortunately, there is no deterministic way to ensure that multiple calls are really executed in parallel
        SleepUtil.sleepSafe(500, TimeUnit.MILLISECONDS);
      }
      if (state.throwExceptionInConstructor) {
        throw new TestBeanException(true);
      }
      state.numConstructorInvocations++;
    }

    @PostConstruct
    private void postConstruct() {
      TestBeanState state = BEANS.get(TestBeanState.class);
      if (state.throwExceptionInPostConstruct) {
        throw new TestBeanException(false);
      }
      state.numPostConstructInvocations++;
    }
  }

  private static class TestBeanException extends PlatformException {

    @Serial
    private static final long serialVersionUID = 1L;
    private final boolean m_fromConstructor;

    public TestBeanException(boolean fromConstructor) {
      super("fixture exception from {}", fromConstructor ? "constructor" : "@PostConstruct");
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
