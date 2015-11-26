/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jdbc.parsers.sql;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.server.jdbc.parsers.BindModel;
import org.eclipse.scout.rt.server.jdbc.parsers.BindParser;
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
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.OuterJoinToken;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.Part;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.PartToken;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.Raw;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.SingleStatement;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.Statement;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.Text;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.UnaryPrefix;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.UnaryPrefixExpr;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.UnionToken;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlParserToken.Unparsed;
import org.eclipse.scout.rt.server.jdbc.parsers.token.TextToken;

/**
 * Parser for sql SELECT statements Syntax (EBNF):
 *
 * <pre>
 * <code>
 * Statement = SingleStatement (UnionToken SingleStatement)* (Unparsed)?
 * SingleStatement = Part+ {with first part being a statement root part such as SELECT, INSERT, WITH, ...}
 * Part = PartToken ListExpr
 * ListExpr = OrExpr (ListSeparator OrExpr)*
 * OrExpr = AndExpr (OrOp AndExpr)*
 * AndExpr = MathExpr (AndOp MathExpr)*
 * MathExpr = _simpleExpr (MathOp _simpleExpr)*
 * _simpleExpr = UnaryPrefixExpr | MinusExpr | Atom
 * UnaryPrefixExpr = UnaryPrefix Atom
 * MinusExpr = BinaryOp['-'] Atom
 * Atom= (BracketExpr | Statement | OrExpr | FunExpr | Name | Text | BinaryOp['*']) (OuterJoinToken)? (Name[alias])?
 * BracketExpr = OpenBracketToken (Statement | ListExpr) CloseBracketToken
 * FunExpr = Name BracketExpr
 * Bind = ':' any until whitespace | '#' any '#' | '&' any '&'
 * Name = nameChar+
 * nameChar = {a-zA-Z0-9_$.@:?}
 * </code>
 * </pre>
 */
public class SqlParser {

  private static final String nameChars = "a-zA-Z0-9_$.@:?";
  private static final Pattern COMMENT_PAT = Pattern.compile("(\\{[^\\}]*\\})");
  private static final Pattern APOS_PAT = Pattern.compile("('[^']*')");
  private static final Pattern QUOT_PAT = Pattern.compile("(\"[^\"]*\")");
  //make all uppercase and single space (order of tokens matters!)
  private static final Pattern UNION_PAT = Pattern.compile("[^" + nameChars + "](UNION ALL|INTERSECT|MINUS|UNION)[^" + nameChars + "]");
  private static final Pattern PART_PAT = Pattern.compile(
      "[^" + nameChars + "](WITH|AS|SELECT|FROM|LEFT JOIN|OUTER JOIN|INNER JOIN|JOIN|ON|WHERE|GROUP BY|HAVING|ORDER BY|INSERT INTO|INSERT|INTO|CONNECT BY( NOCYCLE)?|START WITH|UPDATE|DELETE FROM|DELETE|SET|VALUES|CASE|ELSE|END|THEN|WHEN)[^"
          + nameChars + "]");
  private static final Pattern OUTER_JOIN_PAT = Pattern.compile("(\\(\\+\\))");
  private static final Pattern OR_OP_PAT = Pattern.compile("[^" + nameChars + "](OR)[^" + nameChars + "]");
  private static final Pattern AND_OP_PAT = Pattern.compile("[^" + nameChars + "](AND)[^" + nameChars + "]");
  private static final Pattern MATH_OP_PAT1 = Pattern.compile("[^" + nameChars + "](NOT IN|IN|IS NOT|IS|NOT BETWEEN|BETWEEN|NOT LIKE|LIKE)[^" + nameChars + "]");
  private static final Pattern MATH_OP_PAT2 = Pattern.compile("(=|<>|!=|<=|>=|<|>|\\%|\\^|\\+|\\-|\\*|/|\\|\\||\\&\\&)");
  private static final Pattern UNARY_PREFIX_PAT = Pattern.compile("[^" + nameChars + "](NOT|DISTINCT|NEW)[^" + nameChars + "]");
  //eliminate all remaining spaces
  private static final Pattern NAME_PAT = Pattern.compile("([" + nameChars + "]+)");
  private static final Pattern OPEN_BRACKET_PAT = Pattern.compile("([(])");
  private static final Pattern CLOSE_BRACKET_PAT = Pattern.compile("([)])");
  private static final Pattern LIST_SEPARATOR_PAT = Pattern.compile("([,])");

