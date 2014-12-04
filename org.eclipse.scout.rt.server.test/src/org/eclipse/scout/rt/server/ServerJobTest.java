/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.testing.shared.HandlerAdapter;
import org.junit.Test;

/**
 * Tests for {@link ServerJob}
 *
 * @since 5.0-M2
 */
public class ServerJobTest {

  private static final RuntimeException m_rollBackException = new RuntimeException("Exception in transaction.rollBack()!");
  private static final Throwable[] m_transactionFailures = new Throwable[2];
  static {
    m_transactionFailures[0] = new Exception("transactionFailure1");
    m_transactionFailures[1] = new Exception("transactionFailure2");
  }

  @Test
  public void testExceptionLoggingInRunTransaction() throws Exception {
    P_ServerJob job = new P_ServerJob("job", mock(IServerSession.class));
    P_LogHandler interceptor = installLogInterceptor();
    job.run(new NullProgressMonitor());

    assertEquals("Could not find the RollbackException in the Logs!", true, interceptor.hasExceptionLogged(m_rollBackException));
    assertEquals("Could not find transactionFailure1 in the Logs!", true, interceptor.hasExceptionLogged(m_transactionFailures[0]));
    assertEquals("Could not find transactionFailure1 in the Logs!", true, interceptor.hasExceptionLogged(m_transactionFailures[1]));
  }

  private P_LogHandler installLogInterceptor() {
    Logger logger = LogManager.getLogManager().getLogger(ServerJob.class.getName());
    P_LogHandler handler = new P_LogHandler();
    logger.addHandler(handler);
    return handler;
  }

  private class P_ServerJob extends ServerJob {

    public P_ServerJob(String name, IServerSession serverSession) {
      super(name, serverSession);
    }

    @Override
    protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
      return new Status(IStatus.ERROR, "plugin", "message");
    }

    @Override
    protected ITransaction createNewTransaction() {
      ITransaction transaction = mock(ITransaction.class);
      when(transaction.hasFailures()).thenReturn(true);
      when(transaction.getFailures()).thenReturn(m_transactionFailures);
      doThrow(m_rollBackException).when(transaction).rollback(); //Throw unchecked exception here!
      return transaction;
    }

  }

  private class P_LogHandler extends HandlerAdapter {

    List<LogRecord> m_records = new LinkedList<LogRecord>();

    @Override
    public void publish(LogRecord record) {
      m_records.add(record);
    }

    private boolean hasExceptionLogged(Throwable t) {
      for (LogRecord r : m_records) {
        if (t.equals(r.getThrown())) {
          return true;
        }
      }
      return false;
    }
  }

  private boolean executed;

  /**
   * Tests if runNow() executes runTransaction()
   *
   * @throws Exception
   */
  @Test
  public void testRun() throws Exception {
    ServerJob job = new ServerJob("TestJob", mock(IServerSession.class), new Subject()) {

      private void setExecuted() {
        executed = true;
      }

      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        setExecuted();
        return mock(IStatus.class);
      }
    };

    executed = false;
    job.runNow(new NullProgressMonitor());
    assertTrue(executed);
  }

}
