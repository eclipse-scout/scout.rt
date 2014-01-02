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

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.FormFieldUtility;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;

/**
 * Extracts the unique field id for the form field
 */
public class FormFieldIdExtractor extends AbstractNamedTextExtractor<IFormField> implements IDocTextExtractor<IFormField> {

  public FormFieldIdExtractor(String name) {
    super(name);
  }

  public FormFieldIdExtractor() {
    this(TEXTS.get("org.eclipse.scout.rt.spec.id"));
  }

  /**
   * A unique id text for the field.
   */
  @Override
  public String getText(IFormField field) {
    return FormFieldUtility.getUniqueFieldId(field);
  }

}
