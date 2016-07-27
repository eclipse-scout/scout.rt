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
package org.eclipse.scout.rt.server.jdbc.parsers.sql;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

final class SqlParserToken {

  public interface IToken {
    String getText();

    void setText(String text);

    void addChild(IToken child);

    void addChildren(List<IToken> children);

    List<IToken> getChildren();

    void addComment(Comment c);

    List<Comment> getComments();
  }

  public static abstract class AbstractToken implements IToken {
    private String m_text;
    private List<IToken> m_children;
    private List<Comment> m_comments;

    public AbstractToken() {
    }

    @Override
    public String getText() {
      return m_text;
    }

    @Override
    public void setText(String text) {
      m_text = text;
    }

    @Override
    public void addChild(IToken child) {
      if (m_children == null) {
        m_children = new ArrayList<IToken>();
      }
      m_children.add(child);
    }

    @Override
    public void addChildren(List<IToken> children) {
      if (m_children == null) {
        m_children = new ArrayList<IToken>();
      }
      m_children.addAll(children);
    }

    @Override
    public List<IToken> getChildren() {
      return CollectionUtility.arrayList(m_children);
    }

    @Override
    public void addComment(Comment c) {
      if (m_comments == null) {
        m_comments = new ArrayList<Comment>();
      }
      m_comments.add(c);
    }

    @Override
    public List<Comment> getComments() {
      return CollectionUtility.arrayList(m_comments);
    }

    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder();
      if (getText() != null) {
        buf.append(getText());
      }
      for (IToken t : getChildren()) {
        if (buf.length() > 0) {
          buf.append(" ");
        }
        buf.append(t.toString());
      }
      return buf.toString();
    }
  }

  public static class Raw extends AbstractToken {
    public Raw(String text) {
      setText(text);
    }
  }

  public static class Comment extends AbstractToken {
    public Comment() {
      super();
    }

    public Comment(String text) {
      setText(text);
    }
  }

  public static class Text extends AbstractToken {
  }

  public static class Name extends AbstractToken {
  }

  public static class UnionToken extends AbstractToken {
  }

  public static class PartToken extends AbstractToken {
  }

  public static class UnaryPrefix extends AbstractToken {
  }

  public static class OuterJoinToken extends AbstractToken {
  }

  public static class OrOp extends AbstractToken {
  }

  public static class AndOp extends AbstractToken {
  }

  public static class MathOp extends AbstractToken {
  }

  public static class OpenBracketToken extends AbstractToken {
    private int m_level;

    public int getLevel() {
      return m_level;
    }

    public void setLevel(int level) {
      m_level = level;
    }
  }

  public static class CloseBracketToken extends AbstractToken {
    private int m_level;

    public int getLevel() {
      return m_level;
    }

    public void setLevel(int level) {
      m_level = level;
    }
  }

  public static class ListSeparator extends AbstractToken {
  }

  public static class Statement extends AbstractToken {
  }

  public static class SingleStatement extends AbstractToken {
  }

  public static class Part extends AbstractToken {
  }

  public static class ListExpr extends AbstractToken {
  }

  public static class OrExpr extends AbstractToken {
  }

  public static class AndExpr extends AbstractToken {
  }

  public static class UnaryPrefixExpr extends AbstractToken {
  }

  public static class MinusExpr extends AbstractToken {
  }

  public static class MathExpr extends AbstractToken {
  }

  public static class FunExpr extends AbstractToken {
  }

  public static class BracketExpr extends AbstractToken {
  }

  public static class Atom extends AbstractToken {
  }

  public static class Unparsed extends AbstractToken {
  }
}
