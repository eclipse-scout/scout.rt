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
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;

/**
 * Extracts a text containing the simple class name and label. E.g. "MyCompanyTableField (Companies)"
 */
public class TableFieldTypeWithLabelExtractor<T extends IFormField> extends AbstractNamedTextExtractor<T> {

  public TableFieldTypeWithLabelExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.label"));
  }

  @Override
  public String getText(T field) {
    String label = field.getLabel() == null ? "" : " (" + field.getLabel() + ")";
    return field.getClass().getSimpleName() + label;
  }
}
