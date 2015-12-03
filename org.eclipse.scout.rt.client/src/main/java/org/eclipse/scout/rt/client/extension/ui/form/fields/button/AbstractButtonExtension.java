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
package org.eclipse.scout.rt.client.extension.ui.form.fields.button;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.ButtonChains.ButtonClickActionChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.ButtonChains.ButtonSelectionChangedChain;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;

public abstract class AbstractButtonExtension<OWNER extends AbstractButton> extends AbstractFormFieldExtension<OWNER> implements IButtonExtension<OWNER> {

  public AbstractButtonExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execSelectionChanged(ButtonSelectionChangedChain chain, boolean selection) {
    chain.execSelectionChanged(selection);
  }

  @Override
  public void execClickAction(ButtonClickActionChain chain) {
    chain.execClickAction();
  }
}
