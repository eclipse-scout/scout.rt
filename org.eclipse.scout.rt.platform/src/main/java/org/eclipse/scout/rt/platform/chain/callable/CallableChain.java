/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.chain.callable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.chain.IChainable;
import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator.IUndecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chain to decorate {@link Callable#call()} according to the design pattern 'Chain-of-responsibility'.
 * <p>
 * This chain allows to perform a series of actions prior calling a {@link Callable} command. This implementation allows
 * both, the interception by decorators and interceptors. A decorator can run some actions before or after the execution
 * of the command, but cannot wrap execution. That is where interceptors come into play, because they continue the chain
 * themselves by invoking the next element in the chain.
 * <p>
 * In contrast to a decorator, an interceptor wraps execution of subsequent handlers and the {@link Callable}.
 * Inherently, each interceptor in the chain blows up the stack, which is why to prefer decorators over interceptors
 * whenever possible.
 *
 * @since 5.2
 */
public class CallableChain<RESULT> {

  private static final Logger LOG = LoggerFactory.getLogger(CallableChain.class);

  private final LinkedList<IChainable> m_chainables = new LinkedList<>();

  /**
   * Adds the given decorator to the beginning of this chain to decorate the execution of a {@link Callable}.
   *
   * @return <code>this</code> in order to support method chaining.
   */
  public CallableChain<RESULT> addFirst(final ICallableDecorator decorator) {
    m_chainables.addFirst(decorator);
    return this;
  }

  /**
   * Adds the given interceptor to the beginning of this chain to wrap the execution of a {@link Callable}.
   *
   * @return <code>this</code> in order to support method chaining.
   */
  public CallableChain<RESULT> addFirst(final ICallableInterceptor<RESULT> interceptor) {
    m_chainables.addFirst(interceptor);
    return this;
  }

  /**
   * Adds the given decorator to the end of this chain to decorate the execution of a {@link Callable}.
   *
   * @return <code>this</code> in order to support method chaining.
   */
  public CallableChain<RESULT> addLast(final ICallableDecorator decorator) {
    m_chainables.addLast(decorator);
    return this;
  }

  /**
   * Adds the given interceptor to the end of this chain to wrap the execution of a {@link Callable}.
   *
   * @return <code>this</code> in order to support method chaining.
   */
  public CallableChain<RESULT> addLast(final ICallableInterceptor<RESULT> interceptor) {
    m_chainables.add(interceptor);
    return this;
  }

  /**
   * Adds the given decorator to the end of this chain to decorate the execution of a {@link Callable}.
   * <p>
   * This method is equivalent to {@link CallableChain#addLast(ICallableDecorator)}.
   *
   * @return <code>this</code> in order to support method chaining.
   */
  public CallableChain<RESULT> add(final ICallableDecorator decorator) {
    m_chainables.add(decorator);
    return this;
  }

  /**
   * Adds the given decorator at the index position to the chain to wrap the execution of a {@link Callable}.
   *
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (<tt>index &lt; 0 || index &gt; size()</tt>)
   * @return <code>this</code> in order to support method chaining.
   */
  public CallableChain<RESULT> add(int index, ICallableDecorator decorator) {
    m_chainables.add(index, decorator);
    return this;
  }

  /**
   * Adds the given elements to the end of this chain to decorate the execution of a {@link Callable}.
   *
   * @return <code>this</code> in order to support method chaining.
   */
  public CallableChain<RESULT> addAll(final Collection<? extends IChainable> decorators) {
    m_chainables.addAll(decorators);
    return this;
  }

  /**
   * Adds the given interceptor to the end of this chain to wrap the execution of a {@link Callable}.
   * <p>
   * This method is equivalent to {@link CallableChain#addLast(ICallableInterceptor)}.
   *
   * @return <code>this</code> in order to support method chaining.
   */
  public CallableChain<RESULT> add(final ICallableInterceptor<RESULT> interceptor) {
    m_chainables.add(interceptor);
    return this;
  }

  /**
   * Adds the given interceptor at the index position to the chain to wrap the execution of a {@link Callable}.
   *
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (<tt>index &lt; 0 || index &gt; size()</tt>)
   * @return <code>this</code> in order to support method chaining.
   */
  public CallableChain<RESULT> add(int index, ICallableInterceptor<RESULT> interceptor) {
    m_chainables.add(index, interceptor);
    return this;
  }

  /**
   * Removes all of the decorators and interceptors from this chain. The chain will be empty after this call returns.
   *
   * @return <code>this</code> in order to support method chaining.
   */
  public CallableChain<RESULT> clear() {
    m_chainables.clear();
    return this;
  }

  /**
   * Returns the processors contained in this chain.
   */
  public List<IChainable> values() {
    return m_chainables;
  }

  /**
   * Invokes {@link Callable#call()} and lets all added processors to participate in the invocation.
   *
   * @param command
   *          the command to be executed.
   * @return the result of the command.
   * @throws Exception
   *           thrown during execution of the command.
   */
  public RESULT call(final Callable<RESULT> command) throws Exception {
    return new Chain<>(m_chainables, command).continueChain();
  }

  /**
   * Returns all elements in the order as inserted.
   */
  public List<IChainable> asList() {
    return new ArrayList<>(m_chainables);
  }

  /**
   * A Chain is an object provided by {@link CallableChain} used to invoke the next handler in the chain, or if the
   * calling handler is the last handler in the chain, to finally invoke the {@link Callable}.
   *
   * @param <RESULT>
   *          the result type of the {@link Callable} to be invoked.
   */
  public static class Chain<RESULT> {

    private final Iterator<IChainable> m_iterator;
    private final Callable<RESULT> m_command;

    public Chain(final List<IChainable> chainables, final Callable<RESULT> command) {
      m_iterator = chainables.iterator();
      m_command = command;
    }

    /**
     * Causes the next handler in the chain to be invoked, or if the calling handler is the last handler in the chain,
     * causes the {@link Callable} to be invoked.
     *
     * @return the {@link Callable}'s return value to pass along to the invoker.
     * @throws Exception
     *           the {@link Callable}'s exception to pass along to the invoker.
     */
    public RESULT continueChain() throws Exception {
      // List of decorators invoked in this round.
      final List<IUndecorator> undecorators = new ArrayList<>();

      try {
        while (m_iterator.hasNext()) {
          final IChainable next = m_iterator.next();

          if (next instanceof ICallableDecorator) {
            final IUndecorator undecorator = ((ICallableDecorator) next).decorate();
            if (undecorator != null) {
              undecorators.add(undecorator);
            }
          }
          else if (next instanceof ICallableInterceptor && ((ICallableInterceptor) next).isEnabled()) {
            @SuppressWarnings("unchecked")
            final ICallableInterceptor<RESULT> interceptor = ((ICallableInterceptor<RESULT>) next);
            return interceptor.intercept(this);
          }
        }

        // Invoke the command because all handlers participated in the processing.
        return m_command.call();
      }
      finally {
        // Let the decorators to perform some 'after-execution' actions in reverse order.
        for (int i = undecorators.size() - 1; i >= 0; i--) {
          undecorateSafe(undecorators.get(i));
        }
      }
    }

    private void undecorateSafe(final IUndecorator undecorator) {
      try {
        undecorator.undecorate();
      }
      catch (final RuntimeException e) {
        LOG.error("Unexpected error during the undecoration of a command's execution. [undecorator={}, command={}]", undecorator.getClass().getName(), m_command, e);
      }
    }
  }
}
