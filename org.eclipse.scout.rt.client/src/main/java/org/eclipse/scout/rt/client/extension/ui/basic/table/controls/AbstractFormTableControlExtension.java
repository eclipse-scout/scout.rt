/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.basic.table.controls;

import org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.controls.FormTableControlChains.TableControlInitFormChain;
import org.eclipse.scout.rt.client.ui.basic.table.controls.AbstractTableControl;

public abstract class AbstractFormTableControlExtension<OWNER extends AbstractTableControl> extends AbstractActionExtension<OWNER> implements IFormTableControlExtension<OWNER> {

  public AbstractFormTableControlExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execInitForm(TableControlInitFormChain chain) {
    chain.execInitForm();
  }
}
