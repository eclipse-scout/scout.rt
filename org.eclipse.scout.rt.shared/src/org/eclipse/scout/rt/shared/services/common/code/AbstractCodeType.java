/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

/**
 *
 */
public abstract class AbstractCodeType<CODE_TYPE_ID, CODE_ID> extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ICode<CODE_ID>> {
  private static final long serialVersionUID = 1L;

  /**
   *
   */
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

}
