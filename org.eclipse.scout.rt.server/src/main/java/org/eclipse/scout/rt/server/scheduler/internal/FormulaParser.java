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
package org.eclipse.scout.rt.server.scheduler.internal;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.server.scheduler.internal.node.ArgRef;
import org.eclipse.scout.rt.server.scheduler.internal.node.BooleanAtom;
import org.eclipse.scout.rt.server.scheduler.internal.node.FormulaRoot;
import org.eclipse.scout.rt.server.scheduler.internal.node.INode;
import org.eclipse.scout.rt.server.scheduler.internal.node.IntegerAtom;
import org.eclipse.scout.rt.server.scheduler.internal.node.NotToken;
import org.eclipse.scout.rt.server.scheduler.internal.node.NullAtom;
import org.eclipse.scout.rt.server.scheduler.internal.node.Op;
import org.eclipse.scout.rt.server.scheduler.internal.node.SignalRef;
import org.eclipse.scout.rt.server.scheduler.internal.node.StringAtom;
import org.eclipse.scout.rt.server.scheduler.internal.node.WrappedToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for dynamic formulas Syntax (EBNF): token=token0 = token1 (S? op1 S? token1)* . token1 = token2 (S? op2 S?
 * token2)* . token2 = token3 (S? op3 S? token3)* . token3 = token4 (S? op4 S? token4)* . token4 = token5 (S? op5 S?
 * token5)* . token5 = atom . op1 = '&&' | '||' . op2 = '==' | '<=' | '>=' | '<>' | '!=' | '<' | '>'. op3 = '+' | '-' .
 * op4 = '*' | '/' | '%' . op5 = '^' | '.' | '&' | '|' | '<<' | '>>'. atom = wrapped-token | not-token | null-atom |
 * int-atom | boolean-atom | string-atom | signal-ref | arg-ref . wrapped-token = '(' S? token S? ')' . not-token = '!'
 * token . null-atom = 'null' . int-atom = [+-]? [0-9]* [.]? [0-9]* . boolean-atom = 'true' | 'false' . string-atom =
 * `'` [^delim]* `'` | `"` [^delim]* `"` . NOTE: delim is the delimiting character signal-ref = 'second' | 'minute' |
 * 'hour' | 'day' | 'week' | 'month' | 'year' | 'dayOfWeek' | 'dayOfMonthReverse' | 'dayOfYear' | 'secondOfDay' .
 * arg-ref = 'arg' [0-9]+ . S = ([ \n\t\r] | S_comment)+ .
 */

public class FormulaParser {
  private static final Logger LOG = LoggerFactory.getLogger(FormulaParser.class);
  private static final String S_MAP = " \n\t\r";

  private String str;
  private ParsePosition pos;

  public FormulaParser() {
  }

  public FormulaRoot parse(String formula) throws ParseException {
    str = formula;
    if (str != null) {
      str = str.trim();
    }
    FormulaRoot root;
    if (str == null || str.length() == 0) {
      root = new FormulaRoot(new NullAtom());
    }
    else {
      // parse not-empty string
      pos = new ParsePosition(0);
      try {
        INode t = parseToken(0);
        if (t == null) {
          throw new ParseException("no node parsed", pos.getIndex());
        }
        if (pos.getIndex() < str.length()) {
          throw new ParseException("formula not fully parsed (index " + pos.getIndex() + " of " + str.length() + ") : " + formula, pos.getIndex());
        }
        root = new FormulaRoot(t);
      }
      catch (ParseException ce) {
        throw ce;
      }
      catch (Exception other) {
        LOG.error("Could not parse formula", other);
        throw new ParseException("unexpected exception: " + other, pos.getIndex());
      }
    }
    return root;
  }

