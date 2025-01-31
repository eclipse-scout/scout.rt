/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * Simple form that lists all invisible columns of a table. From these columns, those can be selected, which should be
 * made visible when the form is stored.
 *
 * @since 15.1
 */
public interface IShowInvisibleColumnsForm extends IForm {

  void startModify();

  IShowInvisibleColumnsForm withInsertAfterColumn(IColumn<?> insertAfterColumn);
}
