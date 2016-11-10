package org.eclipse.scout.rt.shared.extension.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm.MainBox.TopBox;

public class TopBoxExtension extends AbstractGroupBoxExtension<BasicForm.MainBox.TopBox> {

  public TopBoxExtension(TopBox owner) {
    super(owner);
  }

  // not a replacement, but a second name field with the same behavior
  @Order(20)
  public class SecondNameField extends BasicForm.MainBox.TopBox.NameField {

    public SecondNameField(BasicForm.MainBox.TopBox container) {
      container.super();
    }
  }
}