  private INode parseToken(int level) throws ParseException {
    if (level >= 5) {
      return parseAtom();
    }
    INode leftToken;
    if ((leftToken = parseToken(level + 1)) != null) {
      int save = pos.getIndex();
      String op;
      INode rightToken;
      while (parseWhitespace(0) && (op = parseOp(level + 1)) != null && parseWhitespace(0) && (rightToken = parseToken(level + 1)) != null) {
        if ("&&".equals(op)) {
          leftToken = new Op.And(leftToken, rightToken);
        }
        else if ("||".equals(op)) {
          leftToken = new Op.Or(leftToken, rightToken);// OR or CONCAT
        }
        else if ("==".equals(op)) {
          leftToken = new Op.Equal(leftToken, rightToken);
        }
        else if ("<=".equals(op)) {
          leftToken = new Op.LessThanOrEqual(leftToken, rightToken);
        }
        else if (">=".equals(op)) {
          leftToken = new Op.GreaterThanOrEqual(leftToken, rightToken);
        }
        else if ("<>".equals(op) || "!=".equals(op)) {
          leftToken = new Op.NotEqual(leftToken, rightToken);
        }
        else if ("<".equals(op)) {
          leftToken = new Op.LessThan(leftToken, rightToken);
        }
        else if (">".equals(op)) {
          leftToken = new Op.GreaterThan(leftToken, rightToken);
        }
        else if ("+".equals(op)) {
          leftToken = new Op.Add(leftToken, rightToken);
        }
        else if ("-".equals(op)) {
          leftToken = new Op.Sub(leftToken, rightToken);
        }
        else if ("*".equals(op)) {
          leftToken = new Op.Mul(leftToken, rightToken);
        }
        else if ("/".equals(op)) {
          leftToken = new Op.Div(leftToken, rightToken);
        }
        else if ("%".equals(op)) {
          leftToken = new Op.Mod(leftToken, rightToken);
        }
        else if ("^".equals(op)) {// Build 177
          leftToken = new Op.BitXor(leftToken, rightToken);
        }
        else if ("|".equals(op)) {// Build 177
          leftToken = new Op.BitOr(leftToken, rightToken);
        }
        else if ("&".equals(op)) {// Build 177
          leftToken = new Op.BitAnd(leftToken, rightToken);
        }
        else if ("<<".equals(op)) {// Build 177
          leftToken = new Op.BitShiftLeft(leftToken, rightToken);
        }
        else if (">>".equals(op)) {// Build 177
          leftToken = new Op.BitShiftRight(leftToken, rightToken);
        }
        else if (">>>".equals(op)) {// Build 206
          leftToken = new Op.BitShiftRightZeroExtending(leftToken, rightToken);
        }
        else {
          throw new ParseException("unexpected operation " + op, pos.getIndex());
        }
        //
        save = pos.getIndex();
      }// end while
      pos.setIndex(save);
      return leftToken;
    }// end if parse token of next level
     // failed
    return null;
  }

  private String parseOp(int level) throws ParseException {
    int index = pos.getIndex();
    if (matches("&&") || matches("||") || matches("==") || matches("<=") || matches(">=") || matches("<>") || matches("!=") || matches("<<") || matches(">>") || matches("<") || matches(">") || matches("+") || matches("-") || matches("*")
        || matches("/") || matches("%") || matches("^") || matches("&") || matches("|")) {
      // ok,found op, valid in current level ?
      String op = str.substring(index, pos.getIndex());
      if (level == 1) {
        if ("&&".equals(op)) {
          return op;
        }
        else if ("||".equals(op)) {
          return op;
        }
        else {
          return null;
        }
      }
      else if (level == 2) {
        if ("==".equals(op)) {
          return op;
        }
        else if ("<=".equals(op)) {
          return op;
        }
        else if (">=".equals(op)) {
          return op;
        }
        else if ("<>".equals(op)) {
          return op;
        }
        else if ("!=".equals(op)) {
          return op;
        }
        else if ("<".equals(op)) {
          return op;
        }
        else if (">".equals(op)) {
          return op;
        }
        else {
          return null;
        }
      }
      else if (level == 3) {
        if ("+".equals(op)) {
          return op;
        }
        else if ("-".equals(op)) {
          return op;
        }
        else {
          return null;
        }
      }
      else if (level == 4) {
        if ("*".equals(op)) {
          return op;
        }
        else if ("/".equals(op)) {
          return op;
        }
        else if ("%".equals(op)) {
          return op;
        }
        else {
          return null;
        }
      }
      else if (level == 5) {
        if ("^".equals(op)) {
          return op;
        }
        else if ("&".equals(op)) {
          return op;
        }
        else if ("|".equals(op)) {
          return op;
        }
        else if ("<<".equals(op)) {
          return op;
        }
        else if (">>".equals(op)) {
          return op;
        }
        return null;
      }
      else {
        throw new ParseException("invalid op level " + level, pos.getIndex());
      }
    }
    else {
      return null;
    }
  }

