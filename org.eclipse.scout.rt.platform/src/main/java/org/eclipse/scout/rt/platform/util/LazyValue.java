/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.BEANS;

/**
 * Holder for a bean that is lazily produced on the first call to {@link #get()}. Useful as a constant in another bean
 * class, because it prevents problems during platform initialization (class might be loaded before bean manager is
 * ready) or with cyclic bean dependencies.
 * <p>
 * Example usage:
 *
 * <pre>
* &#64;Bean
* public class MyBean {
*
*   private static final LazyValue<MyHelper> STATIC_HELPER = new LazyValue<>(MyHelper.class);
*
*   public void myMethod() {
*     ...
*     STATIC_HELPER.get().doSomething();
*     ...
*   }
* }
 * </pre>
 *
 * @since 6.1
 */
public class LazyValue<T> {

  private final FinalValue<T> m_value = new FinalValue<>();
  private final Callable<T> m_producer;

  public LazyValue(final Callable<T> producer) {
    m_producer = producer;
  }

  /**
   * Convenience constructor using a producer that creates a bean for the given type using
   * <code>BEANS.get(beanType)</code>.
   */
  public LazyValue(final Class<T> beanType) {
    this(() -> BEANS.get(beanType));
  }

  public T get() {
    return m_value.setIfAbsentAndGet(m_producer);
  }

  public boolean isSet() {
    return m_value.isSet();
  }

  public void ifSet(Consumer<? super T> action) {
    m_value.ifSet(action);
  }
}
