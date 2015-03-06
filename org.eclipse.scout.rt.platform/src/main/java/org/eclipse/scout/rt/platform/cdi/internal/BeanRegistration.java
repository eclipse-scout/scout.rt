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
package org.eclipse.scout.rt.platform.cdi.internal;

import java.util.concurrent.Semaphore;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.IBeanRegistration;

public class BeanRegistration<T> implements IBeanRegistration<T> {
  private final IBean<T> m_bean;
  private final boolean m_isInstanceBasedBean;
  private final Semaphore m_instanceLock = new Semaphore(1, true);
  private T m_instance;

  public BeanRegistration(IBean<T> bean, T instance) {
    m_bean = Assertions.assertNotNull(bean);
    m_instance = instance;
    m_isInstanceBasedBean = instance != null;
    if (m_isInstanceBasedBean && bean.getBeanAnnotation(ApplicationScoped.class) == null) {
      throw new IllegalArgumentException(String.format("Instance constructor only allows application scoped instances. Class '%s' does not have the '%s' annotation.", bean.getBeanClazz().getName(), ApplicationScoped.class.getName()));
    }
  }

  @Override
  public T getInstance() {
    if (m_instance != null) {
      return m_instance;
    }
    if (m_bean.getBeanClazz().isInterface()) {
      return null;
    }
    if (BeanContext.isApplicationScoped(m_bean)) {
      m_instanceLock.acquireUninterruptibly();
      try {
        if (m_instance == null) {
          m_instance = BeanInstanceUtil.create(m_bean.getBeanClazz());
        }
        return m_instance;
      }
      finally {
        m_instanceLock.release();
      }
    }
    return BeanInstanceUtil.create(m_bean.getBeanClazz());
  }

  @Override
  public IBean<T> getBean() {
    return m_bean;
  }

  public Double getPriority() {
    double prio = -1;
    Priority priorityAnnotation = getBean().getBeanAnnotation(Priority.class);
    if (priorityAnnotation != null) {
      prio = priorityAnnotation.value();
    }
    return prio;
  }

  @Override
  public int compareTo(T obj) {
    @SuppressWarnings("unchecked")
    BeanRegistration<T> other = (BeanRegistration<T>) obj;

    int cmp = other.getPriority().compareTo(this.getPriority());
    if (cmp != 0) {
      return cmp;
    }

    return this.getBean().getBeanClazz().getName().compareTo(other.getBean().getBeanClazz().getName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_bean.hashCode();
    result = prime * result + (m_isInstanceBasedBean ? 1231 : 1237);
    if (m_isInstanceBasedBean) {
      result = prime * result + ((m_instance == null) ? 0 : m_instance.hashCode());
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BeanRegistration other = (BeanRegistration) obj;
    if (!this.m_bean.equals(other.m_bean)) {
      return false;
    }
    if (m_isInstanceBasedBean != other.m_isInstanceBasedBean) {
      return false;
    }
    if (m_isInstanceBasedBean) {
      if (m_instance == null) {
        if (other.m_instance != null) {
          return false;
        }
      }
      else if (!m_instance.equals(other.m_instance)) {
        return false;
      }
    }
    return true;
  }
}
