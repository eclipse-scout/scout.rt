/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.wizard;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * Convenience base class for {@link AbstractMenu} that implement {@link IWizardAction}. Because menus lack the property
 * "label", this class redirects the label methods to the "text" property.
 */
@ClassId("45e07452-4c69-4922-b74f-678991893326")
public abstract class AbstractWizardMenu extends AbstractMenu implements IWizardAction {

  @Override
  public void setLabel(String label) {
    setText(label);
  }

  @Override
  public String getLabel() {
    return getText();
  }
}
