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

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class MockConfigPropertyRule<DATA_TYPE> implements TestRule {

  private final Class<? extends IConfigProperty<DATA_TYPE>> m_configPropertyClazz;
  private DATA_TYPE m_initialValue;
  private DATA_TYPE m_value;
  private final IConfigProperty<DATA_TYPE> m_mock;

  private List<IBean<?>> m_temporaryBeans;

  /**
   * @param defaultValue
   *     the initial value, this value is always restored before each test
   */
  public MockConfigPropertyRule(Class<? extends IConfigProperty<DATA_TYPE>> configPropertyClazz, DATA_TYPE defaultValue) {
    m_configPropertyClazz = configPropertyClazz;
    m_initialValue = defaultValue;
    m_mock = mock(m_configPropertyClazz);

    when(m_mock.getValue()).thenAnswer(i -> m_value);
    when(m_mock.getValue(nullable(String.class))).thenAnswer(i -> m_value);
  }

  public void registerProperty() {
    m_temporaryBeans = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(m_configPropertyClazz)
            .withApplicationScoped(true)
            .withInitialInstance(m_mock));
  }

  public void unregisterProperty() {
    BeanTestingHelper.get().unregisterBeans(m_temporaryBeans);
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        try {
          m_value = m_initialValue;
          registerProperty();
          base.evaluate();
        }
        finally {
          unregisterProperty();
        }
      }
    };
  }

  public void setValue(DATA_TYPE value) {
    m_value = value;
  }
}
