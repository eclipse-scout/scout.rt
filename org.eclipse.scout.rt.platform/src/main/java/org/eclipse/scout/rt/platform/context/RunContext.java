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
package org.eclipse.scout.rt.platform.context;

import java.security.AccessController;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Callables;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.internal.CurrentSubjectLogCallable;
import org.eclipse.scout.rt.platform.context.internal.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.context.internal.SubjectCallable;
import org.eclipse.scout.rt.platform.exception.IThrowableTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingExceptionTranslator;
import org.eclipse.scout.rt.platform.job.PropertyMap;

/**
 * A context typically represents a "snapshot" of the current calling state and is always associated with a
 * {@link RunMonitor}. This class facilitates propagation of that state among different threads, or allows temporary
 * state changes to be done for the time of executing some code.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining. The context has the following
 * characteristics:
 * <ul>
 * <li>{@link RunMonitor#CURRENT}</li>
 * <li>{@link Subject#getSubject(java.security.AccessControlContext)}</li>
 * <li>{@link NlsLocale#CURRENT}</li>
 * <li>{@link PropertyMap#CURRENT}</li>
 * </ul>
 * Implementers:<br/>
 * Internally, the context is obtained by <code>BEANS.get(RunContext.class)</code>, meaning that the context can be
 * intercepted, or replaced. Thereto, the method {@link #interceptCallable(Callable)} can be overwritten to contribute
 * some additional behavior.
 *
 * @since 5.1
 */
@Bean
public class RunContext {

  protected RunMonitor m_runMonitor = BEANS.get(RunMonitor.class);
  protected Subject m_subject;
  protected Locale m_locale;
  protected PropertyMap m_propertyMap = new PropertyMap();

  /**
   * Runs the given {@link IRunnable} on behalf of this {@link RunContext}. Use this method if you run code that does
   * not return a result.
   *
   * @param runnable
   *          runnable to be run.
   * @throws ProcessingException
   *           exception thrown during the runnable's execution.
   */
  public void run(final IRunnable runnable) throws ProcessingException {
    call(Callables.callable(runnable));
  }

  /**
   * Runs the given {@link IRunnable} on behalf of this {@link RunContext}, and allows translation of exceptions thrown
   * during execution.
   *
   * @param runnable
   *          runnable to be run.
   * @param throwableTranslator
   *          to translate exceptions thrown during execution.
   * @throws ERROR
   *           thrown according to the given {@link IThrowableTranslator}.
   */
  public <ERROR extends Throwable> void run(final IRunnable runnable, final IThrowableTranslator<? extends ERROR> throwableTranslator) throws ERROR {
    call(Callables.callable(runnable), throwableTranslator);
  }

  /**
   * Runs the given {@link Callable} on behalf of this {@link RunContext}. Use this method if you run code that returns
   * a result.
   *
   * @param callable
   *          callable to be run.
   * @return the return value of the callable.
   * @throws ProcessingException
   *           exception thrown during the callable's execution.
   */
  public <RESULT> RESULT call(final Callable<RESULT> callable) throws ProcessingException {
    return call(callable, BEANS.get(ProcessingExceptionTranslator.class));
  }

  /**
   * Runs the given {@link Callable} on behalf of this {@link RunContext}, and allows translation of exceptions thrown
   * during execution.
   *
   * @param callable
   *          callable to be run.
   * @param throwableTranslator
   *          to translate exceptions thrown during execution.
   * @return the return value of the callable.
   * @throws ERROR
   *           thrown according to the given {@link IThrowableTranslator}.
   */
  public <RESULT, ERROR extends Throwable> RESULT call(final Callable<RESULT> callable, final IThrowableTranslator<? extends ERROR> throwableTranslator) throws ERROR {
    try {
      return interceptCallable(callable).call();
    }
    catch (final Throwable t) {
      final ERROR error = throwableTranslator.translate(t);
      if (error != null) {
        throw error;
      }
      else {
        return null;
      }
    }
  }

  /**
   * Method invoked to construct the context. Overwrite this method to contribute some behavior to the context.
   * <p/>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility' - it is easiest to read the
   * chain from 'bottom-to-top'.
   * <p/>
   * To contribute on top of the chain (meaning that you are invoked <strong>after</strong> the contributions of super
   * classes and therefore can base on their contributed functionality), you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   Callable c2 = new YourInterceptor2(<strong>next</strong>); // executed 3th<br/>
   *   Callable c1 = new YourInterceptor1(c2); // executed 2nd<br/>
   *   Callable head = <i>super.interceptCallable(c1)</i>; // executed 1st<br/>
   *   return head;
   * </code>
   * </p>
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   Callable c2 = <i>super.interceptCallable(<strong>next</strong>)</i>; // executed 3th<br/>
   *   Callable c1 = new YourInterceptor2(c2); // executed 2nd<br/>
   *   Callable head = new YourInterceptor1(c1); // executed 1st<br/>
   *   return head;
   * </code>
   *
   * @param next
   *          the callable to be run in the context.
   * @return the head of the chain to be invoked first.
   */
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final Callable<RESULT> c5 = new InitThreadLocalCallable<>(next, PropertyMap.CURRENT, propertyMap());

