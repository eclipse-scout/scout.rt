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
import org.eclipse.scout.rt.ui.rap.AbstractStandaloneRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.mobile.window.desktop.RwtScoutMobileDesktop;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutDesktop;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

public abstract class AbstractTabletStandaloneRwtEnvironment extends AbstractStandaloneRwtEnvironment {

  public AbstractTabletStandaloneRwtEnvironment(Bundle applicationBundle, Class<? extends IClientSession> clientSessionClazz) {
    super(applicationBundle, clientSessionClazz);
  }

  @Override
  protected UserAgent initUserAgent() {
    return UserAgent.create(UiLayer.RAP, UiDeviceType.TABLET, RwtUtility.getBrowserInfo().getUserAgent());
  }

  @Override
  protected RwtScoutDesktop createUiDesktop() {
    return new RwtScoutMobileDesktop();
  }

  @Override
  public IRwtScoutForm createForm(Composite parent, IForm scoutForm) {
    RwtScoutMobileForm uiForm = new RwtScoutMobileForm();
    uiForm.createUiField(parent, scoutForm, this);
    return uiForm;
  }

}
