/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.concurrent.ExecutionException;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Used to translate {@link Throwable}s into {@link ProcessingException}s. Thereby, wrapper exceptions like
 * {@link UndeclaredThrowableException}, {@link InvocationTargetException} or {@link ExecutionException} are unwrapped
 * and their cause translated accordingly. Solely, an {@link Error} is not translated but re-throw instead. That is
 * because an {@link Error} indicates a serious problem due to a abnormal condition.
 */
@ApplicationScoped
public class ProcessingExceptionTranslator implements IThrowableTranslator<ProcessingException> {

  /**
   * Translates the given {@link Throwable} into a {@link ProcessingException}. Solely, an {@link Error} is not
   * translated but re-throw instead. That is because an Error indicates a serious problem due to a abnormal condition.
   */
  @Override
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
   * Translates the given {@link Throwable} into a {@link ProcessingException} and adds the given context messages.
   *
   * @see #translate(Throwable)
   */
  public ProcessingException translateAndAddContextMessages(Throwable t, String... msgs) {
    ProcessingException pe = translate(t);
    if (msgs != null) {
      for (String msg : msgs) {
        if (StringUtility.hasText(msg)) {
          pe.addContextMessage(msg);
        }
      }
    }
    return pe;
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

    return pe.withContextInfo("user", SecurityUtility.getPrincipalNames(Subject.getSubject(AccessController.getContext())));
  }
}