    final Callable<RESULT> c4 = new InitThreadLocalCallable<>(c5, NlsLocale.CURRENT, locale());
    final Callable<RESULT> c3 = new CurrentSubjectLogCallable<>(c4);
    final Callable<RESULT> c2 = new SubjectCallable<>(c3, subject());
    final Callable<RESULT> c1 = new InitThreadLocalCallable<>(c2, RunMonitor.CURRENT, Assertions.assertNotNull(runMonitor()));

    return c1;
  }

  /**
   * @return {@link RunMonitor} to be used, is never <code>null</code>.
   */
  public RunMonitor runMonitor() {
    return m_runMonitor;
  }

  /**
   * Set a specific {@link RunMonitor} to be used, which must not be <code>null</code>. However, even if there is a
   * current {@link RunMonitor}, it is NOT registered as child monitor, meaning that it will not be cancelled once the
   * current {@link RunMonitor} is cancelled. If such a linking is needed, you have to do that yourself:
   *
   * <pre>
   * <code>
   *     RunMonitor monitor = BEANS.get(RunMonitor.class);
   * 
   *     // Register your monitor to be cancelled as well
   *     RunMonitor.CURRENT.get().registerCancellable(monitor);
   * 
   *     RunContexts.copyCurrent().runMonitor(monitor).run(new IRunnable() {
   * 
   *       &#064;Override
   *       public void run() throws Exception {
   *         // do something
   *       }
   *     });
   * </code>
   * </pre>
   */
  public RunContext runMonitor(final RunMonitor runMonitor) {
    m_runMonitor = Assertions.assertNotNull(runMonitor, "RunMonitor must not be null");
    return this;
  }

  public Subject subject() {
    return m_subject;
  }

  public RunContext subject(final Subject subject) {
    m_subject = subject;
    return this;
  }

  public Locale locale() {
    return m_locale;
  }

  public RunContext locale(final Locale locale) {
    m_locale = locale;
    return this;
  }

  public PropertyMap propertyMap() {
    return m_propertyMap;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.ref("runMonitor", runMonitor());
    builder.ref("subject", subject());
    builder.attr("locale", locale());

    return builder.toString();
  }

  /**
   * Method invoked to fill this {@link RunContext} with values from the given {@link RunContext}.
   */
  protected void copyValues(final RunContext origin) {
    m_runMonitor = origin.m_runMonitor;
    m_subject = origin.m_subject;
    m_locale = origin.m_locale;
    m_propertyMap = new PropertyMap(origin.m_propertyMap);
  }

  /**
   * Method invoked to fill this {@link RunContext} with values from the current calling {@link RunContext}.
   *
   * @RunMonitor a new {@link RunMonitor} is created, and if the current calling context contains a {@link RunMonitor},
   *             it is also registered within that {@link RunMonitor}. That makes the <i>returned</i> {@link RunContext}
   *             to be cancelled as well once the current calling {@link RunContext} is cancelled,
   *             but DOES NOT cancel the current calling {@link RunContext} if the <i>returned</i> {@link RunContext} is
   *             cancelled.
   */
  protected void fillCurrentValues() {
    m_subject = Subject.getSubject(AccessController.getContext());
    m_locale = NlsLocale.CURRENT.get();
    m_propertyMap = new PropertyMap(PropertyMap.CURRENT.get());
    m_runMonitor = BEANS.get(RunMonitor.class);
    if (RunMonitor.CURRENT.get() != null) {
      RunMonitor.CURRENT.get().registerCancellable(m_runMonitor);
    }
  }

  /**
   * Method invoked to fill this {@link RunContext} with empty values.
   *
   * @RunMonitor a new {@link RunMonitor} is created. However, even if there is a current {@link RunMonitor}, it is
   *             NOT registered as child monitor, meaning that it will not be cancelled once the current
   *             {@link RunMonitor} is cancelled.
   */
  protected void fillEmptyValues() {
    m_subject = null;
    m_locale = null;
    m_runMonitor = BEANS.get(RunMonitor.class);
    m_propertyMap = new PropertyMap();
  }

  /**
   * Creates a copy of <code>this RunContext</code>.
   */
  public RunContext copy() {
    final RunContext copy = BEANS.get(RunContext.class);
    copy.copyValues(this);
    return copy;
  }
}
