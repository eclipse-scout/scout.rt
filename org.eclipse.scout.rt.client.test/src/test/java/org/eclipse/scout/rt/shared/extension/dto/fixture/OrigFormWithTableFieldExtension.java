/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableInitTableChain;
import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableField.MainBox.TableInOrigFormField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableField.MainBox.TableInOrigFormField.Table;

@Data(OrigFormWithTableFieldExtensionData.class)
public class OrigFormWithTableFieldExtension extends AbstractFormExtension<OrigFormWithTableField> {

  public static final Integer VALUE_OF_CONTRIBUTED_COL = Integer.valueOf(111);

  public OrigFormWithTableFieldExtension(OrigFormWithTableField ownerForm) {
    super(ownerForm);
  }

  public class TableInOrigFormFieldExtension extends AbstractTableExtension<TableInOrigFormField.Table> {

    public TableInOrigFormFieldExtension(Table owner) {
      super(owner);
    }

    @Override
    public void execInitTable(TableInitTableChain chain) {
      super.execInitTable(chain);
      getOwner().getColumnSet().getColumnByClass(ContributedIntColumn.class).setValue(0, VALUE_OF_CONTRIBUTED_COL);
    }

    @Order(1000.0)
    public class ContributedIntColumn extends AbstractIntegerColumn {
    }
  }
}
