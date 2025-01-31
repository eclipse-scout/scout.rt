/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.lookup.fixture;

import org.eclipse.scout.rt.shared.services.common.code.ICode;

public interface ILegacyCodeLookupCallVisitor<CODE_ID_TYPE> {

  /**
   * @return true=continue visiting, false=end visiting
   */
  boolean visit(LegacyCodeLookupCall<CODE_ID_TYPE> call, ICode<CODE_ID_TYPE> code, int treeLevel);
}
