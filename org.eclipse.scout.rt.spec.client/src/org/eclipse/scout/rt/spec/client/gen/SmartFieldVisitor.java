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
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.spec.client.SpecUtility;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.IDocTable;
import org.eclipse.scout.rt.spec.client.out.internal.Section;

/**
 * A visitor for {@link org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField ISmartField}s that collects
 * information according to configuration.
 */
public class SmartFieldVisitor implements IDocFormFieldVisitor {

  private final IDocConfig m_config;
  private final List<IDocSection> m_sections = new ArrayList<IDocSection>();

  public SmartFieldVisitor(IDocConfig config) {
    m_config = config;
  }

  @Override
  public boolean visitField(IFormField field, int level, int fieldIndex) {
    if (field instanceof ISmartField<?>) {
      IDocSection fieldDesc = createDocSection((ISmartField<?>) field);
      m_sections.add(fieldDesc);
    }
    return true;
  }

  protected IDocSection createDocSection(ISmartField<?> field) {
    String title = m_config.getSmartFieldConfig().getTitleExtractor().getText(field);
    IDocTable docTable = DocGenUtility.createDocTable(field, m_config.getSmartFieldConfig(), true);
    IDocSection menuSection = DocGenUtility.createDocSection(SpecUtility.expandMenuHierarchy(field.getMenus()), m_config.getMenuTableConfig(), false);
    return new Section(title, docTable, menuSection);
  }

  @Override
  public List<IDocSection> getDocSections() {
    return m_sections;
  }

}
