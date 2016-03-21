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
package org.eclipse.scout.rt.client.extension.ui.basic.table.controls;

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.controls.FormTableControlChains.TableControlInitFormChain;
import org.eclipse.scout.rt.client.ui.basic.table.controls.AbstractTableControl;

public interface IFormTableControlExtension<OWNER extends AbstractTableControl> extends IActionExtension<OWNER> {

  void execInitForm(TableControlInitFormChain chain);

}
