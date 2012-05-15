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
package org.eclipse.scout.rt.ui.rap.mobile;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.mobile.action.ActionButtonBar;
import org.eclipse.scout.rt.ui.rap.util.ScoutFormToolkit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @since 3.8.0
 */
public class MobileScoutFormToolkit extends ScoutFormToolkit {

  public MobileScoutFormToolkit(FormToolkit kit) {
    super(kit);
  }

  @Override
  public Form createForm(Composite parent) {
    Form f = super.createForm(parent);
    decorateFormHeading(f);
    return f;
  }

  /**
   * Creates a {@link ActionButtonBar}.
   * <p>
   * On mobile devices it additionally removes the tooltips because
   * <ul>
   * <li>displaying a tooltip is buggy with rap (it appears often on simple touch down events but should actually only
   * on long pressed ones)</li>
   * <li>displaying a tooltip is not common on mobile devices</li>
   * </ul>
   */
  public ActionButtonBar createActionButtonBar(Composite parent, IMenu[] menus, int style) {
    if (menus != null) {
      for (IMenu menu : menus) {
        menu.setTooltipText(null);
      }
    }

    return new ActionButtonBar(parent, getUiEnvironment(), menus, style);
  }

  /**
   * @see #createActionButtonBar(Composite, IMenu[], int)
   */
  public ActionButtonBar createActionButtonBar(Composite parent, IMenu[] menus) {
    return createActionButtonBar(parent, menus, SWT.NONE);
  }

  protected IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) Display.getCurrent().getData(IRwtEnvironment.class.getName());
  }

}
