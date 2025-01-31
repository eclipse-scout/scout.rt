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

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.controls.FormTableControlChains.TableControlInitFormChain;
import org.eclipse.scout.rt.client.ui.basic.table.controls.AbstractTableControl;

public interface IFormTableControlExtension<OWNER extends AbstractTableControl> extends IActionExtension<OWNER> {

  void execInitForm(TableControlInitFormChain chain);
}
