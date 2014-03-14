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
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.SpecUtility;
import org.eclipse.scout.rt.spec.client.gen.DocGenUtility;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.filter.IDocFilter;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

/**
 * Extracts the label of a field
 */
public class FormFieldLabelExtractor extends AbstractNamedTextExtractor<IFormField> implements IDocTextExtractor<IFormField> {
  private boolean m_hierarchicLabels;
  private List<IDocFilter<IFormField>> m_docFilters;

  /**
   * @param hierarchicLabels
   *          If true, an indentation is applied reflecting the fields hierarchic position (= nesting in GroupBoxes)
   * @param docFilters
   *          Only relevant when <code>hierarchicLabels</code> is <code>true</code>: Used when evaluating the super
   *          hierarchy of a field for calculation the indentation.
   */
  public FormFieldLabelExtractor(boolean hierarchicLabels, List<IDocFilter<IFormField>> docFilters) {
    super(TEXTS.get("org.eclipse.scout.rt.spec.label"));
    m_hierarchicLabels = hierarchicLabels;
    m_docFilters = docFilters;
  }

  @Override
  public String getText(IFormField field) {
    StringBuilder label = new StringBuilder();
    label.append(MediawikiUtility.createAnchor(createAnchorId(field)));
    if (isFieldWithDetailSection(field)) {
      label.append(MediawikiUtility.createLink(FieldDetailTitleExtractor.createAnchorId(field), getLabelOrSubstituteWhenEmpty(field)));
    }
    else {
      label.append(getLabelOrSubstituteWhenEmpty(field));
    }
    return decorateText(field, label.toString());
  }

  /**
   * create the anchor id for a field with the following syntax<br>
   * [anchorId of the containing form spec]_field_[classId of the field]<br>
   * --> e.g. c_721c3f5f-bd28-41e4-a5f0-d78891034485_field_9876545f-bd28-41e4-a5f0-a879987df485
   * 
   * @param field
   * @return
   */
  public static String createAnchorId(IFormField field) {
    StringBuilder sb = new StringBuilder();
    if (field.getForm() != null) {
      sb.append(SpecUtility.createAnchorId(field.getForm()));
    }
    sb.append("_field_");
    sb.append(field.classId());
    return sb.toString();
  }

  /**
   * @param field
   * @return
   */
  protected boolean isFieldWithDetailSection(IFormField field) {
    // TODO ASA improve: only return true if DetailSection is generated; e.g. because a menu is present
    if (field instanceof ISmartField) {
      return true;
    }
    if (field instanceof ITableField) {
      return true;
    }
    return false;
  }

  /**
   * <li>Add indentation <li>Style italic if display text is a substitution for an empty label.
   * 
   * @param field
   * @param displayName
   * @return decoratedText
   */
  protected String decorateText(IFormField field, String displayName) {
    StringBuilder sb = new StringBuilder();
    sb.append(getIndentation(field));
    boolean emptyLabel = StringUtility.isNullOrEmpty(getLabel(field));
    if (emptyLabel) {
      sb.append("''");
    }
    sb.append(displayName);
    if (emptyLabel) {
      sb.append("''");
    }
    return sb.toString();
  }

  /**
   * Reads label-property and returns its value or the text with key "org.eclipse.scout.rt.spec.withoutLabel" in case
   * label is empty
   */
  protected String getLabelOrSubstituteWhenEmpty(IFormField field) {
    StringBuilder sb = new StringBuilder();
    String label = getLabel(field);
    sb.append(StringUtility.substituteWhenEmpty(label,
        TEXTS.get("org.eclipse.scout.rt.spec.withoutLabel")));
    // TODO ASA replace mnemonics: how are ampersands escaped when they are not used for mnemonics?
    return sb.toString().replaceAll("&", "");
  }

  protected String getLabel(IFormField field) {
    return MediawikiUtility.transformToWiki((String) field.getProperty(IFormField.PROP_LABEL));
  }

  protected String getIndentation(IFormField field) {
    StringBuilder sb = new StringBuilder();
    if (m_hierarchicLabels) {
      int level = getLevel(field);
      sb.append(StringUtility.repeat(SpecUtility.getDocConfigInstance().getIndent(), level));
    }
    return sb.toString();
  }

  /**
   * calculate the level for indentation
   * 
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
