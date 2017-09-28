/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jdbc.parsers.sql;

import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.AndExpr;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.AndOp;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.Atom;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.BracketExpr;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.CloseBracketToken;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.Comment;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.FunExpr;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.ListExpr;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.ListSeparator;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.MathExpr;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.MathOp;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.MinusExpr;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.Name;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.OpenBracketToken;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.OrExpr;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.OrOp;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.Part;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.PartToken;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.SingleStatement;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.Statement;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.UnaryPrefix;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.UnaryPrefixExpr;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.UnionToken;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.Unparsed;

/**
 * see {@link SqlParser}
 *
 * <pre>
 * <code>
 * Statement = SingleStatement (UnionToken SingleStatement)* (Unparsed)?
 * SingleStatement = Part+
 * Part = PartToken ListExpr
 * ListExpr = OrExpr (ListSeparator OrExpr)*
 * OrExpr = AndExpr (BinaryOp['OR'] AndExpr)*
 * AndExpr = MathExpr (BinaryOp['AND'] MathExpr)*
 * MathExpr = _simpleExpr (BinaryOp _simpleExpr)*
 * _simpleExpr = UnaryPrefixExpr | MinusExpr | Atom
 * UnaryPrefixExpr = UnaryPrefix Atom
 * MinusExpr = BinaryOp['-'] Atom
 * Atom= (BracketExpr | Statement | OrExpr | FunExpr | Name | Text | BinaryOp['*']) (OuterJoinToken)? (Name["AS"])? (Name[alias])?
 * BracketExpr = OpenBracketToken (Statement | ListExpr) CloseBracketToken
 * FunExpr = Name BracketExpr
 * </code>
 * </pre>
 */
public class SqlFormatter {

  private static class FormatContext {
    private final int m_tabSize;
    private int m_indent;
    private int m_linePos;
    private final StringBuilder m_buf;

    public FormatContext(int tabSize) {
      m_tabSize = tabSize;
      m_buf = new StringBuilder();
    }

    public void in() {
      m_indent += m_tabSize;
      if (m_linePos >= m_indent) {
        print(" ");
      }
    }

    public void out() {
      m_indent -= m_tabSize;
      if (m_linePos > m_indent) {
        println();
      }
    }

    public void println() {
      print("\n");
    }

    public void println(String s) {
      print(s);
      print("\n");
    }

    public void print(String s) {
      for (char ch : s.toCharArray()) {
        if (ch == '\n') {
          m_buf.append('\n');
          m_linePos = 0;
        }
        else {
          //check indentation
          while (m_linePos < m_indent) {
            m_buf.append(' ');
            m_linePos++;
          }
          m_buf.append(ch);
          m_linePos++;
        }
      }
    }

    public String getBuffer() {
      return m_buf.toString();
    }
  }

  public static String wellform(String s) {
    return new SqlFormatter().wellform(s, 10);
  }

  public String wellform(String s, int tabSize) {
    Statement stm = new SqlParser().parse(s);
    FormatContext ctx = new FormatContext(tabSize);
    formatStatement(stm, ctx);
    return ctx.getBuffer().trim();
  }

  private void formatStatement(Statement stm, FormatContext ctx) {
    if (stm != null) {
      for (IToken t : stm.getChildren()) {
        if (t instanceof Statement) {
          formatStatement((Statement) t, ctx);
        }
        else if (t instanceof SingleStatement) {
          formatSingleStatement((SingleStatement) t, ctx);
        }
        else if (t instanceof BracketExpr) {
          formatBracketExpr((BracketExpr) t, ctx);
        }
        else if (t instanceof UnionToken) {
          formatDefault(t, ctx);
          ctx.println();
        }
        else if (t instanceof Unparsed) {
          ctx.println("*** UNPARSED ***");
          formatDefault(t, ctx);
          ctx.println();
        }
        else {
          formatDefault(t, ctx);
        }
      }
    }
  }

  private void formatSingleStatement(SingleStatement stm, FormatContext ctx) {
    for (IToken t : stm.getChildren()) {
      if (t instanceof Part) {
        formatPart((Part) t, ctx);
      }
      else {
        formatDefault(t, ctx);
      }
    }
  }

  private void formatPart(Part stm, FormatContext ctx) {
    boolean hasList = false;
    for (IToken t : stm.getChildren()) {
      if (t instanceof PartToken) {
        formatDefault(t, ctx);
      }
      else if (t instanceof Part) {
        ctx.in();
        formatPart((Part) t, ctx);
        ctx.out();
      }
      else if (t instanceof ListExpr) {
        hasList = true;
        ctx.in();
        formatListExpr((ListExpr) t, true, ctx);
        ctx.out();
      }
      else {
        formatDefault(t, ctx);
      }
    }
    if (!hasList) {
      ctx.in();
      ctx.print(" ");
      ctx.out();
    }
  }

  private void formatListExpr(ListExpr stm, boolean multiline, FormatContext ctx) {
    for (IToken t : stm.getChildren()) {
      if (t instanceof OrExpr) {
        formatOrExpr((OrExpr) t, ctx);
      }
      else if (t instanceof ListSeparator) {
        formatDefault(t, ctx);
        if (multiline) {
          ctx.println();
        }
        else {
          ctx.print(" ");
        }
      }
      else {
        formatDefault(t, ctx);
      }
    }
  }

