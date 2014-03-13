package org.eclipse.scout.rt.ui.swing.form.fields.button;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;

public class SwingScoutRadioButton extends SwingScoutButton<IRadioButton> implements ISwingScoutRadioButton {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutRadioButton.class);

  public SwingScoutRadioButton() {
    super();
  }

  @Override
  protected void adaptButtonLayoutData(LogicalGridData gd) {
    super.adaptButtonLayoutData(gd);
    //bsi ticket 101344: modify the layout constraint for the checkbox, so it is only as wide as its label.
    //this avoids that clicking in white space area toggles the value
    gd.fillHorizontal = false;
  }

}
