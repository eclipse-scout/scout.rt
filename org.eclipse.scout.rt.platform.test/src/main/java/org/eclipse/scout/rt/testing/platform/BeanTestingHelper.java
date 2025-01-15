/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@ApplicationScoped
public class BeanTestingHelper {

  /**
   * Order to make sure testing beans are preferred over regular beans
   */
  public static final int TESTING_BEAN_ORDER = -10000;

  /**
   * Order for testing beans to be used when needed
   */
  public static final int TESTING_RESOURCE_ORDER = 10000;

  public static BeanTestingHelper get() {
    return BEANS.get(BeanTestingHelper.class);
  }

  /**
   * Registers the given beans in the {@link IBeanManager} of {@link Platform#get()} with an {@link Order} value of
   * {@link #TESTING_BEAN_ORDER} (if none is already set) that overrides all other beans
   * <p>
   * If registering Mockito mocks, use {@link BeanMetaData(Class, Object)}.
   *
   * @return the registrations
   */
  public List<IBean<?>> registerBeans(BeanMetaData... beanDatas) {
    if (beanDatas == null) {
      return CollectionUtility.emptyArrayList();
    }
    List<IBean<?>> registeredBeans = new ArrayList<>();
    for (BeanMetaData beanData : beanDatas) {
      registeredBeans.add(registerBean(beanData));
    }
    return registeredBeans;
  }

  /**
   * Registers the given bean in the {@link IBeanManager} of {@link Platform#get()} with an {@link Order} value of
   * {@link #TESTING_BEAN_ORDER} (if none is already set) that overrides all other beans.
   * <p>
   * If registering Mockito mocks, use {@link BeanMetaData(Class, Object)}.
   *
   * @return the registration
   */
  public <T> IBean<T> registerBean(BeanMetaData beanData) {
    if (beanData == null) {
      return null;
    }
    boolean isMock = Mockito.mockingDetails(beanData.getBeanClazz()).isMock();
    Assertions.assertFalse(isMock && beanData.getInitialInstance() == null, "Cannot register mocked bean without initial instance. [mock={}]", beanData.getBeanClazz());
    if (beanData.getBeanAnnotation(Order.class) == null) {
      beanData.withOrder(TESTING_BEAN_ORDER);
    }
    interceptRegisterBean(beanData);
    return Platform.get().getBeanManager().registerBean(beanData);
  }

  protected void interceptRegisterBean(BeanMetaData beanData) {
    // to be overridden by subclasses if specific behavior is required
  }

  /**
   * Unregister a bean
   */
  public void unregisterBean(IBean<?> bean) {
    unregisterBeans(Arrays.asList(bean));
  }

  /**
   * Unregisters the given beans
   *
   * @param beans
   *     The {@link IBean beans} to remove from the {@link IBeanManager}.
   */
  public void unregisterBeans(List<? extends IBean<?>> beans) {
    if (beans == null) {
      return;
    }
    IBeanManager manager = Platform.get().getBeanManager();
    beans.stream().filter(Objects::nonNull).forEach(manager::unregisterBean);
  }

  @SuppressWarnings("unchecked")
  public <T> IBean<?> mockConfigProperty(Class<? extends IConfigProperty<T>> propertyClass, T value) {
    IConfigProperty<?> mock = Mockito.mock(IConfigProperty.class);
    Mockito.when((T) mock.getValue(ArgumentMatchers.any())).thenReturn(value);
    Mockito.when((T) mock.getValue()).thenReturn(value);
    return registerBean(new BeanMetaData(propertyClass, mock));
  }
}
