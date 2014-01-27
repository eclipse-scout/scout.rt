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
package org.eclipse.scout.rt.spec.client.config.entity;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.field.TableFieldTypeWithLabelExtractor;

/**
 *
 */
public class DefaultTableFieldConfig extends DefaultEntityConfig<ITableField<? extends ITable>> {

  @Override
  public List<IDocTextExtractor<ITableField<? extends ITable>>> getPropertyTextExtractors() {
    return new ArrayList<IDocTextExtractor<ITableField<? extends ITable>>>();
  }

  @Override
  public IDocTextExtractor<ITableField<? extends ITable>> getTitleExtractor() {
    return new TableFieldTypeWithLabelExtractor<ITableField<? extends ITable>>();
  }

}
