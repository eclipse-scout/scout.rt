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
package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineTableFieldChains.OutlineTableFieldTableTitleChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.ITableFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineTableField;

public interface IOutlineTableFieldExtension<OWNER extends AbstractOutlineTableField> extends ITableFieldExtension<ITable, OWNER> {

  void execTableTitleChanged(OutlineTableFieldTableTitleChangedChain chain);
}
