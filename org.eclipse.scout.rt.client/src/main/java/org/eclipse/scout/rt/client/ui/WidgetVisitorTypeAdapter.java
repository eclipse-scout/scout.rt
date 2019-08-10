/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.function.Function;

import org.eclipse.scout.rt.platform.util.visitor.DepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;

public class WidgetVisitorTypeAdapter<T extends IWidget> implements IDepthFirstTreeVisitor<IWidget> {

  private final Class<T> m_type;
  private final IDepthFirstTreeVisitor<T> m_visitor;

  public WidgetVisitorTypeAdapter(Function<T, TreeVisitResult> visitor, Class<T> type) {
    this(functionToVisitor(visitor), type);
  }

  public static <S extends IWidget> IDepthFirstTreeVisitor<S> functionToVisitor(Function<S, TreeVisitResult> visitor) {
    assertNotNull(visitor);
    return new DepthFirstTreeVisitor<S>() {
      @Override
      public TreeVisitResult preVisit(S widget, int level, int index) {
        return visitor.apply(widget);
      }
    };
  }

  public WidgetVisitorTypeAdapter(IDepthFirstTreeVisitor<T> visitor, Class<T> type) {
    m_type = assertNotNull(type);
    m_visitor = assertNotNull(visitor);
  }

  @Override
  public TreeVisitResult preVisit(IWidget widget, int level, int index) {
    return delegateToFunctionIfTypeMatches(widget, w -> visitor().preVisit(w, level, index), TreeVisitResult.CONTINUE);
  }

  @Override
  public boolean postVisit(IWidget widget, int level, int index) {
    return delegateToFunctionIfTypeMatches(widget, w -> visitor().postVisit(w, level, index), true);
  }

  @SuppressWarnings("unchecked")
  protected <S> S delegateToFunctionIfTypeMatches(IWidget widget, Function<T, S> function, S resultOnTypeMismatch) {
    if (m_type.isInstance(widget)) {
      return function.apply((T) widget);
    }
    return resultOnTypeMismatch;
  }

  public Class<T> type() {
    return m_type;
  }

  public IDepthFirstTreeVisitor<T> visitor() {
    return m_visitor;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " for type '" + type().getName() + "'.";
  }
}
