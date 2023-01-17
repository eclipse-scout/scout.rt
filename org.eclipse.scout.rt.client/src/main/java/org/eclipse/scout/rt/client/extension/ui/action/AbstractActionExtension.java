/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.action;

import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionActionChain;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionDisposeChain;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionInitActionChain;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionSelectionChangedChain;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractActionExtension<OWNER extends AbstractAction> extends AbstractExtension<OWNER> implements IActionExtension<OWNER> {

  public AbstractActionExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execSelectionChanged(ActionSelectionChangedChain chain, boolean selection) {
    chain.execSelectionChanged(selection);
  }

  @Override
  public void execAction(ActionActionChain chain) {
    chain.execAction();
  }

  @Override
  public void execInitAction(ActionInitActionChain chain) {
    chain.execInitAction();
  }

  @Override
  public void execDispose(ActionDisposeChain chain) {
    chain.execDispose();
  }

}
