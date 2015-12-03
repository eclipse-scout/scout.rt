/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
