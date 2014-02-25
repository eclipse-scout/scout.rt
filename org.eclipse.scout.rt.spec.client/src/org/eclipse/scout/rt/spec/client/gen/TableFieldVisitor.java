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
package org.eclipse.scout.rt.spec.client.gen;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.internal.SectionWithTable;

/**
 * A visitor for {@link ITableField}s that collects information according to configurations for {@link ITableField},
 * {@link org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn IColumn},
 * {@link org.eclipse.scout.rt.client.ui.action.menu.IMenu IMenu}
 */
public class TableFieldVisitor implements IDocFormFieldVisitor {
  private final IDocConfig m_config;
  private final List<IDocSection> m_sections = new ArrayList<IDocSection>();

  public TableFieldVisitor(IDocConfig config) {
    m_config = config;
  }

  @Override
  public boolean visitField(IFormField field, int level, int fieldIndex) {
    if (field instanceof ITableField<?>) {
      IDocSection fieldDesc = createDocSection((ITableField<?>) field);
      m_sections.add(fieldDesc);
    }
    return true;
  }

  private IDocSection createDocSection(ITableField<?> field) {
    String title = m_config.getTableFieldConfig().getTitleExtractor().getText(field);
    IDocSection menuSection = DocGenUtility.createDocSection(field.getTable().getMenus(), m_config.getMenuConfig());
    IDocSection columnsSection = DocGenUtility.createDocSection(field.getTable().getColumns(), m_config.getColumnConfig());
    // TODO ASA refactor if/else cascade
    if (menuSection != null) {
      if (columnsSection != null) {
        return new SectionWithTable(title, menuSection, columnsSection);
      }
      else {
        return new SectionWithTable(title, menuSection);
      }
    }
    if (columnsSection != null) {
      return new SectionWithTable(title, columnsSection);
    }
    return new SectionWithTable(title);
  }

  @Override
  public List<IDocSection> getDocSections() {
    return m_sections;
  }

}
