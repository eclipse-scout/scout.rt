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
package org.eclipse.scout.rt.server.jdbc.parsers;

import java.text.ParsePosition;
import java.util.ArrayList;

import org.eclipse.scout.rt.server.jdbc.parsers.token.DatabaseSpecificToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.FunctionInputToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.TextToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for bind variables in arbitrary sql staments The bind is (possibly) parsed together with its left operator
 * (for better =ANY vs. IN handlings)
 * <p>
 * Bind Possibilities are
 * <ul>
 * <li>:abc</li>
 * <li>:[OUT]abc</li>
 * <li>#abc#</li>
 * <li>#[OUT]abc#</li>
 * </ul>
 * <p>
 * Syntax (EBNF):
 *
 * <pre>
 * statement      = S? token-list .
 * token-list     = token (S? token)* .
 * token          = text | extended-bind | char .
 * extended-bind  = (attribute S?)? (op S?)? bind .
 * attribute      = name . //but not op
 * op             = '=' | '<>' | '!=' | '<=' | '>=' | '<' | '>' | 'IN' | 'NOT' S 'IN' .
 * bind           = escaped-value-bind | plain-sql-bind | jdbc-bind .
 * escaped-value-bind='#' name '#' .
 * jdbc-bind       = ':' name .
 * function-bind  = ( '::' | '##' ) name S? '(' S? function-arg-list? S? ')' .
 * function-arg-list= function-arg (S? ',' S? function-arg)* .
 * function-arg   = text | name | number.
 * database-specific-token = '$$' name .
 * name           = name-char+ .
 * number         = name-char+ .
 * text           = `'` text-char* `'` .
 * name-char      = [a-zA-Z0-9_.{}[]] .
 * number-char    = [0-9] .
 * text-char      = [^'] | `''` .
 * char           = [.] .
 * S              = ([ \n\t\r])+  .
 * </pre>
 * </p>
 */
public class BindParser {
  private static final Logger LOG = LoggerFactory.getLogger(BindParser.class);

  private static final String S_MAP;
  private static final String NAME_MAP;

  static {
    S_MAP = " \n\t\r";
    NAME_MAP = "_.0123456789{}[]ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  }

  private String m_str;
  private ParsePosition m_pos;
  //
  private ArrayList<IToken> m_tokenList = new ArrayList<IToken>();
  private int m_lastTokenEndIndex;

  public BindParser(String sqlStatement) {
    m_str = sqlStatement.trim();
  }

  public BindModel parse() {
    m_pos = new ParsePosition(0);
    parseStatement();
    addTextTokenUntil(m_str.length());
    if (m_pos.getIndex() < m_str.length()) {
      LOG.warn("statement not fully parsed (index {}): {}", m_pos.getIndex(), m_str);
    }
    return new BindModel(m_tokenList.toArray(new IToken[m_tokenList.size()]));
  }

  private boolean parseStatement() {
    if (LOG.isTraceEnabled()) {
      trace("parseStatement");
    }
    int index = m_pos.getIndex();
    parseWhitespace(0);
    if (parseTokenList()) {
      return true;
    }
    else {
      m_pos.setIndex(index);
      return false;
    }
  }

  private boolean parseTokenList() {
    if (LOG.isTraceEnabled()) {
      trace("parseTokenList");
    }
    int index = m_pos.getIndex();
    if (parseToken()) {
      index = m_pos.getIndex();
      parseWhitespace(0);
      while (parseToken()) {
        index = m_pos.getIndex();
        parseWhitespace(0);
      }
      m_pos.setIndex(index);
      return true;
    }
    else {
      m_pos.setIndex(index);
      return false;
    }
  }

  private ArrayList<String> parseFunctionArgList() {
    if (LOG.isTraceEnabled()) {
      trace("parseFunctionArgList");
    }
    int index = m_pos.getIndex();
    ArrayList<String> textList = new ArrayList<String>();
    String text;
    if ((text = parseFunctionArg()) != null) {
      textList.add(text);
      index = m_pos.getIndex();
      parseWhitespace(0);
      while (matches(",") && parseWhitespace(0) && (text = parseFunctionArg()) != null) {
        textList.add(text);
        index = m_pos.getIndex();
        parseWhitespace(0);
      }
      m_pos.setIndex(index);
      return textList;
    }
    else {
      m_pos.setIndex(index);
      return textList;
    }
  }

  private boolean parseToken() {
    if (LOG.isTraceEnabled()) {
      trace("parseToken");
    }
    return (parseText() != null || parseExtendedBind() != null || parseChar());
  }

  private String parseText() {
    if (LOG.isTraceEnabled()) {
      trace("parseText");
    }
    int index = m_pos.getIndex();
    if (matches("'")) {
      while (parseTextChar()) {
      }
      if (!matches("'")) {
        LOG.warn("expected ' at position {} of {}", m_pos.getIndex(), m_str);
      }
      String text = m_str.substring(index + 1, m_pos.getIndex()).replaceAll("''", "'");
      return text;
    }
    m_pos.setIndex(index);
    return null;
  }

  private String parseFunctionArg() {
    if (LOG.isTraceEnabled()) {
      trace("parseFunctionArg");
    }
    int index = m_pos.getIndex();
    String arg;
    if ((arg = parseText()) != null || (arg = parseName()) != null || (arg = parseNumber()) != null) {
      return arg;
    }
    m_pos.setIndex(index);
    return null;
  }

  private IToken parseExtendedBind() {
    if (LOG.isTraceEnabled()) {
      trace("parseExtendedBind");
    }
    int index = m_pos.getIndex();
    String attribute = null;
    String op = null;
    IToken token = null;
    if ((attribute = parseAttribute()) != null && parseWhitespace(0) && (op = parseOp()) != null && parseWhitespace(0)) {
    }
    else {
      m_pos.setIndex(index);
      attribute = null;
      op = null;
      if ((op = parseOp()) != null && parseWhitespace(0)) {
      }
      else {
        m_pos.setIndex(index);
      }
    }
    int indexAfterOp = m_pos.getIndex();
    if ((token = parseBind()) != null) {
      if (token instanceof ValueInputToken) {
        addTextTokenUntil(index);
        ((ValueInputToken) token).setParsedAttribute(attribute);
        ((ValueInputToken) token).setParsedOp(op);
      }
      else {
        addTextTokenUntil(indexAfterOp);
      }
      addToken(token, m_pos.getIndex());
      return token;
    }
    else {
      m_pos.setIndex(index);
      return null;
    }
  }

  private String parseAttribute() {
    if (LOG.isTraceEnabled()) {
      trace("parseAttribute");
    }
    int index = m_pos.getIndex();
    if (parseOp() == null) {
      return parseName();
    }
    else {
      m_pos.setIndex(index);
      return null;
    }
  }

  private String parseOp() {
    if (LOG.isTraceEnabled()) {
      trace("parseOp");
    }
    int index = m_pos.getIndex();
    if (matches("=")) {
      return "=";
    }
    m_pos.setIndex(index);
    if (matches("<>")) {
      return "<>";
    }
    m_pos.setIndex(index);
    if (matches("!=")) {
      return "!=";
    }
    m_pos.setIndex(index);
    if (matches("<=")) {
      return "<=";
    }
    m_pos.setIndex(index);
    if (matches(">=")) {
      return ">=";
    }
    m_pos.setIndex(index);
    if (matches("<")) {
      return "<";
    }
    m_pos.setIndex(index);
    if (matches(">")) {
      return ">";
    }
    m_pos.setIndex(index);
    if (matches("IN") && !peekNameChar()) {
      return "IN";
    }
    m_pos.setIndex(index);
    if (matches("NOT") && parseWhitespace(1) && matches("IN") && !peekNameChar()) {
      return "NOT IN";
    }
    m_pos.setIndex(index);
    if (matches("LIKE") && !peekNameChar()) {
      return "LIKE";
    }
    m_pos.setIndex(index);
    if (matches("NOT") && parseWhitespace(1) && matches("LIKE") && !peekNameChar()) {
      return "NOT LIKE";
    }
    m_pos.setIndex(index);
    return null;
  }

  private IToken parseBind() {
    if (LOG.isTraceEnabled()) {
      trace("parseBind");
    }
    IToken token = null;
    int index = m_pos.getIndex();
    if ((token = parsePlainValueBind()) != null
        || (token = parseFunctionBind()) != null
        || (token = parseDatabaseSpecificToken()) != null
        || (token = parseStdBind()) != null) {
      return token;
    }
    else {
      m_pos.setIndex(index);
      return null;
    }
  }

  private IToken parsePlainValueBind() {
    if (LOG.isTraceEnabled()) {
      trace("parsePlainValueBind");
    }
    int index = m_pos.getIndex();
    String name;
    if (matches("#") && (name = parseName()) != null && matches("#")) {
      if (name.startsWith("[OUT]")) {
        return new ValueOutputToken(m_str.substring(index, m_pos.getIndex()), name.substring(5), false);
      }
      else {
        return new ValueInputToken(m_str.substring(index, m_pos.getIndex()), name, true, false);
      }
    }
    m_pos.setIndex(index);
    return null;
  }

  private IToken parseFunctionBind() {
    if (LOG.isTraceEnabled()) {
      trace("parseFunctionBind");
    }
    int index = m_pos.getIndex();
    if (matches("::") || matches("##")) {
      boolean plainValue = "##".equals(m_str.substring(index, index + 2));
      String name;
      ArrayList<String> args;
      if ((name = parseName()) != null && parseWhitespace(0) && matches("(") && parseWhitespace(0) && (args = parseFunctionArgList()).size() > 0 && parseWhitespace(0) && matches(")")) {
        return new FunctionInputToken(m_str.substring(index, m_pos.getIndex()), name, args.toArray(new String[0]), plainValue, false);
      }
    }
    m_pos.setIndex(index);
    return null;
  }

  private IToken parseDatabaseSpecificToken() {
    if (LOG.isTraceEnabled()) {
      trace("parseDatabaseSpecificToken");
    }
    int index = m_pos.getIndex();
    String name;
    if (matches("$$") && (name = parseName()) != null) {
      return new DatabaseSpecificToken(m_str.substring(index, m_pos.getIndex()), name);
    }
    m_pos.setIndex(index);
    return null;
  }

  private IToken parseStdBind() {
    if (LOG.isTraceEnabled()) {
      trace("parseStdBind");
    }
    int index = m_pos.getIndex();
    String name;
    if (matches(":") && (name = parseName()) != null) {
      if (name.startsWith("[OUT]")) {
        return new ValueOutputToken(m_str.substring(index, m_pos.getIndex()), name.substring(5), false);
      }
      else {
        return new ValueInputToken(m_str.substring(index, m_pos.getIndex()), name, false, false);
      }
    }
    m_pos.setIndex(index);
    return null;
  }

  private String parseName() {
    if (LOG.isTraceEnabled()) {
      trace("parseName");
    }
    int index = m_pos.getIndex();
    while (parseNameChar()) {
    }
    if (m_pos.getIndex() > index) {
      return m_str.substring(index, m_pos.getIndex());
    }
    else {
      m_pos.setIndex(index);
      return null;
    }
  }

  private String parseNumber() {
    if (LOG.isTraceEnabled()) {
      trace("parseNumber");
    }
    int index = m_pos.getIndex();
    while (parseNumberChar()) {
    }
    if (m_pos.getIndex() > index) {
      return m_str.substring(index, m_pos.getIndex());
    }
    else {
      m_pos.setIndex(index);
      return null;
    }
  }

  private boolean parseChar() {
    if (LOG.isTraceEnabled()) {
      trace("parseChar");
    }
    int index = m_pos.getIndex();
    if (index < m_str.length()) {
      m_pos.setIndex(index + 1);
      return true;
    }
    return false;
  }

  private boolean matches(String m) {
    int index = m_pos.getIndex();
    int len = m.length();
    if (index + len <= m_str.length() && m.equalsIgnoreCase(m_str.substring(index, index + len))) {
      m_pos.setIndex(index + len);
      return true;
    }
    else {
      return false;
    }
  }

  private boolean parseTextChar() {
    if (LOG.isTraceEnabled()) {
      trace("parseTextChar");
    }
    int index = m_pos.getIndex();
    int len = m_str.length();
    if (index < len && m_str.charAt(index) != '\'') {
      m_pos.setIndex(index + 1);
      return true;
    }
    else if (index + 1 < len && m_str.charAt(index + 1) == '\'') {
      m_pos.setIndex(index + 2);
      return true;
    }
    else {
      return false;
    }
  }

  private boolean parseNumberChar() {
    if (LOG.isTraceEnabled()) {
      trace("parseNumberChar");
    }
    int index = m_pos.getIndex();
    int len = m_str.length();
    if (index < len && Character.isDigit(m_str.charAt(index))) {
      m_pos.setIndex(index + 1);
      return true;
    }
    else {
      return false;
    }
  }

  private boolean parseNameChar() {
    if (LOG.isTraceEnabled()) {
      trace("parseNameChar");
    }
    int index = m_pos.getIndex();
    int len = m_str.length();
    if (index < len && NAME_MAP.indexOf(m_str.charAt(index)) >= 0) {
      m_pos.setIndex(index + 1);
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * @return true if the next char is a name char, without changing the parse position
   */
  private boolean peekNameChar() {
    if (LOG.isTraceEnabled()) {
      trace("parseNameChar");
    }
    int index = m_pos.getIndex();
    int len = m_str.length();
    if (index < len && NAME_MAP.indexOf(m_str.charAt(index)) >= 0) {
      return true;
    }
    else {
      return false;
    }
  }

  private boolean parseWhitespace(int nunRequired) {
    int index = m_pos.getIndex();
    int len = m_str.length();
    int i = index;
    while (i < len && S_MAP.indexOf(m_str.charAt(i)) >= 0) {
      i++;
    }
    if (i - index >= nunRequired) {
      m_pos.setIndex(i);
      return true;
    }
    else {
      return false;
    }
  }

  private void trace(String s) {
    int len = m_str.length();
    int i0 = Math.min(m_pos.getIndex(), len - 1);
    int i1 = Math.min(i0 + 32, len);
    LOG.trace("# {} at:{}", s, m_str.substring(i0, i1));
  }

  private void addTextTokenUntil(int endIndex) {
    if (endIndex > m_lastTokenEndIndex) {
      m_tokenList.add(new TextToken(m_str.substring(m_lastTokenEndIndex, endIndex)));
    }
    m_lastTokenEndIndex = endIndex;
  }

  private void addToken(IToken t, int endIndex) {
    m_tokenList.add(t);
    m_lastTokenEndIndex = endIndex;
  }

}
