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
package org.eclipse.scout.rt.spec.client.gen.extract.form.field;

import java.util.List;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.DocGenUtility;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.filter.IDocFilter;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

/**
 * Extracts the label of a field
 */
public class FormFieldLabelExtractor extends AbstractNamedTextExtractor<IFormField> implements IDocTextExtractor<IFormField> {
  protected static final String INDENT = "&nbsp;&nbsp;&nbsp;";
  private boolean m_hierarchicLabels;
  private List<IDocFilter<IFormField>> m_docFilters;

  public FormFieldLabelExtractor(boolean hierarchicLabels, List<IDocFilter<IFormField>> docFilters) {
    super(TEXTS.get("org.eclipse.scout.rt.spec.label"));
    m_hierarchicLabels = hierarchicLabels;
    m_docFilters = docFilters;
  }

  /**
   * Reads label-property returns its value with correct indentation
   */
  @Override
  public String getText(IFormField field) {
    StringBuilder sb = new StringBuilder();
    if (m_hierarchicLabels) {
      int level = getLevel(field);
      sb.append(StringUtility.repeat(INDENT, level));
      if (level > 0) {
        sb.append(" ");
      }
    }
    Object labelProperty = MediawikiUtility.transformToWiki((String) field.getProperty(IFormField.PROP_LABEL));
    sb.append(StringUtility.substituteWhenEmpty(labelProperty, "''" + TEXTS.get("org.eclipse.scout.rt.spec.action.withoutLabel") + "''"));
    return sb.toString();
  }

  /**
   * @param field
   * @return
   */
  protected int getLevel(IFormField field) {
    IFormField parentField = field.getParentField();
    int level = 0;
    while (parentField != null) {
      if (DocGenUtility.isAccepted(parentField, m_docFilters)) {
        ++level;
      }
      parentField = parentField.getParentField();
    }
    return level;
  }
}
