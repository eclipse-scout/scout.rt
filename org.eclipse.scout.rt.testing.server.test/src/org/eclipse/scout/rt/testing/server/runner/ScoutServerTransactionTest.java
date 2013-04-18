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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.testing.server.runner.fixture.TestTransactionMember;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link ScoutServerTestRunner}
 */
@RunWith(ScoutServerTestRunner.class)
public class ScoutServerTransactionTest {

  @BeforeClass
  public static void beforeClassCheck() {
    assertTrue("This check ensures the before class runner from scout testing works. @See bug405846", true);
  }

  @AfterClass
  public static void afterClassCheck() {
    assertTrue("This check ensures the after class runner from scout testing works. @See bug405846", true);
  }

  @Test
  public void testValidTransactionMember() {
    final TestTransactionMember transactionMember = new TestTransactionMember("01");
    ServerJob job = new ServerJob("", ServerJob.getCurrentSession()) {

      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        ITransaction transaction = ThreadContext.getTransaction();
        transaction.registerMember(transactionMember);
        return Status.OK_STATUS;
      }
    };
    job.runNow(new NullProgressMonitor());
    assertEquals("CommitPhase1MethodCallCount", 1, transactionMember.getCommitPhase1MethodCallCount());
    assertEquals("CommitPhase2MethodCallCount", 1, transactionMember.getCommitPhase2MethodCallCount());
    assertEquals("ReleaseMethodCallCount", 1, transactionMember.getReleaseMethodCallCount());
    assertEquals("RollbackMethodCallCount", 0, transactionMember.getRollbackMethodCallCount());
  }

  @Test
  public void testTransactionWithoutParticipationOfAMember() {
    final TestTransactionMember transactionMember1 = new TestTransactionMember("01");
    final TestTransactionMember transactionMember2 = new TestTransactionMember("02") {
      @Override
      protected boolean execNeedsCommit() {

        return false;
      }
    };
    ServerJob job = new ServerJob("", ServerJob.getCurrentSession()) {

      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        ITransaction transaction = ThreadContext.getTransaction();
        transaction.registerMember(transactionMember1);
        transaction.registerMember(transactionMember2);
        return Status.OK_STATUS;
      }
    };
    job.runNow(new NullProgressMonitor());
    assertEquals("CommitPhase1MethodCallCount", 1, transactionMember1.getCommitPhase1MethodCallCount());
    assertEquals("CommitPhase2MethodCallCount", 1, transactionMember1.getCommitPhase2MethodCallCount());
    assertEquals("ReleaseMethodCallCount", 1, transactionMember1.getReleaseMethodCallCount());
    assertEquals("RollbackMethodCallCount", 0, transactionMember1.getRollbackMethodCallCount());
    assertEquals("CommitPhase1MethodCallCount", 0, transactionMember2.getCommitPhase1MethodCallCount());
    assertEquals("CommitPhase2MethodCallCount", 0, transactionMember2.getCommitPhase2MethodCallCount());
    assertEquals("ReleaseMethodCallCount", 1, transactionMember2.getReleaseMethodCallCount());
    assertEquals("RollbackMethodCallCount", 0, transactionMember2.getRollbackMethodCallCount());
  }

  @Test
  public void testTransactionWithFailureInMember2() {
    final TestTransactionMember transactionMember1 = new TestTransactionMember("01");
    final TestTransactionMember transactionMember2 = new TestTransactionMember("02") {
      @Override
      protected boolean execCommitPhase1() {
        return false;
      }

    };
    ServerJob job = new ServerJob("", ServerJob.getCurrentSession()) {

      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        ITransaction transaction = ThreadContext.getTransaction();
        transaction.registerMember(transactionMember1);
        transaction.registerMember(transactionMember2);
        return Status.OK_STATUS;
      }
    };
    job.runNow(new NullProgressMonitor());
    assertEquals("CommitPhase1MethodCallCount", 1, transactionMember1.getCommitPhase1MethodCallCount());
    assertEquals("CommitPhase2MethodCallCount", 0, transactionMember1.getCommitPhase2MethodCallCount());
    assertEquals("ReleaseMethodCallCount", 1, transactionMember1.getReleaseMethodCallCount());
    assertEquals("RollbackMethodCallCount", 1, transactionMember1.getRollbackMethodCallCount());
    assertEquals("CommitPhase1MethodCallCount", 1, transactionMember2.getCommitPhase1MethodCallCount());
    assertEquals("CommitPhase2MethodCallCount", 0, transactionMember2.getCommitPhase2MethodCallCount());
    assertEquals("ReleaseMethodCallCount", 1, transactionMember2.getReleaseMethodCallCount());
    assertEquals("RollbackMethodCallCount", 1, transactionMember2.getRollbackMethodCallCount());
  }

  @Test
  public void testTransactionWithExceptionInTransactionMember2() {
    final TestTransactionMember transactionMember1 = new TestTransactionMember("01");
    final TestTransactionMember transactionMember2 = new TestTransactionMember("02") {
      @Override
      protected boolean execCommitPhase1() {
        throw new NullPointerException();
      }

    };
    ServerJob job = new ServerJob("", ServerJob.getCurrentSession()) {

      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        ITransaction transaction = ThreadContext.getTransaction();
        transaction.registerMember(transactionMember1);
        transaction.registerMember(transactionMember2);
        return Status.OK_STATUS;
      }
    };
    job.runNow(new NullProgressMonitor());
    assertEquals("CommitPhase1MethodCallCount", 1, transactionMember1.getCommitPhase1MethodCallCount());
    assertEquals("CommitPhase2MethodCallCount", 0, transactionMember1.getCommitPhase2MethodCallCount());
    assertEquals("ReleaseMethodCallCount", 1, transactionMember1.getReleaseMethodCallCount());
    assertEquals("RollbackMethodCallCount", 1, transactionMember1.getRollbackMethodCallCount());
    assertEquals("CommitPhase1MethodCallCount", 1, transactionMember2.getCommitPhase1MethodCallCount());
    assertEquals("CommitPhase2MethodCallCount", 0, transactionMember2.getCommitPhase2MethodCallCount());
    assertEquals("ReleaseMethodCallCount", 1, transactionMember2.getReleaseMethodCallCount());
    assertEquals("RollbackMethodCallCount", 1, transactionMember2.getRollbackMethodCallCount());
  }

  @Test
  public void testTransactionWithException() {
    final TestTransactionMember transactionMember1 = new TestTransactionMember("01");
    final TestTransactionMember transactionMember2 = new TestTransactionMember("02");
    ServerJob job = new ServerJob("", ServerJob.getCurrentSession()) {

      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        ITransaction transaction = ThreadContext.getTransaction();
        transaction.registerMember(transactionMember1);
        transaction.registerMember(transactionMember2);
        throw new ProcessingException("Blubber");
      }
    };
    job.runNow(new NullProgressMonitor());
    assertEquals("CommitPhase1MethodCallCount", 0, transactionMember1.getCommitPhase1MethodCallCount());
    assertEquals("CommitPhase2MethodCallCount", 0, transactionMember1.getCommitPhase2MethodCallCount());
    assertEquals("ReleaseMethodCallCount", 1, transactionMember1.getReleaseMethodCallCount());
    assertEquals("RollbackMethodCallCount", 1, transactionMember1.getRollbackMethodCallCount());
    assertEquals("CommitPhase1MethodCallCount", 0, transactionMember2.getCommitPhase1MethodCallCount());
    assertEquals("CommitPhase2MethodCallCount", 0, transactionMember2.getCommitPhase2MethodCallCount());
    assertEquals("ReleaseMethodCallCount", 1, transactionMember2.getReleaseMethodCallCount());
    assertEquals("RollbackMethodCallCount", 1, transactionMember2.getRollbackMethodCallCount());
  }

}
