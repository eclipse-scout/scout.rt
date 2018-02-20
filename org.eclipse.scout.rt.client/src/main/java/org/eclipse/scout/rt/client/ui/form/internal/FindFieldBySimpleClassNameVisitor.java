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
