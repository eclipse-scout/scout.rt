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
package org.eclipse.scout.rt.platform.exception;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessController;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;

/**
 * Translator for exceptions into {@link ProcessingException}s.
 */
@ApplicationScoped
public class ExceptionTranslator {

  /**
   * Translates the given {@link Throwable} into a {@link ProcessingException}. Solely, an {@link Error} is not
   * translated but re-throw instead. That is because an Error indicates a serious problem due to a abnormal condition.
   */
  public ProcessingException translate(final Throwable t) {
    if (t instanceof Error) {
      throw (Error) t;
    }
    else if (t instanceof UndeclaredThrowableException && t.getCause() != null) {
      return translate(t.getCause());
    }
    else if (t instanceof InvocationTargetException && t.getCause() != null) {
      return translate(t.getCause());
    }
    else if (t instanceof ExecutionException && t.getCause() != null) {
      return translate(t.getCause());
    }
    else {
      ProcessingException pe;
      if (t instanceof ProcessingException) {
        pe = (ProcessingException) t;
      }
      else if (t.getCause() instanceof ProcessingException) {
        pe = (ProcessingException) t.getCause(); // e.g. if a ProcessingException was encapsulated within a RuntimeException due to API restriction.
      }
      else {
        pe = new ProcessingException(StringUtility.nvl(t.getMessage(), t.getClass().getSimpleName()), t);
      }
      return intercept(pe);
    }
  }

  /**
   * Method invoked to intercept the {@link ProcessingException} to be returned. The default implementation adds the
   * current user's identity and the current executing job to the exception's context message.
   */
  protected ProcessingException intercept(final ProcessingException pe) {
    final IProcessingStatus status = pe.getStatus();
    if (status == null) {
      return pe;
    }

    // Add the current user to the context message.
    final String user = "user=" + getCurrentUserIdentity();
    if (!status.getContextMessages().contains(user)) {
      pe.addContextMessage(user);
    }

    // Add the current job to the context message.
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    if (currentFuture != null && !JobInput.N_A.equals(currentFuture.getJobInput().identifier())) {
      final String job = "job=" + currentFuture.getJobInput().identifier();
      if (!status.getContextMessages().contains(job)) {
        pe.addContextMessage(job);
      }
    }

    return pe;
  }

  /**
   * @return Principal of the current Subject, or <i>anonymous</i> if not running on behalf of a Subject or if no
   *         Principal is associated with that Subject. Multiple Principals are separated by comma.
   */
  @Internal
  protected String getCurrentUserIdentity() {
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
}
