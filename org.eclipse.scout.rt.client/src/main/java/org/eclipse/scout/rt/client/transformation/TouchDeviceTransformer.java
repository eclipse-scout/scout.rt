package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

@Order(5100)
public class TouchDeviceTransformer extends AbstractDeviceTransformer {

  @Override
  public boolean isActive() {
    return UserAgentUtility.isTouchDevice();
  }

  @Override
  public void transformFormField(IFormField field) {
    if (field instanceof IProposalField<?>) {
      ((IProposalField) field).setAutoCloseChooser(false);
    }
  }
}
