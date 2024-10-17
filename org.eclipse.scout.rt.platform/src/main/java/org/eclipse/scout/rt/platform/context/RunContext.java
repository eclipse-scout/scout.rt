/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.context;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.security.auth.Subject;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.RunContexts.RunContextFactory;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.security.SubjectProcessor;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionProcessor;
import org.eclipse.scout.rt.platform.transaction.TransactionRequiredException;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.IAdaptable;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.platform.util.concurrent.Callables;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.slf4j.MDC;

/**
 * A {@link RunContext} represents a "snapshot" of the current calling state and is always associated with a
 * {@link RunMonitor}.
 * <p>
 * This class facilitates propagation of state among different threads, or allows temporary state changes for the time
 * of executing some code. Also, a {@link RunContext} demarcates the transaction boundary, meaning that it controls
 * whether to use an existing transaction, or to start a new transaction. If starting a new transaction, the transaction
 * is committed or rolled back upon the completion of the runnable. The default transaction scope is
 * {@link TransactionScope#REQUIRED}, which starts a new transaction only if not running in a transaction yet.
 *
 * @since 5.1
 */
@Bean
public class RunContext implements IAdaptable {

  /**
   * The {@link RunContext} which is currently associated with the current thread.
   */
  public static final ThreadLocal<RunContext> CURRENT = new ThreadLocal<>();

  protected RunMonitor m_runMonitor = BEANS.get(RunMonitor.class);
  protected RunMonitor m_parentRunMonitor;
  protected Subject m_subject;
  protected Locale m_locale;
  protected String m_correlationId;
  protected PropertyMap m_propertyMap = new PropertyMap();

  protected Map<ThreadLocal<?>, ThreadLocalProcessor<?>> m_threadLocalProcessors = new HashMap<>();
  protected Map<String, DiagnosticContextValueProcessor> m_diagnosticProcessors = new HashMap<>();

  protected TransactionScope m_transactionScope = TransactionScope.REQUIRED;
  protected ITransaction m_transaction;
  protected Supplier<ITransaction> m_newTransactionSupplier;
  protected List<ITransactionMember> m_transactionMembers = new ArrayList<>();

  protected List<IRunContextChainInterceptor<?>> m_interceptors = new ArrayList<>();

  @PostConstruct
  protected void initChainInterceptors() {
    List<IRunContextChainInterceptorProducer<RunContext>> producers = BEANS.get(RunContextChainIntercepterRegistry.class).getRunContextInterceptorProducer(this.getClass());
    for (IRunContextChainInterceptorProducer<RunContext> producer : producers) {
      IRunContextChainInterceptor<Object> interceptor = producer.create();
      if (interceptor != null) {
        m_interceptors.add(interceptor);
      }
    }
  }

