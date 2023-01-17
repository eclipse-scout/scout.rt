/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.testing.platform.mock;

import java.util.function.Supplier;

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
  private final Supplier<BEAN> m_mockSupplier;

  private IBean<?> m_temporaryBean;

  public RegisterBeanTestRule(Class<? super BEAN> beanClazz, BEAN mock) {
    this(beanClazz, () -> mock);
  }

  public RegisterBeanTestRule(Class<? super BEAN> beanClazz, Supplier<BEAN> mockSupplier) {
    m_beanClazz = beanClazz;
    m_mockSupplier = mockSupplier;
  }

  public void registerBean() {
    m_temporaryBean = BeanTestingHelper.get().registerBean(
        new BeanMetaData(m_beanClazz)
            .withApplicationScoped(true)
            .withInitialInstance(m_mockSupplier.get()));
  }

  public void unregisterBean() {
    BeanTestingHelper.get().unregisterBean(m_temporaryBean);
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
