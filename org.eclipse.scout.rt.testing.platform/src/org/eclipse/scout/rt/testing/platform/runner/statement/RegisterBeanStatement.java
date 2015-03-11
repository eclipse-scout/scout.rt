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
import org.eclipse.scout.rt.platform.cdi.BeanImplementor;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.junit.runners.model.Statement;

/**
 * Statement to register a bean-class during the time of executing a statement.
 * 
 * @since5.1
 */
public class RegisterBeanStatement extends Statement {

  protected final Statement m_next;
  private final Class<?> m_beanClass;
  private final double m_priority;

  /**
   * Creates a statement to register a bean-class during the time of executing a statement.
   *
   * @param next
   *          next {@link Statement} to be executed.
   * @param beanClass
   *          bean-class to be registered.
   * @param priority
   *          priority of the bean-class to be registered.
   */
  public RegisterBeanStatement(final Statement next, final Class<?> beanClass, final double priority) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_beanClass = Assertions.assertNotNull(beanClass, "bean-class must not be null");
    m_priority = priority;
  }

  @Override
  public void evaluate() throws Throwable {
    final BeanImplementor<?> bean = new BeanImplementor<>(m_beanClass);
    bean.addAnnotation(AnnotationFactory.createPriority(m_priority));

    OBJ.registerBean(bean, null);
    try {
      m_next.evaluate();
    }
    finally {
      OBJ.unregisterBean(bean);
    }
  }
}
