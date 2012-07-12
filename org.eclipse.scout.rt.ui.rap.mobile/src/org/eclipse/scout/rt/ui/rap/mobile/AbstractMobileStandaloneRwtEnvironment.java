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
import org.eclipse.scout.rt.client.mobile.ui.forms.OutlineChooserForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.ui.rap.AbstractStandaloneRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.mobile.form.RwtScoutMobileForm;
import org.eclipse.scout.rt.ui.rap.mobile.form.RwtScoutMobileFormFooter;
import org.eclipse.scout.rt.ui.rap.mobile.form.RwtScoutMobileFormHeader;
import org.eclipse.scout.rt.ui.rap.mobile.form.RwtScoutMobileOutlineFormHeader;
import org.eclipse.scout.rt.ui.rap.mobile.window.MobileBrowserWindowHandler;
import org.eclipse.scout.rt.ui.rap.mobile.window.desktop.RwtScoutMobileDesktop;
import org.eclipse.scout.rt.ui.rap.mobile.window.dialog.RwtScoutMobileDialog;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.BrowserWindowHandler;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutFormFooter;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutFormHeader;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutDesktop;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.osgi.framework.Bundle;

public abstract class AbstractMobileStandaloneRwtEnvironment extends AbstractStandaloneRwtEnvironment {
  private static final int FORM_HEADER_HEIGHT = 43;

  public AbstractMobileStandaloneRwtEnvironment(Bundle applicationBundle, Class<? extends IClientSession> clientSessionClazz) {
    super(applicationBundle, clientSessionClazz);
  }

  @Override
  protected UserAgent initUserAgent() {
    return UserAgent.create(UiLayer.RAP, UiDeviceType.MOBILE, RwtUtility.getBrowserInfo().getUserAgent());
  }

  @Override
  protected RwtScoutDesktop createUiDesktop() {
    return new RwtScoutMobileDesktop();
  }

  @Override
  protected IRwtScoutPart createUiScoutDialog(IForm form, Shell shell, int dialogStyle) {
    dialogStyle = SWT.APPLICATION_MODAL;

    RwtScoutMobileDialog ui = new RwtScoutMobileDialog();
    ui.createPart(form, shell, dialogStyle, this);
    return ui;
  }

  @Override
  public IRwtScoutForm createForm(Composite parent, IForm scoutForm) {
    RwtScoutMobileForm uiForm = new RwtScoutMobileForm();
    uiForm.createUiField(parent, scoutForm, this);
    return uiForm;
  }

  @Override
  public IRwtScoutFormHeader createFormHeader(Composite parent, IForm scoutForm) {
    IRwtScoutFormHeader uiFormHeader = null;
    if (scoutForm instanceof IOutlineTableForm || scoutForm instanceof OutlineChooserForm) {
      uiFormHeader = new RwtScoutMobileOutlineFormHeader();
    }
    else {
      RwtScoutMobileFormHeader mobileFormHeader = new RwtScoutMobileFormHeader();
      uiFormHeader = mobileFormHeader;
    }

    uiFormHeader.setHeightHint(FORM_HEADER_HEIGHT);
    uiFormHeader.createUiField(parent, scoutForm, this);
    return uiFormHeader;
  }

  @Override
  public IRwtScoutFormFooter createFormFooter(Composite parent, IForm scoutForm) {
    if (!(scoutForm instanceof IOutlineTableForm)) {
      return null;
    }
    RwtScoutMobileFormFooter mobileFormFooter = new RwtScoutMobileFormFooter();
    mobileFormFooter.createUiField(parent, scoutForm, this);
    return mobileFormFooter;
  }

  @Override
  protected MobileScoutFormToolkit createScoutFormToolkit(Display display) {
    return new MobileScoutFormToolkit(new FormToolkit(display));
  }

  @Override
  protected BrowserWindowHandler createBrowserWindowHandler() {
    return new MobileBrowserWindowHandler();
  }

}