  private INode parseAtom() throws ParseException {
    INode cmd;
    if ((cmd = parseWrappedToken()) != null) {
      return cmd;
    }
    if ((cmd = parseNotToken()) != null) {
      return cmd;
    }
    if ((cmd = parseNullAtom()) != null) {
      return cmd;
    }
    if ((cmd = parseIntegerAtom()) != null) {
      return cmd;
    }
    if ((cmd = parseBooleanAtom()) != null) {
      return cmd;
    }
    if ((cmd = parseStringAtom()) != null) {
      return cmd;
    }
    if ((cmd = parseSignalRef()) != null) {
      return cmd;
    }
    if ((cmd = parseArgRef()) != null) {
      return cmd;
    }
    else {
      // failed
      return null;
    }
  }

  private WrappedToken parseWrappedToken() throws ParseException {
    int index = pos.getIndex();
    INode node = null;
    if (matches("(") && parseWhitespace(0) && (node = parseToken(0)) != null && parseWhitespace(0) && matches(")")) {
      return new WrappedToken(node);
    }
    pos.setIndex(index);
    return null;
  }

  private NotToken parseNotToken() throws ParseException {
    int index = pos.getIndex();
    INode node = null;
    if (matches("!") && parseWhitespace(0) && (node = parseToken(0)) != null) {
      return new NotToken(node);
    }
    pos.setIndex(index);
    return null;
  }

  private NullAtom parseNullAtom() {
    int index = pos.getIndex();
    String name = parseName();
    if (name != null && name.equalsIgnoreCase("null")) {
      return new NullAtom();
    }
    pos.setIndex(index);
    return null;
  }

  private IntegerAtom parseIntegerAtom() throws ParseException {
    int index = pos.getIndex();
    int len = str.length();
    int i = index;
    if (matches("+") || matches("-")) {
      i++;
    }
    while (i < len && Character.isDigit(str.charAt(i))) {
      i++;
    }
    if (i < len && str.charAt(i) == '.') {
      i++;
      while (i < len && Character.isDigit(str.charAt(i))) {
        i++;
      }
    }
    if (i > index) {
      pos.setIndex(i);
      String s = str.substring(index, pos.getIndex());
      s = s.trim();
      if (s.startsWith("+")) {
        s = s.substring(1);
      }
      if (s.indexOf('.') >= 0) {
        throw new ParseException("only supporting integer numbers: " + s, index);
      }
      else {
        return new IntegerAtom(new Integer(s));
      }
    }
    return null;
  }

  private BooleanAtom parseBooleanAtom() {
    int index = pos.getIndex();
    String name = parseName();
    if (name != null) {
      if (name.equalsIgnoreCase("true")) {
        return new BooleanAtom(true);
      }
      else if (name.equalsIgnoreCase("false")) {
        return new BooleanAtom(false);
      }
    }
    pos.setIndex(index);
    return null;
  }

  private StringAtom parseStringAtom() throws ParseException {
    int index = pos.getIndex();
    if (matches("'") || matches("\"")) {
      char delimChar = str.charAt(index);
      StringBuffer text = new StringBuffer();
      char ch;
      while ((ch = parseChar(delimChar)) != 0x00) {
        text.append(ch);
      }
      if (matches("" + delimChar)) {
        return new StringAtom(text.toString());
      }
      else {
        throw new ParseException("unclosed string; expected delimChar " + delimChar, pos.getIndex());
      }
    }
    pos.setIndex(index);
    return null;
  }

