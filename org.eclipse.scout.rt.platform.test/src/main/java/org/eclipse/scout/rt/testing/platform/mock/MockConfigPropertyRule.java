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

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.util.AbstractScoutTestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class MockConfigPropertyRule<DATA_TYPE> extends AbstractScoutTestRule {

  private final Class<? extends IConfigProperty<DATA_TYPE>> m_configPropertyClazz;
  private DATA_TYPE m_initialValue;
  private DATA_TYPE m_value;

  /**
   * @param defaultValue
   *     the initial value, this value is always restored before each test
   */
  public MockConfigPropertyRule(Class<? extends IConfigProperty<DATA_TYPE>> configPropertyClazz, DATA_TYPE defaultValue) {
    m_configPropertyClazz = configPropertyClazz;
    m_initialValue = defaultValue;
  }

  public void setValue(DATA_TYPE value) {
    m_value = value;
  }

  protected IBean<Object> registerProperty() {
    IConfigProperty<DATA_TYPE> mockConfig = mock(m_configPropertyClazz);
    when(mockConfig.getValue()).thenAnswer(i -> m_value);
    when(mockConfig.getValue(nullable(String.class))).thenAnswer(i -> m_value);

    return BeanTestingHelper.get().registerBean(
        new BeanMetaData(m_configPropertyClazz)
            .withApplicationScoped(true)
            .withInitialInstance(mockConfig));
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        m_value = m_initialValue;
        IBean<Object> bean = registerProperty();
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
