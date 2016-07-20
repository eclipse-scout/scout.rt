package org.eclipse.scout.rt.client.extension.ui.form.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.BottomBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;

/**
 * Extended {@link OrigForm} with a replaced {@link BottomBox} and an additional city field.
 */
public class OrigFormEx extends OrigForm {

  @Replace
  public class BottomBoxEx extends MainBox.BottomBox {
    public BottomBoxEx(MainBox container) {
      container.super();
    }

    @Order(20)
    public class CityField extends AbstractStringField {
    }
  }
}
