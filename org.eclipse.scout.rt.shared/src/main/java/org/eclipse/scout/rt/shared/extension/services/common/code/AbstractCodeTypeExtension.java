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
package org.eclipse.scout.rt.shared.extension.services.common.code;

import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

public abstract class AbstractCodeTypeExtension<CODE_TYPE_ID, CODE_ID, OWNER extends AbstractCodeType<CODE_TYPE_ID, CODE_ID>>
    extends AbstractCodeTypeWithGenericExtension<CODE_TYPE_ID, CODE_ID, ICode<CODE_ID>, OWNER> {

  private static final long serialVersionUID = 1L;

  /**
   * @param owner
   */
  public AbstractCodeTypeExtension(OWNER owner) {
    super(owner);
  }

}
