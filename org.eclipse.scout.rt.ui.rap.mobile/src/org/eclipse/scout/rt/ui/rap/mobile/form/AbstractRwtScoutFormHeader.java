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
package org.eclipse.scout.rt.ui.rap.mobile.form;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.mobile.action.AbstractRwtScoutActionBar;
import org.eclipse.scout.rt.ui.rap.mobile.action.ActionButtonBar;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutFormHeader;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8.0
 */
public class AbstractRwtScoutFormHeader extends AbstractRwtScoutActionBar<IForm> implements IRwtScoutFormHeader {

  private static final String VARIANT_FORM_HEADER = "mobileFormHeader";

  @Override
  protected void initializeUi(Composite parent) {
    super.initializeUi(parent);

    getUiContainer().setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FORM_HEADER);
  }

  @Override
  public boolean isAlwaysVisible() {
    return true;
  }

  @Override
  protected void attachScout() {
    super.attachScout();

    setTitle(getScoutObject().getTitle());
  }

  @Override
  protected void adaptLeftButtonBar(ActionButtonBar buttonBar) {
    buttonBar.setPilingEnabled(false);
  }

  @Override
  protected void adaptRightButtonBar(ActionButtonBar buttonBar) {
    buttonBar.setMinNumberOfAlwaysVisibleButtons(1);
    buttonBar.setMaxNumberOfAlwaysVisibleButtons(1);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);

    if (name.equals(IForm.PROP_TITLE)) {
      setTitle((String) newValue);
    }
  }
}
