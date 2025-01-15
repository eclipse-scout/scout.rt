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

import com.sun.codemodel.JClass;

/**
 * Similar to {@link com.sun.codemodel.JCodeModel.TypeNameParser}, but with possibility to use <?> generic declaration
 * and use of {@link JCodeModelWrapper}.
 *
 * @since 11
 */
public final class TypeNameParserEx {
  private final JCodeModelWrapper m_model;
  private final String m_s;
  private int m_idx;

  public TypeNameParserEx(JCodeModelWrapper model, String s) {
    this.m_model = model;
    this.m_s = s;
  }

  /**
   * Similar to TypeNameParser#parseTypeName(), but with possibility to use <?> generic declaration and use of
   * {@link JCodeModelWrapper}.
   */
  JClass parseTypeName() throws ClassNotFoundException {
    int start = m_idx;

    if (m_s.charAt(m_idx) == '?') {
      // wildcard
      m_idx++;
      ws();
      String head = m_s.substring(m_idx);
      if (head.startsWith(">")) { // allow <?> generic declaration
        return new JTypeEmptyWildcard(m_model);
      }
      if (head.startsWith("extends")) {
        m_idx += 7;
        ws();
        return parseTypeName().wildcard();
      }
      else if (head.startsWith("super")) {
        throw new UnsupportedOperationException("? super T not implemented");
      }
      else {
        // not supported
        throw new IllegalArgumentException("only extends/super can follow ?, but found " + m_s.substring(m_idx));
      }
    }

    while (m_idx < m_s.length()) {
      char ch = m_s.charAt(m_idx);
      if (Character.isJavaIdentifierStart(ch)
          || Character.isJavaIdentifierPart(ch)
          || ch == '.') {
        m_idx++;
      }
      else {
        break;
      }
    }

    JClass clazz = m_model.ref(m_s.substring(start, m_idx)); // use {@link JCodeModelWrapper#ref}

    return parseSuffix(clazz);
  }

  private JClass parseSuffix(JClass clazz) throws ClassNotFoundException {
    if (m_idx == m_s.length()) {
      return clazz; // hit EOL
    }

    char ch = m_s.charAt(m_idx);
    if (ch == '<') {
      return parseSuffix(parseArguments(clazz));
    }

    if (ch == '[') {
      if (m_s.charAt(m_idx + 1) == ']') {
        m_idx += 2;
        return parseSuffix(clazz.array());
      }
      throw new IllegalArgumentException("Expected ']' but found " + m_s.substring(m_idx + 1));
    }
    return clazz;
  }

  private void ws() {
    while (Character.isWhitespace(m_s.charAt(m_idx)) && m_idx < m_s.length()) {
      m_idx++;
    }
  }

  private JClass parseArguments(JClass rawType) throws ClassNotFoundException {
    if (m_s.charAt(m_idx) != '<') {
      throw new IllegalArgumentException();
    }
    m_idx++;
    List<JClass> args = new ArrayList<>();
    while (true) {
      args.add(parseTypeName());
      if (m_idx == m_s.length()) {
        throw new IllegalArgumentException("Missing '>' in " + m_s);
      }
      char ch = m_s.charAt(m_idx);
      if (ch == '>') {
        return rawType.narrow(args.toArray(new JClass[0]));
      }
      if (ch != ',') {
        throw new IllegalArgumentException(m_s);
      }
      m_idx++;
    }
  }
}
