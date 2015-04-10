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

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Callables;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.ExceptionTranslator;
import org.eclipse.scout.rt.platform.job.PropertyMap;

/**
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * state among different threads, or allows temporary state changes to be done for the time of executing some code.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining. The context has the following
 * characteristics:
 * <ul>
 * <li>{@link Subject}</li>
 * <li>{@link NlsLocale#CURRENT}</li>
 * <li>{@link PropertyMap#CURRENT}</li>
 * </ul>
 * Implementers:<br/>
 * Internally, the context is obtained by <code>OBJ.one(Context.class)</code>, meaning that the context can be
 * intercepted, or replaced. Thereto, the method {@link #interceptCallable(ICallable)} can be overwritten to contribute
 * some additional behavior.
 *
 * @since 5.1
 */
@Bean
public class RunContext {

  protected PreferredValue<Subject> m_subject = new PreferredValue<>(null, false);
  protected PreferredValue<Locale> m_locale = new PreferredValue<>(null, false);
  protected PropertyMap m_propertyMap;

  /**
   * Runs the given runnable on behalf of this {@link RunContext}. Use this method if you run code that does not return
   * a result.
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
   * Runs the given callable on behalf of this {@link RunContext}. Use this method if you run code that returns a
   * result.
   *
   * @param callable
   *          callable to be run.
   * @return the return value of the callable.
   * @throws ProcessingException
   *           exception thrown during the callable's execution.
   */
  public <RESULT> RESULT call(final ICallable<RESULT> callable) throws ProcessingException {
    try {
      return interceptCallable(callable).call();
    }
    catch (final Exception e) {
      throw BEANS.get(ExceptionTranslator.class).translate(e);
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
   *   ICallable c2 = new YourInterceptor2(<strong>next</strong>); // executed 3th<br/>
   *   ICallable c1 = new YourInterceptor1(c2); // executed 2nd<br/>
   *   ICallable head = <i>super.interceptCallable(c1)</i>; // executed 1st<br/>
   *   return head;
   * </code>
   * </p>
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   ICallable c2 = <i>super.interceptCallable(<strong>next</strong>)</i>; // executed 3th<br/>
   *   ICallable c1 = new YourInterceptor2(c2); // executed 2nd<br/>
   *   ICallable head = new YourInterceptor1(c1); // executed 1st<br/>
   *   return head;
   * </code>
   *
   * @param next
   *          the callable to be run in the context.
   * @return the head of the chain to be invoked first.
   */
  protected <RESULT> ICallable<RESULT> interceptCallable(final ICallable<RESULT> next) {
    final ICallable<RESULT> c3 = new InitThreadLocalCallable<>(next, PropertyMap.CURRENT, propertyMap());
    final ICallable<RESULT> c2 = new InitThreadLocalCallable<>(c3, NlsLocale.CURRENT, locale());
    final ICallable<RESULT> c1 = new SubjectCallable<>(c2, subject());

    return c1;
  }

  public Subject subject() {
    return m_subject.get();
  }

  /**
   * Set the Subject to run code under a particular user.
   */
  public RunContext subject(final Subject subject) {
    m_subject.set(subject, true);
    return this;
  }

  public Locale locale() {
    return m_locale.get();
  }

  /**
   * Set a specific Locale.
   */
  public RunContext locale(final Locale locale) {
    m_locale.set(locale, true);
    return this;
  }

  public PropertyMap propertyMap() {
    return m_propertyMap;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.ref("subject", subject());
    builder.attr("locale", locale());
    return builder.toString();
  }

  /**
   * Method invoked to fill this {@link RunContext} with values from the given {@link RunContext}.
   */
  protected void copyValues(final RunContext origin) {
    m_subject = origin.m_subject.copy();
    m_locale = origin.m_locale.copy();
    m_propertyMap = new PropertyMap(origin.m_propertyMap);
  }

  /**
   * Method invoked to fill this {@link RunContext} with values from the current calling {@link RunContext}.
   */
  protected void fillCurrentValues() {
    m_subject = new PreferredValue<>(Subject.getSubject(AccessController.getContext()), false);
    m_locale = new PreferredValue<>(NlsLocale.CURRENT.get(), false);
    m_propertyMap = new PropertyMap(PropertyMap.CURRENT.get());
  }

  /**
   * Method invoked to fill this {@link RunContext} with empty values.
   */
  protected void fillEmptyValues() {
    m_subject = new PreferredValue<>(null, true); // null as preferred Subject
    m_locale = new PreferredValue<>(null, true); // null as preferred Locale
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
