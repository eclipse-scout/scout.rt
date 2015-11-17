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
import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Callables;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.IAdaptable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.ThreadLocalProcessor;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.chain.InvocationChain;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.commons.security.SubjectProcessor;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.IThrowableTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingExceptionTranslator;
import org.eclipse.scout.rt.platform.logging.PrinicpalContextValueProvider;

/**
 * A context typically represents a "snapshot" of the current calling state and is always associated with a
 * {@link RunMonitor}. This class facilitates propagation of that state among different threads, or allows temporary
 * state changes to be done for the time of executing some code.
 * <p>
 * Internally, the context is obtained by <code>BEANS.get(RunContext.class)</code>, meaning that the context can be
 * intercepted, or replaced. Thereto, the method {@link #interceptInvocationChain(InvocationChain)} can be overwritten
 * to contribute some additional behavior.
 *
 * @since 5.1
 */
@Bean
public class RunContext implements IAdaptable {

  protected RunMonitor m_runMonitor = BEANS.get(RunMonitor.class);
  protected Subject m_subject;
  protected Locale m_locale;
  protected PropertyMap m_propertyMap = new PropertyMap();
  protected Deque<String> m_identifiers = new LinkedList<>();

  /**
   * Runs the given {@link IRunnable} on behalf of this {@link RunContext}. Use this method if you run code that does
   * not return a result.
   *
   * @param runnable
   *          runnable to be run.
   * @throws ProcessingException
   *           exception thrown during the runnable's execution.
   */
  public void run(final IRunnable runnable) {
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
  public <RESULT> RESULT call(final Callable<RESULT> callable) {
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
      final InvocationChain<RESULT> invocationChain = new InvocationChain<>();
      interceptInvocationChain(invocationChain);
      return invocationChain.invoke(callable);
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
   * Method invoked to intercept the invocation chain used to initialize this context. Overwrite this method to
   * contribute some behavior to the context.
   * <p>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility'.<br/>
   * To contribute to the end of the chain (meaning that you are invoked <strong>after</strong> the contributions of
   * super classes and therefore can base on their contributed functionality), you can use constructions of the
   * following form:
   *
   * <pre>
   * super.initInvocationChain(invocationChain);
   * invocationChain.addLast(new YourDecorator());
   * </pre>
   *
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   *
   * <pre>
   * super.initInvocationChain(invocationChain);
   * invocationChain.addFirst(new YourDecorator());
   * </pre>
   *
   * @param invocationChain
   *          The chain used to construct the context.
   */
  protected <RESULT> void interceptInvocationChain(final InvocationChain<RESULT> invocationChain) {
    invocationChain
        .add(new ThreadLocalProcessor<>(RunMonitor.CURRENT, Assertions.assertNotNull(m_runMonitor)))
        .add(new SubjectProcessor<RESULT>(m_subject))
        .add(new DiagnosticContextValueProcessor<>(BEANS.get(PrinicpalContextValueProvider.class)))
        .add(new ThreadLocalProcessor<>(NlsLocale.CURRENT, m_locale))
        .add(new ThreadLocalProcessor<>(PropertyMap.CURRENT, m_propertyMap))
        .add(new ThreadLocalProcessor<>(RunContextIdentifiers.CURRENT, m_identifiers));
  }

  /**
   * @return {@link RunMonitor} to be used, is never <code>null</code>.
   */
  public RunMonitor getRunMonitor() {
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
   *     RunContexts.copyCurrent().withRunMonitor(monitor).run(new IRunnable() {
   *
   *       &#064;Override
   *       public void run() throws Exception {
   *         // do something
   *       }
   *     });
   * </code>
   * </pre>
   */
  public RunContext withRunMonitor(final RunMonitor runMonitor) {
    m_runMonitor = Assertions.assertNotNull(runMonitor, "RunMonitor must not be null");
    return this;
  }

  /**
   * @see #withSubject(Subject)
   */
  public Subject getSubject() {
    return m_subject;
  }

  /**
   * Associates this context with the given {@link Subject}, meaning that any code running on behalf of this context is
   * run as the given {@link Subject}.
   */
  public RunContext withSubject(final Subject subject) {
    m_subject = subject;
    return this;
  }

  /**
   * @see #withLocale(Locale)
   */
  public Locale getLocale() {
    return m_locale;
  }

  /**
   * Associates this context with the given {@link Locale}, meaning that any code running on behalf of this context has
   * that {@link Locale} set in {@link NlsLocale#CURRENT} thread-local.
   */
  public RunContext withLocale(final Locale locale) {
    m_locale = locale;
    return this;
  }

  /**
   * Returns the {@link PropertyMap} associated with this context.
   *
   * @see #withProperty(Object, Object)
   */
  public PropertyMap getPropertyMap() {
    return m_propertyMap;
  }

  /**
   * Returns the property value to which the specified key is mapped, or <code>null</code> if not associated with this
   * context.
   *
   * @see #withProperty(Object, Object)
   */
  public <VALUE> VALUE getProperty(final Object key) {
    return m_propertyMap.get(key);
  }

  /**
   * Returns the property value to which the specified key is mapped, or {@code defaultValue} if not associated with
   * this context.
   *
   * @see #withProperty(Object, Object)
   */
  public <VALUE> VALUE getPropertyOrDefault(final Object key, final VALUE defaultValue) {
    return m_propertyMap.getOrDefault(key, defaultValue);
  }

  /**
   * Returns whether the given property is associated with this context.
   *
   * @see #withProperty(Object, Object)
   */
  public boolean containsProperty(final Object key) {
    return m_propertyMap.contains(key);
  }

  /**
   * Associates this context with the given 'key-value' property, meaning that any code running on behalf of this
   * context has that property set in {@link PropertyMap#CURRENT} thread-local.
   * <p>
   * To remove a property, use <code>null</code> as its value.
   */
  public RunContext withProperty(final Object key, final Object value) {
    m_propertyMap.put(key, value);
    return this;
  }

  /**
   * Associates this context with the given 'key-value' properties, meaning that any code running on behalf of this
   * context has those properties set in {@link PropertyMap#CURRENT} thread-local.
   */
  public RunContext withProperties(final Map<?, ?> properties) {
    for (final Entry<?, ?> propertyEntry : properties.entrySet()) {
      withProperty(propertyEntry.getKey(), propertyEntry.getValue());
    }
    return this;
  }

  /**
   * Gets a live reference to the identifiers of this run context.
   *
   * @return A {@link Deque} with all identifiers of this context having the current identifier on top of the deque.
   * @see RunContextIdentifiers#isCurrent(String)
   */
  public Deque<String> getIdentifiers() {
    return m_identifiers;
  }

  /**
   * Pushes a new identifier on top of the identifiers {@link Deque}.
   *
   * @param id
   *          The new top identifier.
   * @return this
   * @see RunContextIdentifiers#isCurrent(String)
   */
  public RunContext withIdentifier(String id) {
    m_identifiers.push(id);
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.ref("runMonitor", getRunMonitor());
    builder.ref("subject", getSubject());
    builder.attr("locale", getLocale());
    builder.attr("ids", CollectionUtility.format(getIdentifiers()));
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
    m_identifiers = new LinkedList<>(origin.m_identifiers);
  }

  /**
   * Method invoked to fill this {@link RunContext} with values from the current calling {@link RunContext}.
   *
   * @RunMonitor a new {@link RunMonitor} is created, and if the current calling context contains a {@link RunMonitor},
   *             it is also registered within that {@link RunMonitor}. That makes the <i>returned</i> {@link RunContext}
   *             to be cancelled as well once the current calling {@link RunContext} is cancelled, but DOES NOT cancel
   *             the current calling {@link RunContext} if the <i>returned</i> {@link RunContext} is cancelled.
   */
  protected void fillCurrentValues() {
    m_subject = Subject.getSubject(AccessController.getContext());
    m_locale = NlsLocale.CURRENT.get();
    m_propertyMap = new PropertyMap(PropertyMap.CURRENT.get());
    m_runMonitor = BEANS.get(RunMonitor.class);

    m_identifiers = new LinkedList<>();
    Deque<String> existingIds = RunContextIdentifiers.CURRENT.get();
    if (existingIds != null) {
      m_identifiers.addAll(existingIds);
    }

    if (RunMonitor.CURRENT.get() != null) {
      RunMonitor.CURRENT.get().registerCancellable(m_runMonitor);
    }
  }

  /**
   * Method invoked to fill this {@link RunContext} with empty values.
   *
   * @RunMonitor a new {@link RunMonitor} is created. However, even if there is a current {@link RunMonitor}, it is NOT
   *             registered as child monitor, meaning that it will not be cancelled once the current {@link RunMonitor}
   *             is cancelled.
   */
  protected void fillEmptyValues() {
    m_subject = null;
    m_locale = null;
    m_runMonitor = BEANS.get(RunMonitor.class);
    m_propertyMap = new PropertyMap();
    m_identifiers = new LinkedList<>();
  }

  /**
   * Creates a copy of <code>this RunContext</code>.
   */
  public RunContext copy() {
    final RunContext copy = BEANS.get(RunContext.class);
    copy.copyValues(this);
    return copy;
  }

  @Override
  public <T> T getAdapter(final Class<T> type) {
    return null;
  }
}
