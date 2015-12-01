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
package org.eclipse.scout.rt.shared.notification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * A registry for beans implementing an interface with one generic parameter type B&ltT&gt that allows querying beans
 * with a given parameter type.
 *
 * @param B
 *          bean type
 */
public class TypeParameterBeanRegistry<B> {

  private final Map<Class<?>, List<B>> m_typeParamToBeans = new LinkedHashMap<>();
  protected final ReadWriteLock m_typeParamToBeansLock = new ReentrantReadWriteLock();

  private final Map<Class<?>, List<B>> m_typeParamSubclassToBeans = new LinkedHashMap<>();
  protected final ReadWriteLock m_typeParamSubclassToBeansLock = new ReentrantReadWriteLock();

  /**
   * Register beans
   *
   * @param definingInterface
   *          interface B&ltT&gt with a type parameter used in this registry
   * @param beans
   *          beans to register
   */
  public void registerBeans(Class<B> definingInterface, List<B> beans) {
    m_typeParamToBeansLock.writeLock().lock();
    try {
      for (B b : beans) {
        Class typeParam = TypeCastUtility.getGenericsParameterClass(b.getClass(), definingInterface);
        List<B> handlerList = m_typeParamToBeans.get(typeParam);
        if (handlerList == null) {
          handlerList = new LinkedList<>();
          m_typeParamToBeans.put(typeParam, handlerList);
        }
        handlerList.add(b);
      }
    }
    finally {
      m_typeParamToBeansLock.writeLock().unlock();
    }
  }

  /**
   * Returns all beans B&ltT&gt in this registry with the given generic parameter type &ltT&gt or a supertype of the
   * given generic parameter type.
   * <p>
   * The beans are returned in the original order (see {@link #registerBeans(Class, List)}).
   * </p>
   *
   * @param typeParamClass
   *          generic parameter type, not <code>null</code>
   */
  public List<B> getBeans(Class<?> typeParamClass) {
    List<B> beans = getCached(Assertions.assertNotNull(typeParamClass));
    if (beans != null) {
      return new ArrayList<>(beans);
    }
    else {
      beans = findBeansForTypeParam(typeParamClass);
      cacheTypeParamSubclass(typeParamClass, beans);
      return new ArrayList<B>(beans);
    }
  }

  private List<B> getCached(Class<?> clazz) {
    m_typeParamSubclassToBeansLock.readLock().lock();
    try {
      return m_typeParamSubclassToBeans.get(clazz);
    }
    finally {
      m_typeParamSubclassToBeansLock.readLock().unlock();
    }
  }

  protected List<B> findBeansForTypeParam(Class<?> typeParam) {
    List<B> beans = new LinkedList<>();
    m_typeParamToBeansLock.readLock().lock();
    try {
      for (Entry<Class<?>, List<B>> e : m_typeParamToBeans.entrySet()) {
        if (e.getKey().isAssignableFrom(typeParam)) {
          beans.addAll(e.getValue());
        }
      }
      return beans;
    }
    finally {
      m_typeParamToBeansLock.readLock().unlock();
    }
  }

  private void cacheTypeParamSubclass(Class<?> clazz, List<B> beans) {
    m_typeParamSubclassToBeansLock.writeLock().lock();
    try {
      m_typeParamSubclassToBeans.put(clazz, beans);
    }
    finally {
      m_typeParamSubclassToBeansLock.writeLock().unlock();
    }
  }

}
