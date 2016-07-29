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
package org.eclipse.scout.rt.shared.services.common.code;

import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.extension.services.common.code.ICodeTypeExtension;

@ClassId("7bae19d9-0dc8-4cda-9bf5-4b3adbcb4704")
public abstract class AbstractCodeType<CODE_TYPE_ID, CODE_ID> extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ICode<CODE_ID>> implements ICodeType<CODE_TYPE_ID, CODE_ID> {
  private static final long serialVersionUID = 1L;

  public AbstractCodeType() {
    super();
  }

  /**
   * @param callInitializer
   */
  public AbstractCodeType(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * @param label
   * @param hierarchy
   */
  public AbstractCodeType(String label, boolean hierarchy) {
    super(label, hierarchy);
  }

  @Override
  protected ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeType<CODE_TYPE_ID, CODE_ID>> createLocalExtension() {
    return new LocalCodeTypeExtension<CODE_TYPE_ID, CODE_ID, AbstractCodeType<CODE_TYPE_ID, CODE_ID>>(this);
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalCodeTypeExtension<CODE_TYPE_ID, CODE_ID, OWNER extends AbstractCodeType<CODE_TYPE_ID, CODE_ID>>
      extends LocalCodeTypeWithGenericExtension<CODE_TYPE_ID, CODE_ID, ICode<CODE_ID>, OWNER> implements ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, OWNER> {
    private static final long serialVersionUID = 1L;

    public LocalCodeTypeExtension(OWNER owner) {
      super(owner);
    }

  }

}
