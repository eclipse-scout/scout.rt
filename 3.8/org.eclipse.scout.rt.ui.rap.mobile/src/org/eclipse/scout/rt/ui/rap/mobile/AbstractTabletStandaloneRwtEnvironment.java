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

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.ui.rap.mobile.window.dialog.RwtScoutMobileDialog;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

public abstract class AbstractTabletStandaloneRwtEnvironment extends AbstractMobileStandaloneRwtEnvironment {

  public AbstractTabletStandaloneRwtEnvironment(Bundle applicationBundle, Class<? extends IClientSession> clientSessionClazz) {
    super(applicationBundle, clientSessionClazz);
  }

  @Override
  protected UserAgent initUserAgent() {
    return UserAgent.create(UiLayer.RAP, UiDeviceType.TABLET, RwtUtility.getBrowserInfo().getUserAgent());
  }

  @Override
  protected IRwtScoutPart createUiScoutDialog(IForm form, Shell shell, int dialogStyle) {
    dialogStyle = SWT.APPLICATION_MODAL | SWT.BORDER;

    RwtScoutMobileDialog ui = new RwtScoutMobileDialog();
    ui.createPart(form, shell, dialogStyle, this);
    return ui;
  }

}
