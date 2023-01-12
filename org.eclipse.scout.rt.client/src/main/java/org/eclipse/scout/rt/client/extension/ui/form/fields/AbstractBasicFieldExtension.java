/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields;

import org.eclipse.scout.rt.client.extension.ui.form.fields.BasicFieldChains.BasicFieldExecChangedDisplayTextChain;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;

public abstract class AbstractBasicFieldExtension<VALUE, OWNER extends AbstractBasicField<VALUE>>
    extends AbstractValueFieldExtension<VALUE, OWNER>
    implements IBasicFieldExtension<VALUE, OWNER> {

  /**
   * @param owner
   */
  public AbstractBasicFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execChangedDisplayText(BasicFieldExecChangedDisplayTextChain<VALUE> chain) {
    chain.execChangedDisplayText();
  }
}
