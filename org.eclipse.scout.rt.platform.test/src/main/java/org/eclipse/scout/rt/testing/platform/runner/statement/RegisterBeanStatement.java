/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.junit.runners.model.Statement;

/**
 * Statement to register a bean during the time of executing subsequent statements.
 *
 * @since 5.1
 */
public class RegisterBeanStatement extends Statement {

  protected final Statement m_next;
  protected final BeanMetaData m_beanMetaData;

  /**
   * Creates a statement to register a bean during the time of executing subsequent statements.
   *
   * @param next
   *     next {@link Statement} to be executed.
   * @param beanMetaData
   *     describes the bean to be registered.
   */
  public RegisterBeanStatement(final Statement next, final BeanMetaData beanMetaData) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_beanMetaData = Assertions.assertNotNull(beanMetaData, "BeanMetaData must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    final IBeanManager beanManager = Platform.get().getBeanManager();

    final IBean bean = beanManager.registerBean(m_beanMetaData);
    try {
      m_next.evaluate();
    }
    finally {
      beanManager.unregisterBean(bean);
    }
  }
}
