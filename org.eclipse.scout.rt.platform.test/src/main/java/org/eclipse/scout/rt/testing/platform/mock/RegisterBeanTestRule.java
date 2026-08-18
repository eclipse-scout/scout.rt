/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.mock;

import static org.junit.Assert.assertEquals;

import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.util.AbstractScoutTestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shortcut to replace a application scoped bean within a test.
 */
public class RegisterBeanTestRule<BEAN> extends AbstractScoutTestRule {

  private static final Logger LOG = LoggerFactory.getLogger(RegisterBeanTestRule.class);

  private final Class<? super BEAN> m_beanClazz;
  private final Supplier<? extends BEAN> m_mockSupplier;

  public RegisterBeanTestRule(Class<? super BEAN> beanClazz, BEAN mock) {
    this(beanClazz, () -> mock);
  }

  public RegisterBeanTestRule(Class<? super BEAN> beanClazz, Supplier<? extends BEAN> mockSupplier) {
    m_beanClazz = beanClazz;
    m_mockSupplier = mockSupplier;
  }

  protected IBean<Object> registerBean() {
    IBean<? super BEAN> bean = Platform.get().getBeanManager().optBean(m_beanClazz);
    if (bean != null) {
      if (m_beanClazz.isInterface() && !Objects.equals(bean.getBeanClazz(), m_beanClazz)) {
        // it may be valid to set the bean clazz to an interface, however it will also just be registered for the interface then (and not be returned when bean is accessed directly by the already registered class)
        LOG.warn("Bean is only registered for the interface {}, if applications accesses bean directly (e.g. using the already registered class {}) our value will not be returned.", m_beanClazz, bean.getBeanClazz());
      }
      else {
        // load existing bean (optional) to ensure there are no additional replacements which are not overwritten; beanClazz must be the actual class of the bean which would be returned otherwise, else mocking would not be successful
        assertEquals("Bean is already registered with another bean class, rule must be used with bean class of actual registered bean!", bean.getBeanClazz(), m_beanClazz);
      }
    }
    return BeanTestingHelper.get().registerBean(
        new BeanMetaData(m_beanClazz)
            .withApplicationScoped(true)
            .withInitialInstance(m_mockSupplier.get()));
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        IBean<Object> bean = registerBean();
        try {
          base.evaluate();
        }
        finally {
          BeanTestingHelper.get().unregisterBean(bean);
        }
      }
    };
  }
}
