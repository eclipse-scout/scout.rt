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

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JStatement;

/**
 * Similar to {@link JConditional} but with support for <code>if-else</code> conditions.
 *
 * @see JConditional
 * @since 5.1
 */
@SuppressWarnings("squid:S00100")
public class JConditionalEx implements JStatement {

  private final List<P_ElseIf> m_elseIfs = new ArrayList<>();

  private JExpression m_test = null;
  private final JBlock m_then = new JBlock();
  private JBlock m_else = null;

  public JConditionalEx(final JBlock block) {
    block.add(this);
  }

  public JBlock _if(final JExpression test) {
    m_test = test;
    return m_then;
  }

  public JBlock _then() {
    return m_then;
  }

  public JBlock _else() {
    if (m_else == null) {
      m_else = new JBlock();
    }
    return m_else;
  }

  public JBlock _elseif(final JExpression boolExp) {
    if (m_test == null) {
      m_test = boolExp;
      return m_then;
    }
    else {
      final P_ElseIf elseIf = new P_ElseIf(boolExp, new JBlock(true, true));
      m_elseIfs.add(elseIf);
      return elseIf.getThenBlock();
    }
  }

  @Override
  public void state(final JFormatter f) {
    f.p("if ").g(m_test);
    f.g(m_then);

    for (final P_ElseIf elseIf : m_elseIfs) {
      f.nl().p("else if ").g(elseIf.getExpr()).g(elseIf.getThenBlock());
    }
    if (m_else != null) {
      f.nl().p("else").g(m_else);
    }
    f.nl();
  }

  private class P_ElseIf {
    private final JExpression m_expr;
    private final JBlock m_thenBlock;

    public P_ElseIf(final JExpression expr, final JBlock thenBlock) {
      m_expr = expr;
      m_thenBlock = thenBlock;
    }

    public JExpression getExpr() {
      return m_expr;
    }

    public JBlock getThenBlock() {
      return m_thenBlock;
    }
  }
}
