/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.mock;

import java.util.List;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Shortcut to replace a application scoped bean within a test.
 */
public class RegisterBeanTestRule<BEAN> implements TestRule {

  private final Class<? super BEAN> m_beanClazz;
  private final BEAN m_mock;

  private List<IBean<?>> m_temporaryBeans;

  public RegisterBeanTestRule(Class<? super BEAN> beanClazz, BEAN mock) {
    m_beanClazz = beanClazz;
    m_mock = mock;
  }

  public void registerBean() {
    m_temporaryBeans = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(m_beanClazz)
            .withApplicationScoped(true)
            .withInitialInstance(m_mock));
  }

  public void unregisterBean() {
    BeanTestingHelper.get().unregisterBeans(m_temporaryBeans);
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        try {
          registerBean();
          base.evaluate();
        }
        finally {
          unregisterBean();
        }
      }
    };
  }
}
