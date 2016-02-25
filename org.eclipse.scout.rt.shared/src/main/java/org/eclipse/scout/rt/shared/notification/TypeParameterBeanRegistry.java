/*******************************************************************************
* Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.index.AbstractMultiValuesIndex;
import org.eclipse.scout.rt.platform.index.IMultiValueIndex;
import org.eclipse.scout.rt.platform.index.IndexedStore;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * A registry for beans implementing an interface with one generic type parameter, and which allows to lookup beans by
 * its generic parameter type.
 */
public class TypeParameterBeanRegistry<BEAN> {

  protected final ReadWriteLock m_lock = new ReentrantReadWriteLock();

  protected final Class<BEAN> m_beanType;

  protected final IndexedStore<BeanRegistration<BEAN>> m_inventory = new IndexedStore<>();
  protected final IMultiValueIndex<Class<?>, BeanRegistration<BEAN>> m_genericTypeIndex;
  protected final Set<Class<?>> m_computedLookupTypes = Collections.synchronizedSet(new HashSet<Class<?>>());

  public TypeParameterBeanRegistry(final Class<BEAN> beanType) {
    m_beanType = beanType;

    // Register the index to lookup beans by their generic parameter type, or by a super type of that generic parameter type.
    m_genericTypeIndex = m_inventory.registerIndex(new AbstractMultiValuesIndex<Class<?>, BeanRegistration<BEAN>>() {

      @Override
      protected Set<Class<?>> calculateIndexesFor(final BeanRegistration<BEAN> beanRegistration) {
        final Set<Class<?>> indexValues = new HashSet<>();
        indexValues.add(beanRegistration.getGenericTypeParameter());
        indexValues.addAll(beanRegistration.getLazyLookupSubTypes());
        return indexValues;
      }
    });
  }

  /**
   * Registers the given beans.
   *
   * @return A token representing the registration of the given beans. This token can later be used to unregister the
   *         beans.
   */
  public IRegistrationHandle registerBeans(final List<BEAN> beans) {
    final List<IRegistrationHandle> registrations = new ArrayList<>();

    for (final BEAN bean : beans) {
      registrations.add(registerBean(bean));
    }

    return new IRegistrationHandle() {

      @Override
      public void dispose() {
        for (final IRegistrationHandle registration : registrations) {
          registration.dispose();
        }
      }
    };
  }

  /**
   * Registers the given bean.
   *
   * @return A token representing the registration of the given bean. This token can later be used to unregister the
   *         bean.
   */
  public IRegistrationHandle registerBean(final BEAN bean) {
    m_computedLookupTypes.clear();

    m_lock.writeLock().lock();
    try {
      final Class genericParameterType = TypeCastUtility.getGenericsParameterClass(bean.getClass(), m_beanType);
      final BeanRegistration<BEAN> beanRegistration = new BeanRegistration<>(bean, genericParameterType);

      // Add the bean to the inventory. Thereby, the indexes are computed for that element.
      m_inventory.add(beanRegistration);

      return new IRegistrationHandle() {

        @Override
        public void dispose() {
          m_inventory.remove(beanRegistration);
        }
      };
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  /**
   * Returns all beans with the given generic type parameter, or which are declared with a super type of that parameter.
   * <p>
   * The beans are returned in the original order (see {@link #registerBeans(Class, List)}).
   * </p>
   *
   * @param lookupType
   *          generic parameter type, not <code>null</code>.
   */
  public List<BEAN> getBeans(final Class<?> lookupType) {
    updateLookupTypeHierarchyIndex(lookupType);

    m_lock.readLock().lock();
    try {
      final List<BEAN> beans = new ArrayList<>();
      for (final BeanRegistration<BEAN> registration : m_genericTypeIndex.get(lookupType)) {
        beans.add(registration.getBean());
      }
      return beans;
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  /**
   * Registers the given lookup type with beans which have a generic type parameter that is a super type of the given
   * lookup type.
   * <p>
   * This is done only the first time a lookup for that lookup type is done.
   */
  protected void updateLookupTypeHierarchyIndex(final Class<?> lookupType) {
    if (m_computedLookupTypes.contains(lookupType)) {
      return;
    }

    m_lock.writeLock().lock();
    try {
      if (m_computedLookupTypes.contains(lookupType)) {
        return; // double checked locking
      }

      for (final BeanRegistration<BEAN> element : m_inventory.values()) {
        if (element.getGenericTypeParameter().isAssignableFrom(lookupType)) {
          element.registerLazyLookupSubType(lookupType);

          // Replace the element in the inventory to update the index.
          m_inventory.remove(element);
          m_inventory.add(element);
        }
      }
      m_computedLookupTypes.add(lookupType);
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  protected static class BeanRegistration<T> {

    private final T m_bean;
    private final Class<?> m_genericTypeParameter;

    private final Set<Class<?>> m_lazyLookupSubTypes;

    public BeanRegistration(final T bean, final Class<?> genericTypeParameter) {
      m_bean = bean;
      m_genericTypeParameter = genericTypeParameter;
      m_lazyLookupSubTypes = new HashSet<>();
    }

    public void registerLazyLookupSubType(final Class<?> lookupType) {
      m_lazyLookupSubTypes.add(lookupType);
    }

    public T getBean() {
      return m_bean;
    }

    /**
     * Returns the generic bean type.
     */
    public Class<?> getGenericTypeParameter() {
      return m_genericTypeParameter;
    }

    /**
     * Returns the sub-types of the bean's generic type parameter. Those sub types are only populated upon a bean lookup
     * for such a sub type, and which is remembered to speed up subsequent lookups.
     */
    public Set<Class<?>> getLazyLookupSubTypes() {
      return m_lazyLookupSubTypes;
    }
  }
}
