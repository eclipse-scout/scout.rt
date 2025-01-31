/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
