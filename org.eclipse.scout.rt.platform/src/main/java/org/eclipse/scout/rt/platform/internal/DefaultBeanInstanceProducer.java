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
import java.util.concurrent.Callable;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.FinalValue;
import org.eclipse.scout.rt.platform.BeanCreationException;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;

public class DefaultBeanInstanceProducer<T> implements IBeanInstanceProducer<T> {
  private static final ThreadLocal<Deque<String>> INSTANTIATION_STACK = new ThreadLocal<>();

  private final FinalValue<T> m_applicationScopedInstance = new FinalValue<>();

  @Override
  public T produce(IBean<T> bean) {
    Deque<String> stack = INSTANTIATION_STACK.get();
    String beanName = bean.getBeanClazz().getName();
    if (stack != null && stack.contains(beanName)) {
      String message = String.format("The requested bean is currently being created. Creation path: [%s]", CollectionUtility.format(stack, ", "));
      throw new BeanCreationException(beanName, message);
    }

    if (BeanManagerImplementor.isApplicationScoped(bean)) {
      return getApplicationScopedInstance(bean);
    }

    return createNewInstance(bean.getBeanClazz());
  }

  private T getApplicationScopedInstance(final IBean<T> bean) {
    return m_applicationScopedInstance.setIfAbsent(new Callable<T>() {
      @Override
      public T call() {
        return createNewInstance(bean.getBeanClazz());
      }
    });
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
