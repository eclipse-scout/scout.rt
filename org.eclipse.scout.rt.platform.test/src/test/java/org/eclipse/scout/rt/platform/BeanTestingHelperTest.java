/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.config.AbstractLongConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author oca
 */
public class BeanTestingHelperTest {

  @IgnoreBean
  static class FixtureBean {
  }

  @Test
  public void testRegisterBeanNull() {
    BeanTestingHelper.get().registerBean(null);
  }

  @Test
  public void testUnregisterBeanNull() {
    BeanTestingHelper.get().unregisterBean(null);
    BeanTestingHelper.get().unregisterBeans(null);
  }

  @Test
  public void testMockConfigProperty() {
    Assert.assertEquals(1L, CONFIG.getPropertyValue(TestingUtilityLongConfigProperty.class).longValue());
    IBean<?> mockProperty = BeanTestingHelper.get().mockConfigProperty(TestingUtilityLongConfigProperty.class, 2L);
    Assert.assertEquals(2L, CONFIG.getPropertyValue(TestingUtilityLongConfigProperty.class).longValue());
    BeanTestingHelper.get().unregisterBean(mockProperty);
    Assert.assertEquals(1L, CONFIG.getPropertyValue(TestingUtilityLongConfigProperty.class).longValue());
  }

  public static class TestingUtilityLongConfigProperty extends AbstractLongConfigProperty {

    @Override
    public String getKey() {
      return getClass().getName();
    }

    @Override
    public String description() {
      return null;
    }

    @Override
    public Long getDefaultValue() {
      return 1L;
    }
  }

  @Test
  public void testRegisterBean() {
    // return value of type <T>
    IBean<FixtureBean> bean1 = BeanTestingHelper.get().registerBean(new BeanMetaData(FixtureBean.class));
    try {
      assertEquals(FixtureBean.class, BEANS.get(FixtureBean.class).getClass());
      assertEquals(BeanTestingHelper.TESTING_BEAN_ORDER, BEANS.getBeanManager().getBean(FixtureBean.class).getBeanAnnotation(Order.class).value(), 0);
    }
    finally {
      BeanTestingHelper.get().unregisterBean(bean1);
    }

    // return value of type <?>
    IBean<?> bean2 = BeanTestingHelper.get().registerBean(new BeanMetaData(FixtureBean.class));
    try {
      assertEquals(FixtureBean.class, BEANS.get(FixtureBean.class).getClass());
      assertEquals(BeanTestingHelper.TESTING_BEAN_ORDER, BEANS.getBeanManager().getBean(FixtureBean.class).getBeanAnnotation(Order.class).value(), 0);
    }
    finally {
      BeanTestingHelper.get().unregisterBean(bean2);
    }

    // return value of type <T>, custom order
    IBean<FixtureBean> bean3 = BeanTestingHelper.get().registerBean(new BeanMetaData(FixtureBean.class).withOrder(42));
    try {
      assertEquals(FixtureBean.class, BEANS.get(FixtureBean.class).getClass());
      assertEquals(42, BEANS.getBeanManager().getBean(FixtureBean.class).getBeanAnnotation(Order.class).value(), 0);
    }
    finally {
      BeanTestingHelper.get().unregisterBean(bean3);
    }
  }
}
