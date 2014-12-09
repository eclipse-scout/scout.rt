package org.eclipse.scout.rt.client.extension.ui.form.fields.snapbox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractCompositeFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.snapbox.AbstractSnapBox;

public abstract class AbstractSnapBoxExtension<OWNER extends AbstractSnapBox> extends AbstractCompositeFieldExtension<OWNER> implements ISnapBoxExtension<OWNER> {

  public AbstractSnapBoxExtension(OWNER owner) {
    super(owner);
  }
}
