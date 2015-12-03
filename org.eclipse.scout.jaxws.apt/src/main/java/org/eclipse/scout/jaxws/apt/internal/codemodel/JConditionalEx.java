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
public class JConditionalEx implements JStatement {

  private final List<P_ElseIf> m_elseIfs = new ArrayList<>();

  private JExpression m_test = null;
  private final JBlock _then = new JBlock();
  private JBlock _else = null;

  public JConditionalEx(final JBlock block) {
    block.add(this);
  }

  public JBlock _if(final JExpression test) {
    m_test = test;
    return _then;
  }

  public JBlock _then() {
    return _then;
  }

  public JBlock _else() {
    if (_else == null) {
      _else = new JBlock();
    }
    return _else;
  }

  public JBlock _elseif(final JExpression boolExp) {
    if (m_test == null) {
      m_test = boolExp;
      return _then;
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
    f.g(_then);

    for (final P_ElseIf elseIf : m_elseIfs) {
      f.nl().p("else if ").g(elseIf.getExpr()).g(elseIf.getThenBlock());
    }
    if (_else != null) {
      f.nl().p("else").g(_else);
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
