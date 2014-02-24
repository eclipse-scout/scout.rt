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
import org.eclipse.scout.rt.spec.client.FieldTypesSpecTest;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.LinkableTypeExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.FormTitleExtractor;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

// TODO ASA javadoc FieldDetailTitleExtractor
public class FieldDetailTitleExtractor<T extends IFormField> extends AbstractNamedTextExtractor<T> {

  public FieldDetailTitleExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.label"));
  }

  @Override
  public String getText(T field) {
    StringBuilder sb = new StringBuilder();
    sb.append(MediawikiUtility.createAnchor(createAnchorId(field)));
    sb.append(new FormFieldLabelExtractor(false, null).getLabelOrSubstituteWhenEmpty(field));
    sb.append(" (").append(new LinkableTypeExtractor<IFormField>(FieldTypesSpecTest.ID).getText(field)).append(")");
    return sb.toString();
  }

  /**
   * create the anchor id for a field with the following syntax<br>
   * [anchorId of the containing form spec]_fielddetail_[classId of the field]<br>
   * --> e.g. c_721c3f5f-bd28-41e4-a5f0-d78891034485_fielddetail_9876545f-bd28-41e4-a5f0-a879987df485
   * 
   * @param field
   * @return
   */
  public static String createAnchorId(IFormField field) {
    StringBuilder sb = new StringBuilder();
    if (field.getForm() != null) {
      sb.append(FormTitleExtractor.getAnchorId(field.getForm()));
    }
    sb.append("_fielddetail_");
    sb.append(field.classId());
    return sb.toString();
  }
}