  /**
   * avoid dead-lock
   */
  private static class ParseContext {
    private HashSet<ParseStep> m_steps;
    private HashMap<String, String> m_bindMap;

    public ParseContext() {
      m_steps = new HashSet<ParseStep>();
      m_bindMap = new HashMap<String, String>();
    }

    public Map<String/*code*/, String/*name*/> getBinds() {
      return m_bindMap;
    }

    /**
     * @return true if the operation can be performed and the context was added, returns false when the operation is a
     *         loop and the context was NOT added
     */
    public ParseStep checkAndAdd(String method, List<IToken> list) {
      ParseStep step = new ParseStep(method, list.size() > 0 ? list.get(0) : null);
      if (m_steps.contains(step)) {
        return null;
      }
      m_steps.add(step);
      return step;
    }

    public void remove(ParseStep step) {
      m_steps.remove(step);
    }
  }

  private static class ParseStep {
    private final String m_method;
    private final IToken m_token;

    public ParseStep(String method, IToken token) {
      m_method = method;
      m_token = token;
    }

    @Override
    public int hashCode() {
      return m_method.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ParseStep)) {
        return false;
      }
      ParseStep o = (ParseStep) obj;
      return this.m_method.equals(o.m_method) && this.m_token == o.m_token;
    }

    @Override
    public String toString() {
      return m_method + ": " + m_token;
    }
  }

  public SqlParser() {
  }

  public Statement parse(String s) {
    ParseContext ctx = new ParseContext();
    List<IToken> list = tokenize(s, ctx);
    Statement stm = parseStatement(list, ctx);
    //sometimes sql is wrapped into brackets
    if (stm == null) {
      ctx = new ParseContext();
      list = tokenize(s, ctx);
      BracketExpr be = parseBracketExpr(list, ctx);
      if (be != null) {
        for (IToken t : be.getChildren()) {
          if (t instanceof Statement) {
            stm = (Statement) t;
            break;
          }
        }
      }
    }
    if (list.size() > 0) {
      Unparsed up = new Unparsed();
      up.setText(flatten(list));
      if (stm == null) {
        stm = new Statement();
      }
      stm.addChild(up);
    }
    return stm;
  }

  private Statement parseStatement(List<IToken> list, ParseContext ctx) {
    //SingleStatement (UnionToken SingleStatement)*
    ParseStep lock = ctx.checkAndAdd("Statement", list);
    if (lock == null) {
      return null;
    }
    try {
      IToken ss = parseSingleStatement(list, ctx);
      if (ss == null) {
        return null;
      }
      Statement s = new Statement();
      s.addChild(ss);
      UnionToken u;
      IToken nexts = null;
      while ((u = removeToken(list, UnionToken.class)) != null && (nexts = parseSingleStatement(list, ctx)) != null) {
        s.addChild(u);
        s.addChild(nexts);
      }
      //restore incomplete
      if (u != null && nexts == null) {
        list.add(0, u);
      }
      return s;
    }
    finally {
      ctx.remove(lock);
    }
  }

  /**
   * return {@link SingleStatement} or {@link BracketExpr}
   */
  private IToken parseSingleStatement(List<IToken> list, ParseContext ctx) {
    ParseStep lock = ctx.checkAndAdd("SingleStatement", list);
    if (lock == null) {
      return null;
    }
    try {
      //brackets
      ArrayList<IToken> backup = new ArrayList<IToken>(list);
      BracketExpr be = parseBracketExpr(list, ctx);
      if (be != null) {
        BracketExpr tmp = be;
        while (tmp != null) {
          IToken ch = tmp.getChildren().get(1);//open, token, close
          if (ch instanceof BracketExpr) {
            tmp = (BracketExpr) ch;
          }
          else if (ch instanceof Statement) {
            return be;
          }
          else if (ch instanceof SingleStatement) {
            return be;
          }
          else {
            tmp = null;
          }
        }
        //restore
        list.clear();
        list.addAll(backup);
        return null;
      }
      //
      //Part+
      Part p;
      SingleStatement ss = new SingleStatement();
      if ((p = parsePart(list, ctx, true)) != null) {
        //ok
        ss.addChild(p);
      }
      else {
        return null;
      }
      while ((p = parsePart(list, ctx, false)) != null) {
        ss.addChild(p);
      }
      if (ss.getChildren().size() == 0) {
        return null;
      }
      return ss;
    }
    finally {
      ctx.remove(lock);
    }
  }

  private Part parsePart(List<IToken> list, ParseContext ctx, boolean rootPart) {
    //PartToken ListExpr
    ParseStep lock = ctx.checkAndAdd("Part", list);
    if (lock == null) {
      return null;
    }
    try {
      PartToken pt;
      ListExpr le;
      if ((pt = removeToken(list, PartToken.class)) != null && (!rootPart || isRootPartToken(pt))) {
        //ok
        le = parseListExpr(list, ctx);
      }
      else {
        //restore incomplete
        if (pt != null) {
          list.add(0, pt);
        }
        return null;
      }
      Part p = new Part();
      p.addChild(pt);
      if (le != null) {
        p.addChild(le);
      }
      return p;
    }
    finally {
      ctx.remove(lock);
    }
  }

  private boolean isRootPartToken(PartToken pt) {
    String s = pt.getText();
    if ("SELECT".equals(s)) {
      return true;
    }
    if ("INSERT INTO".equals(s)) {
      return true;
    }
    if ("INSERT".equals(s)) {
      return true;
    }
    if ("UPDATE".equals(s)) {
      return true;
    }
    if ("DELETE FROM".equals(s)) {
      return true;
    }
    if ("DELETE".equals(s)) {
      return true;
    }
    if ("CASE".equals(s)) {
      return true;
    }
    if ("WITH".equals(s)) {
      return true;
    }
    return false;
  }

  private ListExpr parseListExpr(List<IToken> list, ParseContext ctx) {
    //OrExpr (ListSeparator OrExpr)*
    ParseStep lock = ctx.checkAndAdd("ListExpr", list);
    if (lock == null) {
      return null;
    }
    try {
      OrExpr oe = parseOrExpr(list, ctx);
      if (oe == null) {
        return null;
      }
      ListExpr le = new ListExpr();
      le.addChild(oe);
      ListSeparator ls = null;
      while ((ls = removeToken(list, ListSeparator.class)) != null && (oe = parseOrExpr(list, ctx)) != null) {
        le.addChild(ls);
        le.addChild(oe);
      }
      //restore incomplete
      if (ls != null && oe == null) {
        list.add(0, ls);
      }
      return le;
    }
    finally {
      ctx.remove(lock);
    }
  }

  private OrExpr parseOrExpr(List<IToken> list, ParseContext ctx) {
    //AndExpr (BinaryOp['OR'] AndExpr)*
    ParseStep lock = ctx.checkAndAdd("OrExpr", list);
    if (lock == null) {
      return null;
    }
    try {
      AndExpr ae = parseAndExpr(list, ctx);
      if (ae == null) {
        return null;
      }
      OrExpr oe = new OrExpr();
      oe.addChild(ae);
      OrOp oo;
      while ((oo = removeToken(list, OrOp.class)) != null && (ae = parseAndExpr(list, ctx)) != null) {
        oe.addChild(oo);
        oe.addChild(ae);
      }
      //remaining?
      if (oo != null) {
        oo.addComment(new Comment("/*syntax warning*/"));
        oe.addChild(oo);
      }
      return oe;
    }
    finally {
      ctx.remove(lock);
    }
  }

  private AndExpr parseAndExpr(List<IToken> list, ParseContext ctx) {
    //MathExpr (BinaryOp['AND'] MathExpr)*
    ParseStep lock = ctx.checkAndAdd("AndExpr", list);
    if (lock == null) {
      return null;
    }
    try {
      IToken me = parseMathExpr(list, ctx);
      if (me == null) {
        return null;
      }
      AndExpr ae = new AndExpr();
      ae.addChild(me);
      AndOp ao;
      while ((ao = removeToken(list, AndOp.class)) != null && (me = parseMathExpr(list, ctx)) != null) {
        ae.addChild(ao);
        ae.addChild(me);
      }
      //remaining?
      if (ao != null) {
        ao.addComment(new Comment("/*syntax warning*/"));
        ae.addChild(ao);
      }
      return ae;
    }
    finally {
      ctx.remove(lock);
    }
  }

  private MathExpr parseMathExpr(List<IToken> list, ParseContext ctx) {
    //_simpleExpr (BinaryOp _simpleExpr)*
    ParseStep lock = ctx.checkAndAdd("MathExpr", list);
    if (lock == null) {
      return null;
    }
    try {
      IToken se = parseSimpleExpr(list, ctx);
      if (se == null) {
        return null;
      }
      MathExpr me = new MathExpr();
      me.addChild(se);
      MathOp mo;
      while ((mo = removeToken(list, MathOp.class)) != null && (se = parseSimpleExpr(list, ctx)) != null) {
        me.addChild(mo);
        me.addChild(se);
      }
      //restore incomplete
      if (mo != null && se == null) {
        list.add(0, mo);
      }
      return me;
    }
    finally {
      ctx.remove(lock);
    }
  }

  private IToken parseSimpleExpr(List<IToken> list, ParseContext ctx) {
    //UnaryPrefixExpr | MinusExpr | Atom
    ParseStep lock = ctx.checkAndAdd("SimpleExpr", list);
    if (lock == null) {
      return null;
    }
    try {
      IToken t;
      if ((t = parseUnaryPrefixExpr(list, ctx)) != null) {
        return t;
      }
      if ((t = parseMinusExpr(list, ctx)) != null) {
        return t;
      }
      if ((t = parseAtom(list, ctx)) != null) {
        return t;
      }
      return null;
    }
    finally {
      ctx.remove(lock);
    }
  }

  private UnaryPrefixExpr parseUnaryPrefixExpr(List<IToken> list, ParseContext ctx) {
    //UnaryPrefix Atom
    ParseStep lock = ctx.checkAndAdd("UnaryPrefixExpr", list);
    if (lock == null) {
      return null;
    }
    try {
      UnaryPrefix up = null;
      IToken a = null;
      if ((up = removeToken(list, UnaryPrefix.class)) != null && (a = parseAtom(list, ctx)) != null) {
        //ok
      }
      else {
        //restore incomplete
        if (up != null && a == null) {
          list.add(0, up);
        }
        return null;
      }
      UnaryPrefixExpr e = new UnaryPrefixExpr();
      e.addChild(up);
      e.addChild(a);
      return e;
    }
    finally {
      ctx.remove(lock);
    }
  }

  private MinusExpr parseMinusExpr(List<IToken> list, ParseContext ctx) {
    //BinaryOp['-'] Atom
    ParseStep lock = ctx.checkAndAdd("MinusExpr", list);
    if (lock == null) {
      return null;
    }
    try {
      MathOp mo = null;
      IToken a = null;
      if ((mo = removeToken(list, MathOp.class, "-")) != null && (a = parseAtom(list, ctx)) != null) {
        //ok
      }
      else {
        //restore incomplete
        if (mo != null && a == null) {
          list.add(0, mo);
        }
        return null;
      }
      MinusExpr e = new MinusExpr();
      e.addChild(mo);
      e.addChild(a);
      return e;
    }
    finally {
      ctx.remove(lock);
    }
  }

  private FunExpr parseFunExpr(List<IToken> list, ParseContext ctx) {
    //Name BracketExpr
    ParseStep lock = ctx.checkAndAdd("FunExpr", list);
    if (lock == null) {
      return null;
    }
    try {
      Name nm = null;
      BracketExpr be = null;
      if ((nm = removeToken(list, Name.class)) != null && (be = parseBracketExpr(list, ctx)) != null) {
        //ok
      }
      else {
        //restore incomplete
        if (nm != null && be == null) {
          list.add(0, nm);
        }
        return null;
      }
      FunExpr e = new FunExpr();
      e.addChild(nm);
      e.addChild(be);
      return e;
    }
    finally {
      ctx.remove(lock);
    }
  }

  private BracketExpr parseBracketExpr(List<IToken> list, ParseContext ctx) {
    //BracketExpr = OpenBracketToken (Statement | ListExpr) CloseBracketToken
    ParseStep lock = ctx.checkAndAdd("BracketExpr", list);
    if (lock == null) {
      return null;
    }
    try {
      IToken open = null;
      IToken t = null;
      IToken close = null;
      ArrayList<IToken> backup = new ArrayList<IToken>(list);
      if ((open = removeToken(list, OpenBracketToken.class)) != null && ((t = parseStatement(list, ctx)) != null || (t = parseListExpr(list, ctx)) != null || t == null) && (close = removeToken(list, CloseBracketToken.class)) != null) {
        //ok
      }
      else {
        //restore incomplete
        list.clear();
        list.addAll(backup);
        return null;
      }
      BracketExpr e = new BracketExpr();
      e.addChild(open);
      if (t != null) {
        e.addChild(t);
      }
      e.addChild(close);
      return e;
    }
    finally {
      ctx.remove(lock);
    }
  }

  private Atom parseAtom(List<IToken> list, ParseContext ctx) {
    //(BracketExpr Outer | Statement | OrExpr | FunExpr | Name | Text | BinaryOp['*']) (OuterJoinToken)? (Name["AS"])? (Name[alias])?
    ParseStep lock = ctx.checkAndAdd("Atom", list);
    if (lock == null) {
      return null;
    }
    try {
      IToken t = null;
      if ((t = parseBracketExpr(list, ctx)) != null) {
        //ok
      }
      else if ((t = parseStatement(list, ctx)) != null) {
        //ok
      }
      else if ((t = parseOrExpr(list, ctx)) != null) {
        //ok
      }
      else if ((t = parseFunExpr(list, ctx)) != null) {
        //ok
      }
      else if ((t = removeToken(list, Name.class)) != null) {
        //ok
      }
      else if ((t = removeToken(list, Text.class)) != null) {
        //ok
      }
      else if ((t = removeToken(list, MathOp.class, "*")) != null) {
        //ok
      }
      else {
        return null;
      }
      //found a match
      Atom a = new Atom();
      a.addChild(t);
      if ((t = removeToken(list, OuterJoinToken.class)) != null) {
        a.addChild(t);
      }
      if ((t = removeToken(list, Name.class)) != null) {
        a.addChild(t);
      }
      return a;
    }
    finally {
      ctx.remove(lock);
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends IToken> T removeToken(List<IToken> list, Class<T> tokenType) {
    if (0 < list.size() && tokenType.isAssignableFrom(list.get(0).getClass())) {
      return (T) list.remove(0);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private <T extends IToken> T removeToken(List<IToken> list, Class<T> tokenType, String text) {
    if (0 < list.size() && tokenType.isAssignableFrom(list.get(0).getClass()) && text.equals(list.get(0).getText())) {
      return (T) list.remove(0);
    }
    return null;
  }

  /**
   * Tokenize a string into Whitespace (containing Comment and HintComment), Text and Token items. There are no Raw
   * objects left.
   *
   * @throws ParseException
   */
  private List<IToken> tokenize(String s, ParseContext ctx) {
    s = encodeBinds(s, ctx);
    s = s.replaceAll("[\\n\\r]+", " ");
    List<IToken> list = new ArrayList<IToken>();
    list.add(new Raw(s));
    //
    list = tokenizeRaw(list, COMMENT_PAT, Comment.class, true);
    list = tokenizeRaw(list, APOS_PAT, Text.class, true);
    list = tokenizeRaw(list, APOS_PAT, Text.class, false);
    list = tokenizeRaw(list, QUOT_PAT, Name.class, true);
    list = tokenizeRaw(list, QUOT_PAT, Name.class, false);
    //replace all remaining whitespace by a single space and convert to upper case
    for (IToken item : list) {
      if (item instanceof Raw) {
        String text = item.getText();
        text = text.replaceAll("[\\s]+", " ");
        text = text.toUpperCase();
        item.setText(text);
      }
    }
    list = tokenizeRaw(list, UNION_PAT, UnionToken.class, false);
    list = tokenizeRaw(list, PART_PAT, PartToken.class, false);
    list = tokenizeRaw(list, OUTER_JOIN_PAT, OuterJoinToken.class, false);
    list = tokenizeRaw(list, OR_OP_PAT, OrOp.class, false);
    list = tokenizeRaw(list, AND_OP_PAT, AndOp.class, false);
    list = tokenizeRaw(list, MATH_OP_PAT1, MathOp.class, false);
    list = tokenizeRaw(list, MATH_OP_PAT2, MathOp.class, false);
    list = tokenizeRaw(list, UNARY_PREFIX_PAT, UnaryPrefix.class, false);
    list = tokenizeRaw(list, NAME_PAT, Name.class, false);
    list = tokenizeRaw(list, OPEN_BRACKET_PAT, OpenBracketToken.class, false);
    list = tokenizeRaw(list, CLOSE_BRACKET_PAT, CloseBracketToken.class, false);
    list = tokenizeRaw(list, LIST_SEPARATOR_PAT, ListSeparator.class, false);
    //eliminate all empty Raw
    for (Iterator<IToken> it = list.iterator(); it.hasNext();) {
      IToken item = it.next();
      if (item instanceof Raw) {
        if (!StringUtility.hasText(item.getText())) {
          it.remove();
        }
      }
    }
    //check the rest, no more Raw, convert to comment with warning
    for (int i = 0; i < list.size(); i++) {
      IToken tok = list.get(i);
      if (tok instanceof Raw) {
        list.remove(i);
        Comment c = new Comment();
        c.setText("/*XXX unexpected token: " + tok.getText() + "*/");
        list.add(i, c);
      }
    }
    //associate comments with the successor token
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i) instanceof Comment) {
        //find successor
        IToken succ = null;
        for (int k = i + 1; k < list.size(); k++) {
          if (!(list.get(k) instanceof Comment)) {
            succ = list.get(k);
            break;
          }
        }
        if (succ == null) {
          for (int k = 0; k < list.size(); k++) {
            if (!(list.get(k) instanceof Comment)) {
              succ = list.get(k);
              break;
            }
          }
        }
        if (succ != null) {
          succ.addComment((Comment) list.get(i));
        }
      }
    }
    for (Iterator<IToken> it = list.iterator(); it.hasNext();) {
      IToken item = it.next();
      if (item instanceof Comment) {
        it.remove();
      }
    }
    decodeBinds(list, ctx);
    return list;
  }

  private List<IToken> tokenizeRaw(List<IToken> list, Pattern p, Class<? extends IToken> tokenType, boolean transcodeDelimiters) {
    ArrayList<IToken> newList = new ArrayList<IToken>(list.size());
    for (IToken item : list) {
      if (item instanceof Raw) {
        String s = ((Raw) item).getText();
        if (transcodeDelimiters) {
          s = encodeDelimiters(s);
        }
        //extend s to start and end with an empty text (simpler regex can be used then)
        s = " " + s + " ";
        Matcher m = p.matcher(s);
        int lastEnd = 0;
        while (lastEnd < s.length() && m.find(lastEnd)) {
          String r = s.substring(lastEnd, m.start(1));
          if (transcodeDelimiters) {
            r = decodeDelimiters(r);
          }
          newList.add(new Raw(r.trim()));
          //
          r = m.group(1);
          if (transcodeDelimiters) {
            r = decodeDelimiters(r);
          }
          IToken t;
          try {
            t = tokenType.newInstance();
          }
          catch (Exception e) {
            throw new RuntimeException(e);
          }
          t.setText(r);
          newList.add(t);
          //
          lastEnd = m.end(1);
        }
        //remaining part
        if (lastEnd < s.length()) {
          String r = s.substring(lastEnd);
          if (transcodeDelimiters) {
            r = decodeDelimiters(r);
          }
          newList.add(new Raw(r.trim()));
        }
      }
      else {
        newList.add(item);
      }
    }
    return newList;
  }

  private String encodeBinds(String s, ParseContext ctx) {
    BindModel m = new BindParser(s).parse();
    for (org.eclipse.scout.rt.server.jdbc.parsers.token.IToken bindToken : m.getAllTokens()) {
      if (bindToken instanceof TextToken) {
        continue;
      }
      String code = "___BIND" + ctx.getBinds().size();
      String name = bindToken.getParsedToken();
      bindToken.setReplaceToken(code);
      ctx.getBinds().put(code, name);
    }
    return m.getFilteredStatement();
  }

  private void decodeBinds(List<IToken> list, ParseContext ctx) {
    for (IToken t : list) {
      if (t instanceof Name) {
        String s = ctx.getBinds().get(t.getText());
        if (s != null) {
          t.setText(s);
        }
      }
    }
  }

  /**
   * @return text containing only the white space delimters and ' " { }
   */
  private String encodeDelimiters(String s) {
    //backup chars and escapes
    s = s.replace("$", "$0");
    s = s.replace("{", "$1");
    s = s.replace("}", "$2");
    s = s.replace("''", "$3");
    s = s.replace("\"\"", "$4");
    //define single-char delimiters for comments
    s = s.replace("/*", "{");
    s = s.replace("*/", "}");
    return s;
  }

  private String decodeDelimiters(String s) {
    s = s.replace("}", "*/");
    s = s.replace("{", "/*");
    s = s.replace("$4", "\"\"");
    s = s.replace("$3", "''");
    s = s.replace("$2", "}");
    s = s.replace("$1", "{");
    s = s.replace("$0", "$");
    return s;
  }

  private String flatten(List<IToken> list) {
    StringBuffer buf = new StringBuffer();
    for (IToken item : list) {
      if (buf.length() > 0) {
        buf.append(" ");
      }
      buf.append(item);
    }
    return buf.toString();
  }
}
