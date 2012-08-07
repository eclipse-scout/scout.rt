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
import org.eclipse.scout.rt.client.mobile.ui.form.IMobileForm;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.IMainPageForm;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.IOutlineChooserForm;
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
import org.osgi.framework.Bundle;

public abstract class AbstractMobileStandaloneRwtEnvironment extends AbstractStandaloneRwtEnvironment {
  //TODO CGU move to look and feel decoration
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
    if (scoutForm instanceof IMainPageForm || scoutForm instanceof IOutlineChooserForm) {
      uiFormHeader = new RwtScoutMobileOutlineFormHeader();
    }
    else {
      uiFormHeader = new RwtScoutMobileFormHeader();
    }

    uiFormHeader.setHeightHint(FORM_HEADER_HEIGHT);
    uiFormHeader.createUiField(parent, scoutForm, this);
    return uiFormHeader;
  }

  @Override
  public IRwtScoutFormFooter createFormFooter(Composite parent, IForm scoutForm) {
    if (scoutForm instanceof IMobileForm && ((IMobileForm) scoutForm).isFooterVisible()) {
      RwtScoutMobileFormFooter mobileFormFooter = new RwtScoutMobileFormFooter();
      mobileFormFooter.createUiField(parent, scoutForm, this);
      return mobileFormFooter;
    }

    return null;
  }

  @Override
  protected MobileScoutFormToolkit createScoutFormToolkit(Display display) {
    return new MobileScoutFormToolkit(new MobileFormToolkit(display));
  }

  @Override
  protected BrowserWindowHandler createBrowserWindowHandler() {
    return new MobileBrowserWindowHandler();
  }

}
