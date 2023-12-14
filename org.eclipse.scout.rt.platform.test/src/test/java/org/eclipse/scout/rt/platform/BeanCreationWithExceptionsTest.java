/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.exception.BeanCreationException;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests bean creation with constructor and / or {@link PostConstruct} methods throwing exceptions.
 *
 * @since 6.1
 */
@RunWith(PlatformTestRunner.class)
@RunWithNewPlatform
public class BeanCreationWithExceptionsTest {

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
    f1.awaitDoneAndGet();
    f2.awaitDoneAndGet();
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
    state.assertInvocations(1, 0);
  }

  @Test
  public void testConcurrentExceptionInConstructorThenOk() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(true, false, true);
    state.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    state.constructorLatch.countDown();
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
    state.assertInvocations(1, 0);

    // disable exceptions, try again
    state.throwExceptionInPostConstruct = false;
    BEANS.get(TestBean.class);
    state.assertInvocations(2, 1);
  }

  @Test
  public void testConcurrentExceptionInConstructorThenInPostConstructThenOk() {
    TestBeanState state = BEANS.get(TestBeanState.class);
    state.reset(true, false, true);
    state.armLatch(true);
    IFuture<TestBean> f1 = scheduleGetBean();
    IFuture<TestBean> f2 = scheduleGetBean();
    state.constructorLatch.countDown();
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
    state.assertInvocations(0, 0);

    // disable constructor exception, enable postConstruct exception
    state.throwExceptionInConstructor = false;
    state.throwExceptionInPostConstruct = true;
    state.armLatch(true);
    f1 = scheduleGetBean();
    f2 = scheduleGetBean();
    state.constructorLatch.countDown();
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
    state.assertInvocations(1, 0);

    // disable exceptions, try again
    state.throwExceptionInPostConstruct = false;
    BEANS.get(TestBean.class);
    state.assertInvocations(2, 1);
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
