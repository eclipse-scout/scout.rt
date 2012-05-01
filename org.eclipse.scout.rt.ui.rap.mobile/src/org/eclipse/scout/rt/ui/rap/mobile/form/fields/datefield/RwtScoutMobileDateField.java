/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.datefield;

import java.util.Date;

import org.eclipse.scout.rt.ui.rap.form.fields.datefield.RwtScoutDateField;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser.DateChooserDialog;
import org.eclipse.scout.rt.ui.rap.mobile.form.fields.datefield.chooser.MobileDateChooserDialog;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileDateField extends RwtScoutDateField {

  @Override
  protected DateChooserDialog createDateChooserDialog(Shell parentShell, Date currentDate) {
    return new MobileDateChooserDialog(parentShell, currentDate);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);

    // Disable field to avoid the appearance of the keyboard. Choosing a date is only possible with the date picker popup.
    getUiField().setEnabled(false);
  }

  @Override
  protected void handleUiFocusLostOnDatePickerPopup(FocusEvent event) {
    // Since the popup hides the ui field it is not necessary to reset the focus
    // It's also not necessary to close the popup on focus lost because the it can be closed with the "x" at the title bar.
  }

}
