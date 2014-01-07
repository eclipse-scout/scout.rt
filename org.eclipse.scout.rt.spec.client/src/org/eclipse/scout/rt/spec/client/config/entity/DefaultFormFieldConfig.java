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

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.LinkableTypeExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.field.FormFieldBooleanPropertyExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.field.FormFieldPropertyExtractor;

/**
 * The default configuration for {@link IFormField}
 */
public class DefaultFormFieldConfig extends AbstractEntityListConfig<IFormField> {

  /**
   * Default properties for {@link org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn IColumn} with
   * <p>
   * Label,Type,Length,Mandatory,Enabled,Tooltip,Description
   * </p>
   */
  @Override
  public List<IDocTextExtractor<IFormField>> getTexts() {
    List<IDocTextExtractor<IFormField>> propertyTemplate = new ArrayList<IDocTextExtractor<IFormField>>();
    propertyTemplate.add(new FormFieldPropertyExtractor(IFormField.PROP_LABEL, TEXTS.get("org.eclipse.scout.rt.spec.label")));
    propertyTemplate.add(new DescriptionExtractor<IFormField>());
    propertyTemplate.add(new FormFieldPropertyExtractor(IFormField.PROP_TOOLTIP_TEXT, TEXTS.get("org.eclipse.scout.rt.spec.tooltip")));
    propertyTemplate.add(new FormFieldBooleanPropertyExtractor(IFormField.PROP_MANDATORY, TEXTS.get("org.eclipse.scout.rt.spec.mandatory")));
    propertyTemplate.add(new FormFieldBooleanPropertyExtractor(IFormField.PROP_ENABLED, TEXTS.get("org.eclipse.scout.rt.spec.enabled")));
    propertyTemplate.add(new FormFieldPropertyExtractor(IStringField.PROP_MAX_LENGTH, TEXTS.get("org.eclipse.scout.rt.spec.length")));
    propertyTemplate.add(new LinkableTypeExtractor<IFormField>());
    return propertyTemplate;
  }

  @Override
  public String getTitle() {
    return TEXTS.get("org.eclipse.scout.rt.spec.fields");
  }

}
