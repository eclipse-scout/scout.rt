/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
