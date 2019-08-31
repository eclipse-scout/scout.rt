/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6.parser;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;

import jdk.nashorn.api.tree.Tree;
import jdk.nashorn.api.tree.Tree.Kind;

public class ES6Node<N extends Tree> {
  private final ES6Ast m_ast;
  private final N m_impl;
  private ES6Node<?> m_parent;
  private List<ES6Node<?>> m_children = Collections.emptyList();
  private int m_startOfComment = -1;
  private int m_start;
  private int m_end;
  private String m_source;
  private String m_comment = "";

  public ES6Node(ES6Ast ast, N impl) {
    m_ast = ast;
    m_impl = impl;
    m_start = (int) impl.getStartPosition();
    m_end = (int) impl.getEndPosition();

    if (m_start > m_end) {
      m_end = m_start;
      //will be handled in adjustEndPosition
    }
    m_source = m_ast.getSource().substring(m_start, m_end);
  }

  protected void adjustPositionsPass1() {
    int bakStart = m_start;
    int bakEnd = m_end;

    //bug fix: path identifier parse with negative length
    if (getKind() == Kind.IDENTIFIER) {
      String all = m_ast.getSource();
      if (m_start == m_end) {
        //regular id
        while (m_end < all.length() && isIdentifierChar(all.charAt(m_end))) {
          m_end++;
        }
      }
      if (m_start - 1 >= 0 && all.charAt(m_start - 1) == '\'') {
        //quoted ES6 id
        m_start--;
        autoFixEndPosition("'");
      }
      else if (m_start - 1 >= 0 && all.charAt(m_start - 1) == '\"') {
        //quoted ES6 id
        m_start--;
        m_start--;
        autoFixEndPosition("'");
      }
    }

    //bug fix: skipping last string quote character
    if (getKind() == Kind.STRING_LITERAL) {
      String all = m_ast.getSource();
      if (m_start - 1 >= 0 && all.charAt(m_start - 1) == '\'') {
        int pos = m_start;
        while (pos < all.length() && all.charAt(pos) != '\'') {
          pos++;
        }
        m_parent.setEndPosition(Math.max(m_parent.getEndPosition(), pos + 1));
      }
      else if (m_start - 1 >= 0 && all.charAt(m_start - 1) == '\"') {
        int pos = m_start;
        while (pos < all.length() && all.charAt(pos) != '\"') {
          pos++;
        }
        m_parent.setEndPosition(Math.max(m_parent.getEndPosition(), pos + 1));
      }
    }

    String s = m_ast.getSource().substring(m_start, m_end);
    switch (getKind()) {
      //bug fix: missing 'export' and closing '}'
      case IMPORT_ENTRY:
        autoFixEndPosition(";");
        autoFixStartPosition("import");
        break;
      //bug fix: missing closing '}'
      case EXPORT_ENTRY:
        if (s.startsWith("{")) {
          autoFixEndPosition(";");
        }
        autoFixStartPosition("export");
        break;
      //bug fix: missing closing '}'
      case BLOCK:
        if (s.startsWith("{")) {
          autoFixEndPosition("}");
        }
        break;
    }

    if (m_start == bakStart && m_end == bakEnd) {
      return;
    }
    m_source = m_ast.getSource().substring(m_start, m_end);
  }

  protected void adjustPositionsPass2() {
    int bakStart = m_start;
    int bakEnd = m_end;

    switch (getKind()) {
      case IMPORT_ENTRY:
      case EXPORT_ENTRY:
      case EXPRESSION_STATEMENT:
      case FUNCTION:
      case FUNCTION_EXPRESSION:
      case VARIABLE:
      case BLOCK:
        autoIncludeOptionalStatementTermination();
        break;
    }

    if (m_start == bakStart && m_end == bakEnd) {
      return;
    }
    m_source = m_ast.getSource().substring(m_start, m_end);
  }

  protected void setEndPosition(int end) {
    if (m_end == end) {
      return;
    }
    m_end = end;
    m_source = m_ast.getSource().substring(m_start, m_end);
  }

  private void autoFixStartPosition(String expectedStart) {
    String s = m_ast.getSource().substring(m_start, m_end);
    if (s.startsWith(expectedStart)) return;

    int pos = m_ast.getSource().substring(0, m_start).lastIndexOf(expectedStart);
    if (pos < 0) throw new ProcessingException("missing '{}'", expectedStart);
    m_start = pos;
  }

