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
