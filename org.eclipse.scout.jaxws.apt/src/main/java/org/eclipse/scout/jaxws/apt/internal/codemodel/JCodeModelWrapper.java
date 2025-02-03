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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;

public final class JCodeModelWrapper {

  private final Map<String, JClass> m_refJClassCache = new HashMap<>();
  private final JCodeModel m_model = new JCodeModel();

  public JCodeModelWrapper() {
  }

  public JCodeModel getModel() {
    return m_model;
  }

  /**
   * Wrapper method to ensure that we get the same JClass instance all the time for the same fullyQualifiedClassName.
   */
  public JClass ref(String fullyQualifiedClassName) {
    return m_refJClassCache.computeIfAbsent(fullyQualifiedClassName, m_model::ref);
  }

  /**
   * This method is similar to {@link JCodeModel#parseType(String)}, but uses own TypeNameParser.
   */
  public JType parseType(String name) throws ClassNotFoundException {
    // array
    String suffix = "[]";
    if (name.endsWith(suffix)) {
      return parseType(name.substring(0, name.length() - suffix.length())).array();
    }

    // try primitive type
    try {
      return JType.parse(m_model, name);
    }
    catch (IllegalArgumentException e) {
      // NOP - not a primitive type
    }

    // existing class
    return new TypeNameParserEx(this, name).parseTypeName();
  }

  public JClass ref(Class<?> clazz) {
    return m_model.ref(clazz);
  }

  public JType _ref(Class<?> c) {
    return m_model._ref(c);
  }

  public JDefinedClass _class(String fullyqualifiedName) throws JClassAlreadyExistsException {
    return m_model._class(fullyqualifiedName);
  }

  public JDefinedClass anonymousClass(JClass baseType) {
    return m_model.anonymousClass(baseType);
  }

  public JDefinedClass anonymousClass(Class<?> baseType) {
    return m_model.anonymousClass(baseType);
  }

  public void build(CodeWriter out) throws IOException {
    m_model.build(out);
  }
}
