/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.testing.platform.mock;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.platform.internal.SingeltonBeanInstanceProducer;
import org.mockito.Mockito;

/**
 * Uses {@link Mockito#mock(Class)} to create a new instance for a bean.
 */
public class MockBeanInstanceProducer<T> extends SingeltonBeanInstanceProducer<T> {

  @Override
  public T produce(IBean<T> bean) {
    if (BeanManagerImplementor.isApplicationScoped(bean)) {
      return super.produce(bean);
    }
    else {
      return BeanInstanceUtil.createAndAssertNoCircularDependency(() -> createInstance(bean), bean.getBeanClazz());
    }
  }

  @Override
  protected T createInstance(IBean<T> bean) {
    T mock = Mockito.mock(bean.getBeanClazz());
    BeanInstanceUtil.initializeBeanInstance(mock);
    return mock;
  }
}
