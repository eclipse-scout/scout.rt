/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.AnnotationFactory;
import org.eclipse.scout.rt.platform.BeanData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.junit.runners.model.Statement;

/**
 * Statement to register a bean during the time of execution.
 *
 * @since 5.1
 */
public class RegisterBeanStatement extends Statement {

  protected final Statement m_next;
  private final Class<?> m_beanClass;

  /**
   * Creates a statement to register a bean-class during the time of executing a statement.
   *
   * @param next
   *          next {@link Statement} to be executed.
   * @param beanClass
   *          bean-class to be registered.
   *          order of the bean-class to be registered. Lowest value is first in result list (preferred).
   */
  public RegisterBeanStatement(final Statement next, final Class<?> beanClass) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_beanClass = Assertions.assertNotNull(beanClass, "bean-class must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    final BeanData bean = new BeanData(m_beanClass);
    bean.addAnnotation(AnnotationFactory.createOrder(-1000));

    IBean reg = Platform.get().getBeanContext().registerBean(bean);
    try {
      m_next.evaluate();
    }
    finally {
      Platform.get().getBeanContext().unregisterBean(reg);
    }
  }
}
