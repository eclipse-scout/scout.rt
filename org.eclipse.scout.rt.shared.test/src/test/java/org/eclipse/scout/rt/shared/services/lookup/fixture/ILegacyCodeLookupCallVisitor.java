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
package org.eclipse.scout.rt.shared.services.lookup.fixture;

import org.eclipse.scout.rt.shared.services.common.code.ICode;

public interface ILegacyCodeLookupCallVisitor<CODE_ID_TYPE> {

  /**
   * @return true=continue visiting, false=end visiting
   */
  boolean visit(LegacyCodeLookupCall<CODE_ID_TYPE> call, ICode<CODE_ID_TYPE> code, int treeLevel);

}
