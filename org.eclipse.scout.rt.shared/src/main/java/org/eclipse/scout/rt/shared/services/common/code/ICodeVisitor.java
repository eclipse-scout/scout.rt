/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

@FunctionalInterface
public interface ICodeVisitor<CODE extends ICode<?>> {

  /**
   * @return true=continue visiting, false=end visiting
   */
  boolean visit(CODE code, int treeLevel);
}
