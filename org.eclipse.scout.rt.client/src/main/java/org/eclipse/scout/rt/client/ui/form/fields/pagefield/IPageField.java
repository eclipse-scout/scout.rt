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
package org.eclipse.scout.rt.client.ui.form.fields.pagefield;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;

/**
 * Representation of a page as a composite of detailForm, table, searchForm for usage inside a {@link IForm}
 */
public interface IPageField<T extends IPage> extends IGroupBox {

  T getPage();

  void setPage(T newPage);

  IWrappedFormField<IForm> getDetailFormField();

  ITableField<ITable> getTableField();

  IWrappedFormField<IForm> getSearchFormField();
}
