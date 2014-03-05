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

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.IDocTable;
import org.eclipse.scout.rt.spec.client.out.internal.DocTable;
import org.eclipse.scout.rt.spec.client.out.internal.SectionWithTable;

/**
 * Extracts information from {@link IFormField}s by visiting all fields.
 */
public class FormFieldSpecsVisitor implements IDocFormFieldVisitor {
  private final IDocConfig m_config;
  private final List<String[]> m_rows = new ArrayList<String[]>();

  public FormFieldSpecsVisitor(IDocConfig config) {
    m_config = config;
  }

  /**
   * Collects the form field properties ignoring fields that are not accepted by all filters.
   * 
   * @return <code>true</code>, if the field is accepted by all filters, <code>false</code> otherwise.
   */
  @Override
  public boolean visitField(IFormField field, int level, int fieldIndex) {
    if (DocGenUtility.isAccepted(field, m_config.getFieldListConfig().getFilters())) {
      String[] row = DocGenUtility.getTexts(field, m_config.getFieldListConfig().getTextExtractors());
      m_rows.add(row);
    }
    return true;
  }

  @Override
  public List<IDocSection> getDocSections() {
    String[][] rowArray = CollectionUtility.toArray(m_rows, String[].class);
    String[] headers = DocGenUtility.getHeaders(m_config.getFieldListConfig().getTextExtractors());
    IDocTable table = new DocTable(headers, rowArray);
    String title = TEXTS.get("org.eclipse.scout.rt.spec.fields");
    ArrayList<IDocSection> sections = new ArrayList<IDocSection>();
    sections.add(new SectionWithTable(title, table));
    return sections;
  }

}
