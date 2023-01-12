/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.jaxws.apt.internal.codemodel;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JType;

/**
 * Similar to {@link JExpr}, but with some modifications.
 *
 * @since 5.1
 */
public final class JExprEx {

  private JExprEx() {
  }

  /**
   * Casts the given expression without adding double parentheses.
   *
   * @see JExpr#cast(JType, JExpression)
   */
  public static JExpression cast(final JType castType, final JExpression expression) {
    return new JExpressionImpl() {

      @Override
      public void generate(final JFormatter f) {
        f.p(" (").g(castType).p(')').g(expression);
      }
    };
  }
}
