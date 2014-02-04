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

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.FieldTypesSpecTest;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.LinkableTypeExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.SimpleTypeTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.field.FormFieldBooleanPropertyExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.field.FormFieldLabelExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.field.FormFieldPropertyExtractor;

/**
 * The default configuration for {@link IFormField}
 */
public class DefaultFormFieldConfig extends AbstractEntityListConfig<IFormField> {

  private boolean m_hierarchicLabels;

  public DefaultFormFieldConfig() {
    this(true);
  }

  public DefaultFormFieldConfig(boolean hierarchicLabels) {
    m_hierarchicLabels = hierarchicLabels;
  }

  /**
   * Default properties for {@link IFormField} with
   * <p>
   * Label,Description,Tooltip,Mandatory,Enabled,Length,Type
   * </p>
   */
  @Override
  public List<IDocTextExtractor<IFormField>> getTextExtractors() {
    List<IDocTextExtractor<IFormField>> extractors = new ArrayList<IDocTextExtractor<IFormField>>();
    extractors.add(new FormFieldLabelExtractor(m_hierarchicLabels, getFilters()));
    extractors.add(new DescriptionExtractor<IFormField>());
    extractors.add(new FormFieldPropertyExtractor(IFormField.PROP_TOOLTIP_TEXT, TEXTS.get("org.eclipse.scout.rt.spec.tooltip")));
    extractors.add(new FormFieldBooleanPropertyExtractor(IFormField.PROP_MANDATORY, TEXTS.get("org.eclipse.scout.rt.spec.mandatory")));
    extractors.add(new FormFieldBooleanPropertyExtractor(IFormField.PROP_ENABLED, TEXTS.get("org.eclipse.scout.rt.spec.enabled")));
    extractors.add(new FormFieldPropertyExtractor(IStringField.PROP_MAX_LENGTH, TEXTS.get("org.eclipse.scout.rt.spec.length")));
    if (Platform.inDevelopmentMode()) {
      extractors.add(new SimpleTypeTextExtractor<IFormField>("[DEV] Classname"));
    }
    extractors.add(new LinkableTypeExtractor<IFormField>(FieldTypesSpecTest.ID));
    return extractors;
  }

  @Override
  public String getTitle() {
    return TEXTS.get("org.eclipse.scout.rt.spec.fields");
  }

}
