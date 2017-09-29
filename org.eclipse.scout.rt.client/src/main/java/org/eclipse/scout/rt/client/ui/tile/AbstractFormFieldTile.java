package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * @since 7.1
 */
public abstract class AbstractFormFieldTile<T extends IFormField> extends AbstractWidgetTile<T> implements IFormFieldTile<T> {

  public AbstractFormFieldTile() {
    this(true);
  }

  public AbstractFormFieldTile(boolean callInitializer) {
    super(false);
    if (callInitializer) {
      callInitializer();
    }
  }
}
