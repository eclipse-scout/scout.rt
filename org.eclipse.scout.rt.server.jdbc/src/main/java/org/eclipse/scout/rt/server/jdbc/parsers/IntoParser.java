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

import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for into variables in arbitrary sql text Syntax (EBNF):
 *
 * <pre>
 * statement = S? * token-list .
 * token-list = token (S? token)* .
 * token = text | into | char .
 * text = `'` text-char* `'` .
 * text-char = [^'] | `''` .
 * into = 'into' S bind-list .
 * bind-list = bind (S? ',' S? bind)* .
 * bind = hash-bind | plain-bind | std-bind .
 * hash-bind = `#` name `#` .
 * plain-bind = `&` name `&` .
 * std-bind = `:` name .
 * name = name-char+ .
 * name-char = [a-zA-Z0-9_.{}] .
 * char = . .
 * S = ([ \n\t\r])+ .
 * </pre>
 */
public class IntoParser {
  private static final Logger LOG = LoggerFactory.getLogger(IntoParser.class);

  private static final String S_MAP;
  private static final String NAME_MAP;

  static {
    S_MAP = " \n\t\r";
    NAME_MAP = "_.0123456789{}ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  }

  private final String m_str;
  private ParsePosition m_pos;
  //
  private final ArrayList<ValueOutputToken> m_intoList = new ArrayList<>();
  private final StringBuilder m_filteredText = new StringBuilder();
  private int m_lastTextIndex;

  public IntoParser(String sqlStatement) {
    m_str = sqlStatement.trim();
  }

  public IntoModel parse() {
    m_pos = new ParsePosition(0);
    parseStatement();
    addTextUntil(m_str.length());
    if (m_pos.getIndex() < m_str.length()) {
      LOG.warn("statement not fully parsed (index {}): {}", m_pos.getIndex(), m_str);
    }
    return new IntoModel(m_filteredText.toString(), m_intoList.toArray(new ValueOutputToken[0]));
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

  private boolean parseToken() {
    if (LOG.isTraceEnabled()) {
      trace("parseToken");
    }
    return (parseText() || parseInto() || parseChar());
  }

  private boolean parseText() {
    if (LOG.isTraceEnabled()) {
      trace("parseText");
    }
    int index = m_pos.getIndex();
    if (matches("'")) {
      while (parseTextChar()) {
        // nop
      }
      if (!matches("'")) {
        LOG.warn("expected ' at position {} of {}", m_pos.getIndex(), m_str);
      }
      return true;
    }
    m_pos.setIndex(index);
    return false;
  }

  private boolean parseInto() {
    if (LOG.isTraceEnabled()) {
      trace("parseInto");
    }
    int index = m_pos.getIndex();
    if (matches("into") && parseWhitespace(1) && parseBindList()) {
      addTextUntil(index);
      ignoreTextUntil(m_pos.getIndex());
      return true;
    }
    else {
      m_pos.setIndex(index);
      return false;
    }
  }

  private boolean parseBindList() {
    if (LOG.isTraceEnabled()) {
      trace("parseBindList");
    }
    int index = m_pos.getIndex();
    if (parseBind()) {
      index = m_pos.getIndex();
      while (parseWhitespace(0) && matches(",") && parseWhitespace(0) && parseBind()) {
        index = m_pos.getIndex();
      }
      m_pos.setIndex(index);
      return true;
    }
    else {
      m_pos.setIndex(index);
      return false;
    }
  }

  private boolean parseBind() {
    if (LOG.isTraceEnabled()) {
      trace("parseBind");
    }
    int index = m_pos.getIndex();
    if (parseHashBind()
        || parsePlainBind()
        || parseStdBind()) {
      return true;
    }
    else {
      m_pos.setIndex(index);
      return false;
    }
  }

  private boolean parseHashBind() {
    if (LOG.isTraceEnabled()) {
      trace("parseHashBind");
    }
    int index = m_pos.getIndex();
    if (matches("#") && parseName() && matches("#")) {
      addIntoToken(new ValueOutputToken(m_str.substring(index, m_pos.getIndex()), m_str.substring(index + 1, m_pos.getIndex() - 1), true));
      return true;
    }
    m_pos.setIndex(index);
    return false;
  }

  private boolean parsePlainBind() {
    if (LOG.isTraceEnabled()) {
      trace("parsePlainBind");
    }
    int index = m_pos.getIndex();
    if (matches("&") && parseName() && matches("&")) {
      addIntoToken(new ValueOutputToken(m_str.substring(index, m_pos.getIndex()), m_str.substring(index + 1, m_pos.getIndex() - 1), true));
      return true;
    }
    m_pos.setIndex(index);
    return false;
  }

  private boolean parseStdBind() {
    if (LOG.isTraceEnabled()) {
      trace("parseStdBind");
    }
    int index = m_pos.getIndex();
    if (matches(":") && parseName()) {
      addIntoToken(new ValueOutputToken(m_str.substring(index, m_pos.getIndex()), m_str.substring(index + 1, m_pos.getIndex()), true));
      return true;
    }
    m_pos.setIndex(index);
    return false;
  }

  private boolean parseName() {
    if (LOG.isTraceEnabled()) {
      trace("parseName");
    }
    int index = m_pos.getIndex();
    while (parseNameChar()) {
      // nop
    }
    if (m_pos.getIndex() > index) {
      return true;
    }
    else {
      m_pos.setIndex(index);
      return false;
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

  private void addTextUntil(int endIndex) {
    if (endIndex > m_lastTextIndex) {
      m_filteredText.append(m_str.substring(m_lastTextIndex, endIndex));
    }
    m_lastTextIndex = endIndex;
  }

  private void ignoreTextUntil(int endIndex) {
    m_lastTextIndex = endIndex;
  }

  private void addIntoToken(ValueOutputToken t) {
    m_intoList.add(t);
  }

}
