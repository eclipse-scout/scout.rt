/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
  private final IConfigProperty<DATA_TYPE> m_mock;

  private List<IBean<?>> m_temporaryBeans;

  public MockConfigPropertyRule(Class<? extends IConfigProperty<DATA_TYPE>> configPropertyClazz, DATA_TYPE value) {
    m_configPropertyClazz = configPropertyClazz;

    m_mock = mock(m_configPropertyClazz);
    when(m_mock.getValue()).thenReturn(value);
    when(m_mock.getValue(nullable(String.class))).thenReturn(value);
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
          registerProperty();
          base.evaluate();
        }
        finally {
          unregisterProperty();
        }
      }
    };
  }

}
