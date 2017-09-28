/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
