package org.eclipse.scout.rt.client.extension.ui.form.fields.splitbox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractCompositeFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.AbstractSplitBox;

public abstract class AbstractSplitBoxExtension<OWNER extends AbstractSplitBox> extends AbstractCompositeFieldExtension<OWNER> implements ISplitBoxExtension<OWNER> {

  public AbstractSplitBoxExtension(OWNER owner) {
    super(owner);
  }
}
