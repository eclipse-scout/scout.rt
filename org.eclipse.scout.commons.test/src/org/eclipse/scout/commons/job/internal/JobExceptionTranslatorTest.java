/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.job.internal;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.junit.Test;

public class JobExceptionTranslatorTest {

  @Test
  public void test() {

    Exception e = new Exception();
    ProcessingException pe = JobExceptionTranslator.translate(e, "job-1");
    assertSame(e, pe.getCause());

    e = new RuntimeException();
    pe = JobExceptionTranslator.translate(e, "job-1");
    assertSame(e, pe.getCause());

    e = new ProcessingException();
    pe = JobExceptionTranslator.translate(e, "job-1");
    assertSame(e, pe);

    e = new ProcessingException();
    pe = JobExceptionTranslator.translate(new ExecutionException(e), "job-1");
    assertSame(e, pe);

    e = new RuntimeException();
    pe = JobExceptionTranslator.translate(new ExecutionException(e), "job-1");
    assertTrue(pe.getClass().equals(ProcessingException.class));
    assertSame(e, pe.getCause());

    e = new InterruptedException();
    pe = JobExceptionTranslator.translate(e, "job-1");
    assertTrue(pe instanceof JobExecutionException);
    assertTrue(pe.isInterruption());

    e = new CancellationException();
    pe = JobExceptionTranslator.translate(e, "job-1");
    assertTrue(pe instanceof JobExecutionException);
    assertTrue(((JobExecutionException) pe).isCancellation());

    TimeoutException te = new TimeoutException();
    pe = JobExceptionTranslator.translate(te, 10, TimeUnit.SECONDS, "job-1");
    assertTrue(pe instanceof JobExecutionException);
    assertTrue(((JobExecutionException) pe).isTimeout());
  }

}
