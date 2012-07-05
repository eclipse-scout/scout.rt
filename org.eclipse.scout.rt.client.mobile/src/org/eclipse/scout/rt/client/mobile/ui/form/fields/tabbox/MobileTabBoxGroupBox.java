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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.FormFieldPropertyDelegator;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;

/**
 * Group box which represents a {@link ITabBox} and contains {@link MobileTabBoxButton}s to represent the tab items.
 * 
 * @since 3.9.0
 */
public class MobileTabBoxGroupBox extends AbstractGroupBox {
  private List<IButton> m_injectedButtons;
  private FormFieldPropertyDelegator<ITabBox, IGroupBox> m_propertyDelegator;

  public MobileTabBoxGroupBox(ITabBox tabBox) {
    super(false);

    m_propertyDelegator = new FormFieldPropertyDelegator<ITabBox, IGroupBox>(tabBox, this);
    List<IButton> buttons = new LinkedList<IButton>();
    for (IGroupBox groupBox : tabBox.getGroupBoxes()) {
      IButton button = new MobileTabBoxButton(groupBox);
      buttons.add(button);
    }
    m_injectedButtons = buttons;
    callInitializer();
    setFormInternal(tabBox.getForm());
    rebuildFieldGrid();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
  }

  @Override
  protected void injectFieldsInternal(List<IFormField> fieldList) {
    if (m_injectedButtons != null) {
      for (IFormField f : m_injectedButtons) {
        fieldList.add(f);
      }
    }
  }

  @Override
  protected int getConfiguredGridColumnCount() {
    return 2;
  }

  @Override
  protected String getConfiguredBorderDecoration() {
    return BORDER_DECORATION_LINE;
  }

}
