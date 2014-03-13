/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.button;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.snapbox.ISnapBox;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.snapbox.button.RwtScoutSnapBoxMaximizedButton;
import org.eclipse.swt.widgets.Composite;

public class ButtonFieldFactory implements IFormFieldFactory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ButtonFieldFactory.class);

  @Override
  public IRwtScoutFormField<?> createUiFormField(Composite parent, IFormField model, IRwtEnvironment uiEnvironment) {
    if (model instanceof IRadioButton<?>) {
      IRadioButton<?> radio = (IRadioButton<?>) model;
      RwtScoutRadioButton field = new RwtScoutRadioButton();
      field.createUiField(parent, radio, uiEnvironment);
      return field;
    }
    else if (model instanceof IButton) {
      IButton button = (IButton) model;
      IRwtScoutButton<IButton> field = null;
      if (button.getParentField() instanceof ISnapBox) {
        field = createRwtScoutSnapBoxMaximizedButton();
      }
      else {
        if (((IButton) model).getDisplayStyle() == IButton.DISPLAY_STYLE_RADIO) {
          LOG.warn("It seems your RadioButton extends from AbstractButton with getConfiguredDisplayStyle set to DISPLAY_STYLE_RADIO. Please use the class AbstractRadioButton instead.");
        }
        field = createRwtScoutButton();
      }
      field.createUiField(parent, button, uiEnvironment);
      return field;
    }
    return null;
  }

  protected IRwtScoutButton<IButton> createRwtScoutSnapBoxMaximizedButton() {
    return new RwtScoutSnapBoxMaximizedButton();
  }

  protected IRwtScoutButton<IButton> createRwtScoutButton() {
    return new RwtScoutButton<IButton>();
  }
}
