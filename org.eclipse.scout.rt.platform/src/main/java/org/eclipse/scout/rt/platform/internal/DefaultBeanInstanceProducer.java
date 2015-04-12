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
package org.eclipse.scout.rt.platform.internal;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.platform.BeanCreationException;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;

public class DefaultBeanInstanceProducer<T> implements IBeanInstanceProducer<T> {
  private static final ThreadLocal<Deque<String>> INSTANTIATION_STACK = new ThreadLocal<>();

  private final Semaphore m_instanceLock;
  private T m_applicationScopedInstance;

  public DefaultBeanInstanceProducer() {
    m_instanceLock = new Semaphore(1, true);
  }

  @Override
  public T produce(IBean<T> bean) {
    if (m_applicationScopedInstance != null) {
      return m_applicationScopedInstance;
    }

    Deque<String> stack = INSTANTIATION_STACK.get();
    String beanName = bean.getBeanClazz().getName();
    if (stack != null && stack.contains(beanName)) {
      String message = String.format("The requested bean is currently being created. Creation path: [%s]", CollectionUtility.format(stack, ", "));
      throw new BeanCreationException(beanName, message);
    }

    if (BeanManagerImplementor.isApplicationScoped(bean)) {
      m_instanceLock.acquireUninterruptibly();
      try {
        m_applicationScopedInstance = createNewInstance(bean.getBeanClazz());
        return m_applicationScopedInstance;
      }
      finally {
        m_instanceLock.release();
      }
    }
    return createNewInstance(bean.getBeanClazz());
  }

  /**
   * @returns a new instance of the bean
   */
  protected T createNewInstance(Class<? extends T> beanClass) {
    Deque<String> stack = INSTANTIATION_STACK.get();
    boolean removeStack = false;
    if (stack == null) {
      stack = new LinkedList<>();
      INSTANTIATION_STACK.set(stack);
      removeStack = true;
    }

    try {
      stack.addLast(beanClass.getName());
      return BeanInstanceUtil.create(beanClass);
    }
    finally {
      if (removeStack) {
        INSTANTIATION_STACK.remove();
      }
      else {
        stack.removeLast();
      }
    }
  }

}
