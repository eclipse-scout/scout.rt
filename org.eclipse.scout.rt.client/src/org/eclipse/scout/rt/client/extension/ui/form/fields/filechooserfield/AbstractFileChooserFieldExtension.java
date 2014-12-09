package org.eclipse.scout.rt.client.extension.ui.form.fields.filechooserfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.AbstractFileChooserField;

public abstract class AbstractFileChooserFieldExtension<OWNER extends AbstractFileChooserField> extends AbstractValueFieldExtension<String, OWNER> implements IFileChooserFieldExtension<OWNER> {

  public AbstractFileChooserFieldExtension(OWNER owner) {
    super(owner);
  }
}
