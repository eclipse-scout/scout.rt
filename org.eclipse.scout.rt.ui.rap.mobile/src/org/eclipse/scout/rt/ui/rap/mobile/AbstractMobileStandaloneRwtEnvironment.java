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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileForm;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.IMainPageForm;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.IOutlineChooserForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.ui.rap.AbstractStandaloneRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.busy.RwtBusyHandler;
import org.eclipse.scout.rt.ui.rap.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.mobile.busy.RwtMobileBusyHandler;
import org.eclipse.scout.rt.ui.rap.mobile.form.RwtScoutMobileForm;
import org.eclipse.scout.rt.ui.rap.mobile.form.RwtScoutMobileFormFooter;
import org.eclipse.scout.rt.ui.rap.mobile.form.RwtScoutMobileFormHeader;
import org.eclipse.scout.rt.ui.rap.mobile.form.RwtScoutMobileOutlineFormHeader;
import org.eclipse.scout.rt.ui.rap.mobile.patches.TouchScrollingPatch;
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
  protected synchronized void init(Runnable additionalInitCallback) throws CoreException {
    super.init(additionalInitCallback);
    initPatches();
  }

  protected void initPatches() {
    TouchScrollingPatch.enable();
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
  protected boolean needsClientNotificationServerPushSession() {
    // Disabled on mobile devices to avoid having a constant circle of doom.
    // TODO: Should actually also be enabled for mobile devices so that client notifications works.
    return false;
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
  public IRwtScoutFormHeader createFormHeader(org.eclipse.swt.widgets.Composite parent, IForm scoutForm) {
    if (!AbstractMobileForm.isHeaderVisible(scoutForm)) {
      return null;
    }

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
    if (!AbstractMobileForm.isFooterVisible(scoutForm)) {
      return null;
    }

    RwtScoutMobileFormFooter mobileFormFooter = new RwtScoutMobileFormFooter();
    mobileFormFooter.createUiField(parent, scoutForm, this);
    return mobileFormFooter;
  }

  @Override
  protected MobileScoutFormToolkit createScoutFormToolkit(Display display) {
    return new MobileScoutFormToolkit(new MobileFormToolkit(display));
  }

  @Override
  protected BrowserWindowHandler createBrowserWindowHandler() {
    return new MobileBrowserWindowHandler();
  }

  @Override
  protected RwtBusyHandler createBusyHandler() {
    return new RwtMobileBusyHandler(getClientSession(), this);
  }
}