  private void formatOrExpr(OrExpr stm, FormatContext ctx) {
    for (IToken t : stm.getChildren()) {
      if (t instanceof AndExpr) {
        formatAndExpr((AndExpr) t, ctx);
      }
      else if (t instanceof OrOp) {
        ctx.println();
        formatDefault(t, ctx);
        ctx.println();
      }
      else {
        formatDefault(t, ctx);
      }
    }
  }

  private void formatAndExpr(AndExpr stm, FormatContext ctx) {
    for (IToken t : stm.getChildren()) {
      if (t instanceof MathExpr) {
        formatMathExpr((MathExpr) t, ctx);
      }
      else if (t instanceof AndOp) {
        ctx.println();
        formatDefault(t, ctx);
        ctx.print(" ");
      }
      else {
        formatDefault(t, ctx);
      }
    }
  }

  private void formatMathExpr(MathExpr stm, FormatContext ctx) {
    for (IToken t : stm.getChildren()) {
      if (!(t instanceof MathOp)) {
        formatSimpleExpr(t, ctx);
      }
      else {
        ctx.print(" ");
        formatDefault(t, ctx);
        ctx.print(" ");
      }
    }
  }

  private void formatSimpleExpr(IToken stm, FormatContext ctx) {
    IToken t = stm;
    if (t instanceof UnaryPrefixExpr) {
      formatUnaryPrefixExpr((UnaryPrefixExpr) t, ctx);
    }
    else if (t instanceof MinusExpr) {
      formatMinusExpr((MinusExpr) t, ctx);
    }
    else if (t instanceof Atom) {
      formatAtom((Atom) t, ctx);
    }
    else {
      formatDefault(t, ctx);
    }
  }

  private void formatUnaryPrefixExpr(UnaryPrefixExpr stm, FormatContext ctx) {
    for (IToken t : stm.getChildren()) {
      if (t instanceof UnaryPrefix) {
        formatDefault(t, ctx);
        ctx.print(" ");
      }
      else if (t instanceof Atom) {
        formatAtom((Atom) t, ctx);
      }
      else {
        formatDefault(t, ctx);
      }
    }
  }

  private void formatMinusExpr(MinusExpr stm, FormatContext ctx) {
    for (IToken t : stm.getChildren()) {
      if (t instanceof MathOp) {
        formatDefault(t, ctx);
      }
      else if (t instanceof Atom) {
        formatAtom((Atom) t, ctx);
      }
      else {
        formatDefault(t, ctx);
      }
    }
  }

  private void formatAtom(Atom stm, FormatContext ctx) {
    int index = 0;
    for (IToken t : stm.getChildren()) {
      if (t instanceof BracketExpr) {
        formatBracketExpr((BracketExpr) t, ctx);
      }
      else if (t instanceof Statement) {
        formatStatement((Statement) t, ctx);
      }
      else if (t instanceof Part) {
        formatPart((Part) t, ctx);
      }
      else if (t instanceof OrExpr) {
        formatOrExpr((OrExpr) t, ctx);
      }
      else if (t instanceof FunExpr) {
        formatFunExpr((FunExpr) t, ctx);
      }
      else if (t instanceof Name) {
        if (index > 0) {
          ctx.print(" ");
        }
        formatDefault(t, ctx);
      }
      else {
        formatDefault(t, ctx);
      }
      index++;
    }
  }

  private void formatBracketExpr(BracketExpr stm, FormatContext ctx) {
    boolean multiline = isMultiline(stm);
    for (IToken t : stm.getChildren()) {
      if (t instanceof OpenBracketToken) {
        formatDefault(t, ctx);
        if (multiline) {
          ctx.println();
          ctx.in();
        }
      }
      else if (t instanceof CloseBracketToken) {
        if (multiline) {
          ctx.out();
        }
        formatDefault(t, ctx);
      }
      else if (t instanceof Statement) {
        formatStatement((Statement) t, ctx);
      }
      else if (t instanceof SingleStatement) {
        formatSingleStatement((SingleStatement) t, ctx);
      }
      else if (t instanceof ListExpr) {
        formatListExpr((ListExpr) t, multiline, ctx);
      }
      else {
        formatDefault(t, ctx);
      }
    }
  }

  private void formatFunExpr(FunExpr stm, FormatContext ctx) {
    for (IToken t : stm.getChildren()) {
      if (t instanceof Name) {
        formatDefault(t, ctx);
      }
      else if (t instanceof BracketExpr) {
        formatBracketExpr((BracketExpr) t, ctx);
      }
      else {
        formatDefault(t, ctx);
      }
    }
  }

  private void formatDefault(IToken t, FormatContext ctx) {
    for (Comment c : t.getComments()) {
      ctx.print(c.toString());
    }
    ctx.print(t.toString());
  }

  private boolean isMultiline(IToken stm) {
    if (stm instanceof Statement) {
      return true;
    }
    for (IToken t : stm.getChildren()) {
      if (isMultiline(t)) {
        return true;
      }
    }
    return false;
  }

}