  private SignalRef parseSignalRef() throws ParseException {
    int index = pos.getIndex();
    String name = parseName();
    if (name != null) {
      if ("second".equalsIgnoreCase(name)) {
        return new SignalRef(SignalRef.SECOND);
      }
      else if ("minute".equalsIgnoreCase(name)) {
        return new SignalRef(SignalRef.MINUTE);
      }
      else if ("hour".equalsIgnoreCase(name)) {
        return new SignalRef(SignalRef.HOUR);
      }
      else if ("day".equalsIgnoreCase(name)) {
        return new SignalRef(SignalRef.DAY);
      }
      else if ("week".equalsIgnoreCase(name)) {
        return new SignalRef(SignalRef.WEEK);
      }
      else if ("month".equalsIgnoreCase(name)) {
        return new SignalRef(SignalRef.MONTH);
      }
      else if ("year".equalsIgnoreCase(name)) {
        return new SignalRef(SignalRef.YEAR);
      }
      else if ("dayOfWeek".equalsIgnoreCase(name)) {
        return new SignalRef(SignalRef.DAY_OF_WEEK);
      }
      else if ("dayOfMonthReverse".equalsIgnoreCase(name)) {
        return new SignalRef(SignalRef.DAY_OF_MONTH_REVERSE);
      }
      else if ("dayOfYear".equalsIgnoreCase(name)) {
        return new SignalRef(SignalRef.DAY_OF_YEAR);
      }
      else if ("secondOfDay".equalsIgnoreCase(name)) {
        return new SignalRef(SignalRef.SECOND_OF_DAY);
      }
    }
    pos.setIndex(index);
    return null;
  }

  private ArgRef parseArgRef() throws ParseException {
    int index = pos.getIndex();
    String name = parseName();
    if (name != null) {
      name = name.toLowerCase();
      Matcher mat = Pattern.compile("arg([0-9]+)").matcher(name.toLowerCase());
      if (mat.matches()) {
        return new ArgRef(Integer.parseInt(mat.group(1)));
      }
    }
    pos.setIndex(index);
    return null;
  }

  private String parseName() {
    int index = pos.getIndex();
    int len = str.length();
    int i = index;
    if (i < len && Character.isJavaIdentifierStart(str.charAt(i))) {
      i++;
      while (i < len && (Character.isJavaIdentifierPart(str.charAt(i)))) {
        i++;
      }
    }
    if (i > index) {
      pos.setIndex(i);
      return str.substring(index, pos.getIndex());
    }
    else {
      pos.setIndex(index);
      return null;
    }
  }

  private char parseChar(char quoteChar) {
    try {
      String notCharacterMap = "\\";
      if (quoteChar != 0x00) {
        notCharacterMap += quoteChar;
      }
      int index = pos.getIndex();
      int len = str.length();
      if (index >= len) {
        return 0x00;
      }
      char ch = str.charAt(index);
      if (index < len && notCharacterMap.indexOf(ch) < 0) {// regular character
        pos.setIndex(index + 1);
        return ch;
      }
      else if (index + 1 < len && ch == '\\') { // escaped character
        pos.setIndex(index + 2);
        ch = str.charAt(index + 1);
        // unesc some characters
        switch (ch) {
          case 'b': {
            ch = '\b';
            break;
          }
          case 't': {
            ch = '\t';
            break;
          }
          case 'n': {
            ch = '\n';
            break;
          }
          case 'f': {
            ch = '\f';
            break;
          }
          case 'r': {
            ch = '\r';
            break;
          }
        }
        return ch;
      }
      else {
        return 0x00;
      }
    }
    finally {

    }
  }

  private boolean matches(String m) {
    int index = pos.getIndex();
    int len = m.length();
    if (index + len <= str.length() && m.equalsIgnoreCase(str.substring(index, index + len))) {
      pos.setIndex(index + len);
      return true;
    }
    else {
      return false;
    }
  }

  private boolean parseWhitespace(int numRequired) throws ParseException {
    int index = pos.getIndex();
    int len = str.length();
    int i = index;
    // white
    while (i < len && S_MAP.indexOf(str.charAt(i)) >= 0) {
      i++;
    }
    // comment?
    while (i + 1 < len && str.charAt(i) == '/' && str.charAt(i + 1) == '*') {
      i = i + 2;
      int end = str.indexOf("*/", i);
      if (end < 0) {
        throw new ParseException("missing comment end: */", i);
      }
      String text = str.substring(i, end).trim();
      if (text.length() > 0) {
        // found comment
      }
      i = end + 2;
      // white
      while (i < len && S_MAP.indexOf(str.charAt(i)) >= 0) {
        i++;
      }
    }
    if (i - index >= numRequired) {
      pos.setIndex(i);
      return true;
    }
    else {
      return false;
    }
  }

}
