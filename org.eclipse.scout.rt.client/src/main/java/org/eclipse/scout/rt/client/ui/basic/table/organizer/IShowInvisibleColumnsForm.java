/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
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
