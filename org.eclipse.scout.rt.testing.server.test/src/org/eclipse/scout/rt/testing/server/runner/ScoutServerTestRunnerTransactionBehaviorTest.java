/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.server.runner;

import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.testenvironment.TestEnvironmentServerSession;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test verifies the transaction behavior of Scout server tests.
 * <ul>
 * <li>All methods annotated with {@link BeforeClass} are executed in the same Scout transaction</li>
 * <li>All methods annotated with {@link Before} or {@link After} are executed in the same Scout transaction as the test
 * method itself (i.e. the one annotated with {@link Test}).</li>
 * <li>All methods annotated with {@link AfterClass} are executed in the same Scout transaction</li>
 * </ul>
 */
@RunWith(ScoutServerTestRunner.class)
public class ScoutServerTestRunnerTransactionBehaviorTest {

  // before and after class transactions
  private static ITransaction s_beforeClassTransaction;
  private static ITransaction s_afterClassTransaction;

  // before and after test transactions. The content is switched in method checkTestTransactionBehavior.
  private static ITransaction s_thisTestBeforeTransaction;
  private static ITransaction s_thisTestAfterTransaction;

  // the transaction the other test is compared to
  private static ITransaction s_otherTestTransaction;

  @BeforeClass
  public static void beforeClass1() throws Exception {
    checkBeforeClassTransactionBehavior();
  }

  @BeforeClass
  public static void beforeClass2() throws Exception {
    checkBeforeClassTransactionBehavior();
  }

  @AfterClass
  public static void afterClass1() throws Exception {
    checkAfterClassTransactionBehavior();
  }

  @AfterClass
  public static void afterClass2() throws Exception {
    checkAfterClassTransactionBehavior();
  }

  @Before
  public void before1() throws Exception {
    checkBeforeTransactionBehavior();
  }

  @Before
  public void before2() throws Exception {
    checkBeforeTransactionBehavior();
  }

  @After
  public void after1() throws Exception {
    checkAfterTransactionBehavior();
  }

  @After
  public void after2() throws Exception {
    checkAfterTransactionBehavior();
  }

  @Test
  public void test1() {
    checkTestTransactionBehavior();
  }

  @Test
  public void test2() {
    checkTestTransactionBehavior();
  }

  /**
   * All methods annotated with @BeforeClass are expected to be executed in the same Scout transaction.
   */
  private static void checkBeforeClassTransactionBehavior() {
    Assert.assertNotNull(TestEnvironmentServerSession.get());
    Assert.assertNotNull(ThreadContext.getTransaction());
    Assert.assertTrue(s_beforeClassTransaction == null || s_beforeClassTransaction == ThreadContext.getTransaction());
    s_beforeClassTransaction = ThreadContext.getTransaction();
  }

  /**
   * All methods annotated with @BeforeClass are expected to be executed in the same Scout transaction.
   */
  private static void checkAfterClassTransactionBehavior() {
    Assert.assertNotNull(TestEnvironmentServerSession.get());
    Assert.assertNotNull(ThreadContext.getTransaction());
    Assert.assertNotSame(s_beforeClassTransaction, ThreadContext.getTransaction());
    Assert.assertTrue(s_afterClassTransaction == null || s_afterClassTransaction == ThreadContext.getTransaction());
    s_afterClassTransaction = ThreadContext.getTransaction();
  }

  /**
   * All methods annotated with @Before are expected to be executed in the same Scout transaction.
   */
  private static void checkBeforeTransactionBehavior() {
    Assert.assertNotNull(TestEnvironmentServerSession.get());
    Assert.assertNotNull(ThreadContext.getTransaction());
    Assert.assertNotSame(s_beforeClassTransaction, ThreadContext.getTransaction());
    Assert.assertTrue(s_thisTestBeforeTransaction == null || s_thisTestBeforeTransaction == ThreadContext.getTransaction());
    Assert.assertTrue(s_otherTestTransaction == null || s_otherTestTransaction != ThreadContext.getTransaction());
    s_thisTestBeforeTransaction = ThreadContext.getTransaction();
  }

  /**
   * All methods annotated with @After are expected to be executed in the same Scout transaction.
   */
  private static void checkAfterTransactionBehavior() {
    Assert.assertNotNull(TestEnvironmentServerSession.get());
    Assert.assertNotNull(ThreadContext.getTransaction());
    Assert.assertNotSame(s_beforeClassTransaction, ThreadContext.getTransaction());
    Assert.assertSame(s_thisTestAfterTransaction, ThreadContext.getTransaction());
  }

  /**
   * Every test is expected to run in its own Scout transaction.
   */
  private void checkTestTransactionBehavior() {
    Assert.assertNotNull(TestEnvironmentServerSession.get());
    Assert.assertNotNull(ThreadContext.getTransaction());
    Assert.assertNotSame(s_beforeClassTransaction, ThreadContext.getTransaction());
    Assert.assertTrue(s_otherTestTransaction == null || s_otherTestTransaction != ThreadContext.getTransaction());
    s_otherTestTransaction = ThreadContext.getTransaction();
    s_thisTestAfterTransaction = s_thisTestBeforeTransaction;
    s_thisTestBeforeTransaction = null;
  }
}
