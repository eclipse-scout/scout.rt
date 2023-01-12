/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.visitor;

/**
 * Empty adapter implementation for {@link IDepthFirstTreeVisitor}.
 *
 * @since 8.0
 * @see TreeVisitResult
 * @see IDepthFirstTreeVisitor
 * @see TreeTraversals
 */
public class DepthFirstTreeVisitor<T> implements IDepthFirstTreeVisitor<T> {

  @Override
  public TreeVisitResult preVisit(T element, int level, int index) {
    return TreeVisitResult.CONTINUE;
  }

  @Override
  public boolean postVisit(T element, int level, int index) {
    return true;
  }
}
