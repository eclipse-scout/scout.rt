package org.eclipse.scout.rt.client.extension.ui.form.fields.filechooserfield;

import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.AbstractFileChooserField;

public abstract class AbstractFileChooserFieldExtension<OWNER extends AbstractFileChooserField> extends AbstractValueFieldExtension<BinaryResource, OWNER> implements IFileChooserFieldExtension<OWNER> {

  public AbstractFileChooserFieldExtension(OWNER owner) {
    super(owner);
  }
}