  /**
   * Runs the given {@link IRunnable} on behalf of this {@link RunContext}. Use this method if you run code that does
   * not return a result.
   *
   * @param runnable
   *          runnable to be run.
   * @throws RuntimeException
   *           if the runnable throws an exception, and is translated by {@link DefaultRuntimeExceptionTranslator}.
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
   * @param exceptionTranslator
   *          to translate exceptions thrown during execution.
   * @throws EXCEPTION
   *           if the runnable throws an exception, and is translated by the given {@link IExceptionTranslator}.
   */
  public <EXCEPTION extends Throwable> void run(final IRunnable runnable, final Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION {
    call(Callables.callable(runnable), exceptionTranslator);
  }

  /**
   * Runs the given {@link Callable} on behalf of this {@link RunContext}. Use this method if you run code that returns
   * a result.
   *
   * @param callable
   *          callable to be run.
   * @return the return value of the callable.
   * @throws RuntimeException
   *           if the callable throws an exception, and is translated by {@link DefaultRuntimeExceptionTranslator}.
   */
  public <RESULT> RESULT call(final Callable<RESULT> callable) {
    return call(callable, DefaultRuntimeExceptionTranslator.class);
  }

  /**
   * Runs the given {@link Callable} on behalf of this {@link RunContext}, and allows translation of exceptions thrown
   * during execution.
   *
   * @param callable
   *          callable to be run.
   * @param exceptionTranslator
   *          to translate exceptions thrown during execution.
   * @return the return value of the callable.
   * @throws EXCEPTION
   *           if the callable throws an exception, and is translated by the given {@link IExceptionTranslator}.
   */
  @SuppressWarnings("squid:S1181")
  public <RESULT, EXCEPTION extends Throwable> RESULT call(final Callable<RESULT> callable, final Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION {
    final ThreadInterrupter threadInterrupter = new ThreadInterrupter(Thread.currentThread(), m_runMonitor);
    try {
      return this.<RESULT> createCallableChain().call(callable);
    }
    catch (final Throwable t) {
      throw BEANS.get(exceptionTranslator).translate(t);
    }
    finally {
      threadInterrupter.destroy();
    }
  }

  /**
   * Creates the {@link CallableChain} to initialize this context, and provides basic functionality for this
   * {@link RunContext}.
   * <p>
   * This method is not intended to be overwritten. To contribute behavior, overwrite
   * {@link #interceptCallableChain(CallableChain)}. Contributions are added before setting the transaction boundary.
   */
  protected <RESULT> CallableChain<RESULT> createCallableChain() {
    final CallableChain<RESULT> contributions = new CallableChain<>();
    interceptCallableChain(contributions);

    @SuppressWarnings("unchecked")
    final TransactionProcessor<RESULT> transactionProcessor = BEANS.get(TransactionProcessor.class)
        .withCallerTransaction(m_transaction)
        .withTransactionScope(m_transactionScope)
        .withNewTransactionSupplier(m_newTransactionSupplier)
        .withTransactionMembers(m_transactionMembers);

    return new CallableChain<RESULT>()
        .add(new RunMonitorCancellableProcessor(m_parentRunMonitor, m_runMonitor))
        .add(new ThreadLocalProcessor<>(CURRENT, this))
        .add(new ThreadLocalProcessor<>(CorrelationId.CURRENT, m_correlationId))
        .add(new ThreadLocalProcessor<>(RunMonitor.CURRENT, Assertions.assertNotNull(m_runMonitor)))
        .add(new SubjectProcessor<>(m_subject))
        .add(new DiagnosticContextValueProcessor(BEANS.get(PrinicpalContextValueProvider.class)))
        .add(new DiagnosticContextValueProcessor(BEANS.get(CorrelationIdContextValueProvider.class)))
        .add(new ThreadLocalProcessor<>(NlsLocale.CURRENT, m_locale))
        .add(new ThreadLocalProcessor<>(PropertyMap.CURRENT, m_propertyMap))
        .addAll(m_threadLocalProcessors.values())
        .addAll(contributions.asList())
        .addAll(m_diagnosticProcessors.values())
        .add(transactionProcessor)
        .addAll(m_interceptors);
  }

  /**
   * Method invoked to contribute to the {@link CallableChain} to initialize this context.
   *
   * @param callableChain
   *          The chain used to construct the context.
   */
  protected <RESULT> void interceptCallableChain(final CallableChain<RESULT> callableChain) {
    // intercept method for sub classes
  }

  /**
   * Returns the {@link RunMonitor} associated, and is not <code>null</code>.
   */
  public RunMonitor getRunMonitor() {
    return m_runMonitor;
  }

  /**
   * Associates this context with a {@link RunMonitor}.
   * <p>
   * This method does not register the monitor as child monitor of {@link RunMonitor#CURRENT}, meaning that this context
   * is not cancelled upon cancellation of the current monitor. If propagated cancellation is required, register the
   * given monitor as following:
   *
   * <pre>
   * <code>
   *     RunMonitor monitor = BEANS.get(RunMonitor.class);
   *     RunContexts.copyCurrent().withRunMonitor(monitor).withParentRunMonitor(RunMonitor.CURRENT.get()).run(new IRunnable() {
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
    m_parentRunMonitor = null; // for compatibility reasons, registering a new run monitor must also clear parent run monitor
    m_runMonitor = Assertions.assertNotNull(runMonitor, "RunMonitor must not be null");
    return this;
  }

  /**
   * @see #withParentRunMonitor(RunMonitor)
   */
  public RunMonitor getParentRunMonitor() {
    return m_parentRunMonitor;
  }

  /**
   * Set a specific {@link RunMonitor} as parent run monitor. The run monitor for this context specified by
   * {@link #withRunMonitor(RunMonitor)} is registered as a {@link ICancellable} for the parent run monitor. The parent
   * run monitor may be <code>null</code>, in this case the run monitor for this context is just not registered as a
   * {@link ICancellable}. It is registered as soon as execution is started and unregistered again after finishing
   * execution.
   */
  public RunContext withParentRunMonitor(RunMonitor parentRunMonitor) {
    m_parentRunMonitor = parentRunMonitor;
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
   * @see #withCorrelationId(String)
   */
  public String getCorrelationId() {
    return m_correlationId;
  }

  /**
   * Associates this context with the given <em>correlation ID</em>.
   */
  public RunContext withCorrelationId(final String correlationId) {
    m_correlationId = correlationId;
    return this;
  }

  /**
   * @see #withTransactionScope(TransactionScope)
   */
  public TransactionScope getTransactionScope() {
    return m_transactionScope;
  }

  /**
   * Sets the transaction scope to demarcate the transaction boundary of this context.
   * <ul>
   * <li>Use {@link TransactionScope#REQUIRES_NEW} to start a new transaction.</li>
   * <li>Use {@link TransactionScope#REQUIRED} to start a new transaction only if not running in a transaction yet.</li>
   * <li>Use {@link TransactionScope#MANDATORY} to enforce running in the current transaction. If there is no current
   * transaction, a {@link TransactionRequiredException} is thrown.</li>
   * </ul>
   */
  public RunContext withTransactionScope(final TransactionScope transactionScope) {
    m_transactionScope = transactionScope;
    return this;
  }

  /**
   * @deprecated Use <code>ITransaction.CURRENT.get()</code> to access the current transaction. This member represents
   *             the transaction that was set during the build time of a run context, either explicitly or via
   *             {@link #copy()}. It may differ from the actual transaction used during the execution of the runnable,
   *             e.g. when the transaction scope {@link TransactionScope#REQUIRES_NEW} is used.
   * @see #withTransaction(ITransaction)
   */
  @Deprecated
  public ITransaction getTransaction() {
    return m_transaction;
  }

  /**
   * Sets the transaction to be used for this context. Has only an effect, if the transaction scope is set to
   * {@link TransactionScope#REQUIRED} or {@link TransactionScope#MANDATORY}. In most cases, this property should not be
   * set. <br>
   * <b>Caution:</b> in case the given transaction is used, it <b>it will not be completed (commit/rollback/release) by
   * this context</b>. If your intention is to use a particular {@link ITransaction}, use
   * {@link #withNewTransactionSupplier(Supplier)} and {@link TransactionScope#REQUIRES_NEW} instead.
   */
  public RunContext withTransaction(final ITransaction transaction) {
    m_transaction = transaction;
    return this;
  }

  /**
   * @see #withNewTransactionSupplier(Supplier)
   */
  public Supplier<ITransaction> getNewTransactionSupplier() {
    return m_newTransactionSupplier;
  }

  /**
   * Sets the supplier for a new {@link ITransaction}, which must not return {@code null}. Has only effect, if the
   * transaction scope is set to {@link TransactionScope#REQUIRES_NEW} or {@link TransactionScope#REQUIRED} and
   * {@link ITransaction#CURRENT} is not set. If {@code null}, {@code BEANS.get(ITransaction.class)} is used.
   */
  public RunContext withNewTransactionSupplier(final Supplier<ITransaction> newTransactionSupplier) {
    m_newTransactionSupplier = newTransactionSupplier;
    return this;
  }

  /**
   * Associates this context with the given {@link ITransactionMember}. A transaction member participates in this
   * context's transaction to take action upon commit or rollback.
   * <p>
   * The registration of a transaction member is only allowed, if this context demarcates a new transaction boundary,
   * which always applies for transaction scope {@link TransactionScope#REQUIRES_NEW}.
   */
  public RunContext withTransactionMember(final ITransactionMember transactionMember) {
    m_transactionMembers.add(transactionMember);
    return this;
  }

  /**
   * Removes all transaction members associated with this context.
   */
  public RunContext withoutTransactionMembers() {
    m_transactionMembers.clear();
    return this;
  }

  /**
   * @see #withThreadLocal(ThreadLocal, Object)
   */
  @SuppressWarnings("unchecked")
  public <VALUE> VALUE getThreadLocal(final ThreadLocal<VALUE> threadLocal) {
    final ThreadLocalProcessor<?> processor = m_threadLocalProcessors.get(threadLocal);
    if (processor == null) {
      return null;
    }
    return (VALUE) processor.getValue();
  }

  /**
   * Associates this context with the specified {@link ThreadLocal}, meaning that any code running on behalf of this
   * context has this {@link ThreadLocal} set.
   */
  public <THREAD_LOCAL> RunContext withThreadLocal(final ThreadLocal<THREAD_LOCAL> threadLocal, final THREAD_LOCAL value) {
    m_threadLocalProcessors.put(threadLocal, new ThreadLocalProcessor<>(threadLocal, value));
    return this;
  }

  /**
   * Associates this context with the given diagnostic data to be used by the underlying logging system.
   *
   * @see MDC
   */
  public RunContext withDiagnostic(final IDiagnosticContextValueProvider provider) {
    m_diagnosticProcessors.put(provider.key(), new DiagnosticContextValueProcessor(provider));
    return this;
  }

  /**
   * Associates this context with the given diagnostic data to be used by the underlying logging system.
   *
   * @see MDC
   */
  public RunContext withDiagnostics(final Collection<? extends IDiagnosticContextValueProvider> diagnosticContextValueProviders) {
    for (final IDiagnosticContextValueProvider provider : diagnosticContextValueProviders) {
      withDiagnostic(provider);
    }
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

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    interceptToStringBuilder(builder);
    return builder.toString();
  }

  /**
   * Allows the contribution of <code>toString</code> tokens.
   */
  protected void interceptToStringBuilder(final ToStringBuilder builder) {
    builder
        .attr("subject", getSubject())
        .attr("locale", getLocale())
        .attr("cid", getCorrelationId())
        .attr("transactionScope", getTransactionScope())
        .ref("transaction", getTransaction())
        .ref("newTransactionSupplier", getNewTransactionSupplier())
        .ref("runMonitor", getRunMonitor())
        .ref("transactionMembers", m_transactionMembers)
        .ref("threadLocalProcessors", m_threadLocalProcessors);
  }

  /**
   * Copies the values of the specified {@link RunContext} to <code>this</code> context.
   */
  protected void copyValues(final RunContext origin) {
    m_runMonitor = origin.m_runMonitor;
    m_parentRunMonitor = origin.m_parentRunMonitor;
    m_subject = origin.m_subject;
    m_locale = origin.m_locale;
    m_correlationId = origin.m_correlationId;
    m_propertyMap = new PropertyMap(origin.m_propertyMap);
    m_transactionScope = origin.m_transactionScope;
    m_transaction = origin.m_transaction;
    m_newTransactionSupplier = origin.m_newTransactionSupplier;
    m_transactionMembers = new ArrayList<>(origin.m_transactionMembers);
    m_threadLocalProcessors = new HashMap<>(origin.m_threadLocalProcessors);
    m_diagnosticProcessors = new HashMap<>(origin.m_diagnosticProcessors);
    m_interceptors = new ArrayList<>(origin.m_interceptors);
  }

  /**
   * Takes a "snapshot" of the calling context, and applies it to <code>this</code> context, or throws
   * {@link AssertionException} if not running in a {@link RunContext}.
   */
  protected void fillCurrentValues() {
    final RunContext currentRunContext = Assertions.assertNotNull(CURRENT.get());

    m_runMonitor = RunMonitor.CURRENT.get();
    m_subject = Subject.getSubject(AccessController.getContext());
    m_locale = NlsLocale.CURRENT.get();
    m_correlationId = CorrelationId.CURRENT.get();
    m_propertyMap = new PropertyMap(PropertyMap.CURRENT.get());
    m_transactionScope = currentRunContext.m_transactionScope;
    m_transaction = ITransaction.CURRENT.get();
    m_newTransactionSupplier = currentRunContext.m_newTransactionSupplier;
    m_transactionMembers = new ArrayList<>(currentRunContext.m_transactionMembers);
    m_diagnosticProcessors = new HashMap<>(currentRunContext.m_diagnosticProcessors);

    // Create a copy of the current 'thread-local' processors, and update their values to their current value.
    m_threadLocalProcessors = new HashMap<>(currentRunContext.m_threadLocalProcessors.size());
    for (final ThreadLocalProcessor<?> threadLocalProcessor : currentRunContext.m_threadLocalProcessors.values()) {
      @SuppressWarnings("unchecked")
      final ThreadLocal<Object> threadLocal = (ThreadLocal<Object>) threadLocalProcessor.getThreadLocal();
      m_threadLocalProcessors.put(threadLocal, new ThreadLocalProcessor<>(threadLocal, threadLocal.get()));
    }

    // copy interceptors
    for (IRunContextChainInterceptor<?> interceptor : m_interceptors) {
      interceptor.fillCurrent();
    }
  }

  protected void fillEmpty() {
    for (IRunContextChainInterceptor<?> interceptor : m_interceptors) {
      interceptor.fillEmtpy();
    }
  }

  /**
   * Creates a copy of <code>this</code> context.
   * <p>
   * <b>Important:</b> Be careful when using this method! It should only be used to create a shallow copy of a run
   * context that you have under control, e.g. a template context. Avoid using it to create a copy of the current run
   * context, as simply copying may not make sense for all values (e.g. transaction scope). In most cases, it is
   * recommended to use {@link RunContexts#copyCurrent()} instead to create a new run context. Unlike this copy()
   * method, it initializes the values according to the current thread state. For example, the transaction scope is
   * reset to the default value of the active {@link RunContextFactory} and the transaction is taken from
   * {@code ITransaction.CURRENT.get()}.
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

  /**
   * Interrupts the associated thread upon a hard cancellation of the given {@link RunMonitor}.
   */
  private static class ThreadInterrupter implements ICancellable {

    private final RunMonitor m_monitor;
    private final AtomicBoolean m_cancelled = new AtomicBoolean();

    private volatile Thread m_thread;

    ThreadInterrupter(final Thread thread, final RunMonitor monitor) {
      m_thread = thread;
      m_monitor = monitor;
      m_monitor.registerCancellable(this);
    }

    @Override
    public boolean isCancelled() {
      return m_cancelled.get();
    }

    @Override
    public boolean cancel(final boolean interruptIfRunning) {
      if (!m_cancelled.compareAndSet(false, true)) {
        return false;
      }

      if (interruptIfRunning) {
        synchronized (this) {
          // Interrupt in synchronized block to ensure the thread still to be associated upon interruption.
          if (m_thread != null) {
            m_thread.interrupt();
          }
        }
      }

      return true;
    }

    /**
     * Invoke to no longer interrupt the associated thread upon a hard cancellation of the monitor.
     */
    public synchronized void destroy() {
      m_monitor.unregisterCancellable(this);

      synchronized (this) {
        m_thread = null;
      }
    }
  }
}
