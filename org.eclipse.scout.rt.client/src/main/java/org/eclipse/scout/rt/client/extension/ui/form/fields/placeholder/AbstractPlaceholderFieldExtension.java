package org.eclipse.scout.rt.client.extension.ui.form.fields.placeholder;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.placeholder.AbstractPlaceholderField;

public abstract class AbstractPlaceholderFieldExtension<OWNER extends AbstractPlaceholderField> extends AbstractFormFieldExtension<OWNER> implements IPlaceholderFieldExtension<OWNER> {

  public AbstractPlaceholderFieldExtension(OWNER owner) {
    super(owner);
  }
}
