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
package org.eclipse.scout.commons.job.internal.callable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessController;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.JobExecutionException;

/**
 * Processor and utility to translate computing exceptions into {@link ProcessingException}s. If the exception is a
 * {@link UndeclaredThrowableException} or {@link InvocationTargetException}, its cause is translated instead.
 * <p/>
 * This {@link Callable} is a processing object in the language of the design pattern 'chain-of-responsibility'.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 */
public class ExceptionTranslator<RESULT> implements Callable<RESULT>, Chainable<RESULT> {

  @Internal
  protected final Callable<RESULT> m_next;
  @Internal
  protected final IJobInput m_input;

  /**
   * Creates a processor to translate computing exceptions into {@link ProcessingException}s.
   *
   * @param next
   *          next processor in the chain; must not be <code>null</code>.
   */
  public ExceptionTranslator(final Callable<RESULT> next, final IJobInput input) {
    m_input = input;
    m_next = Assertions.assertNotNull(next);
  }

  @Override
  public RESULT call() throws Exception {
    try {
      return m_next.call();
    }
    catch (final Throwable t) {
      final ProcessingException pe = ExceptionTranslator.translate(t);
      pe.addContextMessage(String.format("identity=%s", getIdentity()));
      pe.addContextMessage(String.format("job=%s", m_input.getIdentifier()));

      throw pe;
    }
  }

  /**
   * Translates the given {@link Throwable} into a {@link ProcessingException}.
   *
   * @param t
   *          {@link Throwable} to be translated.
   * @return {@link ProcessingException}.
   */
  public static ProcessingException translate(final Throwable t) {
    if (t instanceof UndeclaredThrowableException && t.getCause() != null) {
      return translate(t.getCause());
    }
    else if (t instanceof InvocationTargetException && t.getCause() != null) {
      return translate(t.getCause());
    }
    else if (t instanceof ProcessingException) {
      return (ProcessingException) t;
    }
    else if (t.getCause() instanceof ProcessingException) {
      return (ProcessingException) t.getCause(); // e.g. if a ProcessingException was encapsulated within a RuntimeException due to API restriction.
    }
    else {
      return new ProcessingException(StringUtility.nvl(t.getMessage(), t.getClass().getSimpleName()), t);
    }
  }

  /**
   * Translates the given {@link InterruptedException} into a {@link JobExecutionException} with the
   * {@link JobExecutionException#isInterruption()}-flag set.
   */
  public static JobExecutionException translateInterruptedException(final InterruptedException e, final String job) {
    return new JobExecutionException(String.format("Interrupted while waiting for the job to complete. [job=%s]", job), e);
  }

  /**
   * Translates the given {@link TimeoutException} into a {@link JobExecutionException} with the
   * {@link JobExecutionException#isTimeout()}-flag set.
   */
  public static JobExecutionException translateTimeoutException(final TimeoutException e, final long timeout, final TimeUnit unit, final String job) {
    return new JobExecutionException(String.format("Failed to wait for the job to complete because it took longer than %sms [job=%s]", unit.toMillis(timeout), job), e);
  }

  /**
   * Translates the given {@link CancellationException} into a {@link JobExecutionException} with the
   * {@link JobExecutionException#isCancellation()}-flag set.
   */
  public static JobExecutionException translateCancellationException(final CancellationException e, final String job) {
    return new JobExecutionException(String.format("Failed to wait for the job to complete because it was canceled. [job=%s]", job), e);
  }

  /**
   * @return the current subject's principals.
   */
  @Internal
  protected String getIdentity() {
    Subject subject = null;
    try {
      subject = Subject.getSubject(AccessController.getContext());
    }
    catch (final SecurityException e) {
      // NOOP
    }

    if (subject == null || subject.getPrincipals().isEmpty()) {
      return "anonymous";
    }

    final List<String> principalNames = new ArrayList<String>();
    for (final Principal principal : subject.getPrincipals()) {
      principalNames.add(principal.getName());
    }
    return StringUtility.join(", ", principalNames);
  }

  @Override
  public Callable<RESULT> getNext() {
    return m_next;
  }
}
