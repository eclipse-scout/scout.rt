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
package org.eclipse.scout.rt.ui.rap.mobile.window.dialog;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.IRwtStandaloneEnvironment;
import org.eclipse.scout.rt.ui.rap.window.IFormBoundsProvider;
import org.eclipse.scout.rt.ui.rap.window.dialog.RwtScoutDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileDialog extends RwtScoutDialog {
  private P_ResizeListener m_resizeListener;

  @Override
  protected IRwtStandaloneEnvironment getUiEnvironment() {
    return (IRwtStandaloneEnvironment) super.getUiEnvironment();
  }

  @Override
  public void createPart(IForm scoutForm, Shell parentShell, int style, IRwtEnvironment uiEnvironment) {
    scoutForm.setCacheBounds(true);

    super.createPart(scoutForm, parentShell, style, uiEnvironment);

    m_resizeListener = new P_ResizeListener();
    getUiEnvironment().getUiDesktop().getUiContainer().addListener(SWT.Resize, m_resizeListener);
  }

  @Override
  protected void closePartImpl() {
    super.closePartImpl();

    getUiEnvironment().getUiDesktop().getUiContainer().removeListener(SWT.Resize, m_resizeListener);
  }

  @Override
  protected IFormBoundsProvider createFormBoundsProvider(IForm scoutForm, IRwtEnvironment uiEnvironment) {
    return new MobileDialogBoundsProvider(scoutForm, uiEnvironment);
  }

  @Override
  protected MobileDialogBoundsProvider getBoundsProvider() {
    return (MobileDialogBoundsProvider) super.getBoundsProvider();
  }

  private class P_ResizeListener implements Listener {

    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      if (getUiDialog().getShell().isDisposed()) {
        return;
      }

      getUiDialog().getShell().setBounds(getBoundsProvider().getBounds());
    }
  }

}
