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

import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.extension.services.common.code.ICodeTypeExtension;

@ClassId("7bae19d9-0dc8-4cda-9bf5-4b3adbcb4704")
public abstract class AbstractCodeType<CODE_TYPE_ID, CODE_ID> extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ICode<CODE_ID>> {
  private static final long serialVersionUID = 1L;

  public AbstractCodeType() {
    super();
  }

  public AbstractCodeType(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeType<CODE_TYPE_ID, CODE_ID>> createLocalExtension() {
    return new LocalCodeTypeExtension<>(this);
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalCodeTypeExtension<CODE_TYPE_ID, CODE_ID, OWNER extends AbstractCodeType<CODE_TYPE_ID, CODE_ID>>
      extends LocalCodeTypeWithGenericExtension<CODE_TYPE_ID, CODE_ID, ICode<CODE_ID>, OWNER> {
    private static final long serialVersionUID = 1L;

    public LocalCodeTypeExtension(OWNER owner) {
      super(owner);
    }
  }
}
