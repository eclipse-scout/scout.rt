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
package org.eclipse.scout.rt.ui.swt.internal;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.snapbox.ISnapBox;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.form.fields.button.ISwtScoutButton;
import org.eclipse.scout.rt.ui.swt.form.fields.button.SwtScoutButton;
import org.eclipse.scout.rt.ui.swt.form.fields.button.SwtScoutRadioButton;
import org.eclipse.scout.rt.ui.swt.form.fields.snapbox.button.SwtScoutSnapBoxMaximizedButton;
import org.eclipse.swt.widgets.Composite;

public class ButtonFieldFactory implements IFormFieldFactory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ButtonFieldFactory.class);

  @Override
  public ISwtScoutFormField<?> createFormField(Composite parent, IFormField model, ISwtEnvironment environment) {
    if (model instanceof IRadioButton<?>) {
      IRadioButton radio = (IRadioButton) model;
      SwtScoutRadioButton field = new SwtScoutRadioButton();
      field.createField(parent, radio, environment);
      return field;
    }
    else if (model instanceof IButton) {
      IButton button = (IButton) model;
      ISwtScoutButton<IButton> field = null;
      if (button.getParentField() instanceof ISnapBox) {
        field = new SwtScoutSnapBoxMaximizedButton();
      }
      else {
        if (((IButton) model).getDisplayStyle() == IButton.DISPLAY_STYLE_RADIO) {
          LOG.warn("It seems your RadioButton extends from AbstractButton with getConfiguredDisplayStyle set to DISPLAY_STYLE_RADIO. Please use the class AbstractRadioButton instead.");
        }
        field = new SwtScoutButton<IButton>();
      }
      field.createField(parent, button, environment);
      return field;
    }
    return null;
  }

}
