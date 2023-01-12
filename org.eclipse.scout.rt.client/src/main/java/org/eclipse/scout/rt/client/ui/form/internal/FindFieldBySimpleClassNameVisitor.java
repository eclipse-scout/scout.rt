/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.internal;

import java.util.function.Function;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;

public class FindFieldBySimpleClassNameVisitor implements Function<IFormField, TreeVisitResult> {
  private final String m_simpleName;
  private IFormField m_found;

  public FindFieldBySimpleClassNameVisitor(String simpleName) {
    m_simpleName = simpleName;
  }

  @Override
  public TreeVisitResult apply(IFormField field) {
    if (field.getClass().getSimpleName().equalsIgnoreCase(m_simpleName)) {
      m_found = field;
      return TreeVisitResult.TERMINATE;
    }
    return TreeVisitResult.CONTINUE;
  }

  public IFormField getField() {
    return m_found;
  }
}
