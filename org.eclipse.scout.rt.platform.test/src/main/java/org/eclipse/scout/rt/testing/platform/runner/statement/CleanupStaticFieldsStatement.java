/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanupStaticFieldsStatement extends Statement {

  private static final Logger LOG = LoggerFactory.getLogger(CleanupStaticFieldsStatement.class);

  private final Statement m_next;
  private final Class<?> m_testClass;

  public CleanupStaticFieldsStatement(Statement next, Class<?> testClass) {
    m_next = next;
    m_testClass = testClass;
  }

  @Override
  public void evaluate() throws Throwable {
    try {
      m_next.evaluate();
    }
    finally {
      Class<?> currentClass = m_testClass;
      while (currentClass != null) {
        for (Field field : currentClass.getDeclaredFields()) {
          int modifiers = field.getModifiers();
          boolean staticField = Modifier.isStatic(modifiers);
          boolean finalField = Modifier.isFinal(modifiers);

          if (!staticField || finalField) {
            continue;
          }

          if (field.getType().isPrimitive()) {
            continue;
          }

          LOG.info("{}#{}: setting static field to null after test is finished", currentClass.getName(), field.getName());

          field.setAccessible(true);
          field.set(null, null);
        }

        currentClass = currentClass.getSuperclass();
      }
    }
  }
}
