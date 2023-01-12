/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
