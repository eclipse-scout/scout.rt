/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.fixture;

import org.eclipse.scout.commons.annotations.Doc;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.spec.client.fixture.SimplePersonForm.MainBox.GroupBox;
import org.eclipse.scout.rt.spec.client.fixture.SimplePersonForm.MainBox.TableField;
import org.eclipse.scout.rt.spec.client.fixture.SimplePersonForm.MainBox.TableField.Table;

/**
 * A simple form with @Doc(filter = Doc.FilterResult.REJECTED) for testing
 */
public class SimplePersonForm extends AbstractForm {

  public SimplePersonForm() throws ProcessingException {
    super();
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public GroupBox getGroupBox() {
    return getFieldByClass(GroupBox.class);
  }

  public TableField getTableField() {
    return getFieldByClass(TableField.class);
  }

  @Order(10.0f)
  public class MainBox extends AbstractGroupBox {

    @Order(20.0)
    @Doc(filter = Doc.Filtering.REJECT)
    public class GroupBox extends AbstractGroupBox {

      @Order(10.0)
      public class NameField extends AbstractStringField {
      }

      @Order(20.0)
      public class FirstNameField extends AbstractStringField {
      }
    }

    @Order(10.0)
    public class TableField extends AbstractTableField<Table> {

      public class Table extends AbstractTable {

      }
    }

  }

}