  private void autoFixEndPosition(String expectedEnd) {
    String s = m_ast.getSource().substring(m_start, m_end);
    if (s.endsWith(expectedEnd)) return;

    int pos = m_ast.getSource().indexOf(expectedEnd, m_end - expectedEnd.length());
    if (pos < 0) {
      throw new ProcessingException("missing '{}'", expectedEnd);
    }
    m_end = pos + 1;
  }

  private void autoIncludeOptionalStatementTermination() {
    String s = m_ast.getSource();
    int i = m_end;
    while (i < s.length() && isOptionalStatementTermination(s.charAt(i))) {
      i++;
    }
    //remove trailing spaces only
    while (i > m_end && s.charAt(i - 1) == ' ') {
      i--;
    }
    m_end = i;
  }

  private static boolean isIdentifierChar(char c) {
    if (Character.isJavaIdentifierStart(c)) return true;
    if (Character.isJavaIdentifierPart(c)) return true;
    switch (c) {
      case '@':
      case '-':
      case '_':
      case '$':
      case '.':
      case '/':
        return true;
    }
    return false;
  }

  private static boolean isOptionalStatementTermination(char c) {
    switch (c) {
      case ' ':
      case '\n':
      case '\r':
      case '\t':
      case ';':
        return true;
    }
    return false;
  }

  public ES6Ast getAst() {
    return m_ast;
  }

  public ES6Node<?> getParent() {
    return m_parent;
  }

  protected void setParent(ES6Node<?> parent) {
    m_parent = parent;
  }

  public List<ES6Node<?>> getChildren() {
    return m_children;
  }

  protected void setChildren(List<ES6Node<?>> children) {
    m_children = children;
  }

  public int getCommentStartPosition() {
    return m_startOfComment >= 0 ? m_startOfComment : m_start;
  }

  public void setCommentStartPosition(int pos) {
    m_startOfComment = pos;
    if (pos < m_start)
      m_comment = m_ast.getSource().substring(m_startOfComment, m_start);
    else {
      m_comment = "";
    }
  }

  public int getStartPosition() {
    return m_start;
  }

  public int getEndPosition() {
    return m_end;
  }

  public int getLine() {
    return (int) m_ast.getRoot().getImpl().getLineMap().getLineNumber(m_impl.getStartPosition());
  }

  public int getColumn() {
    return (int) m_ast.getRoot().getImpl().getLineMap().getColumnNumber(m_impl.getStartPosition()) + 1;
  }

  public String getComment() {
    return m_comment;
  }

  public String getSource() {
    return m_source;
  }

  public Kind getKind() {
    return m_impl.getKind();
  }

  public int getDepth() {
    ES6Node<?> n = this;
    int count = 0;
    while (n.getParent() != null) {
      count++;
      n = n.getParent();
    }
    return count;
  }

  public N getImpl() {
    return m_impl;
  }

  /**
   * Visit tree using depth first strategy. First children, then node itself.
   */
  public void visitDepthFirst(Consumer<ES6Node<?>> visitor) {
    m_children.forEach(c -> c.visitDepthFirst(visitor));
    visitor.accept(this);
  }

  /**
   * Visit tree using breath first strategy. First node itself then all children.
   */
  public void visitBreathFirst(Consumer<ES6Node<?>> visitor) {
    visitor.accept(this);
    m_children.forEach(c -> c.visitBreathFirst(visitor));
  }

  @Override
  public String toString() {
    return toString(80, 20);
  }

  public String toString(int maxPrefix, int maxSuffix) {
    String s = getSource();
    s = s.replace("\r", "\\r");
    s = s.replace("\n", "\\n");
    s = s.replace("\t", "\\t");
    if (s.length() > maxPrefix + maxSuffix) {
      s = (s.substring(0, maxPrefix)) + " ... " + (s.substring(s.length() - maxSuffix, s.length()));
    }
    return StringUtility.repeat(" ", getDepth() * 2) + "[" + getLine() + ":" + getColumn() + "]" + " [" + getKind() + "] " + s;
  }
}
