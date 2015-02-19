package org.eclipse.scout.rt.client.extension.ui.form.fields.colorpickerfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractBasicFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.colorpickerfield.AbstractColorField;

public abstract class AbstractColorFieldExtension<OWNER extends AbstractColorField> extends AbstractBasicFieldExtension<String, OWNER> implements IColorFieldExtension<OWNER> {

  public AbstractColorFieldExtension(OWNER owner) {
    super(owner);
  }
}
