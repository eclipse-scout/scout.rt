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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileDialog extends RwtScoutDialog {
  private P_ResizeListener m_resizeListener;

  @Override
  protected IRwtStandaloneEnvironment getUiEnvironment() {
    return (IRwtStandaloneEnvironment) super.getUiEnvironment();
  }

  @Override
  public boolean isEclipseFormUsed() {
    //Eclipse forms are too heavyweight and may even crash chrome on android.
    //Since the mobile forms don't use any feature of the eclipse forms there is no need to create them.
    return false;
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
    return new FixedSizeDialogBoundsProvider();
  }

  /**
   * Adjusts the size of the dialog if the screen gets resized (e.g. on device rotation).
   */
  private class P_ResizeListener implements Listener {

    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      if (getUiDialog().getShell().isDisposed()) {
        return;
      }

      Rectangle bounds = getBoundsProvider().getBounds();
      if (bounds != null) {
        if (bounds.x >= 0 || bounds.y >= 0) {
          getUiDialog().getShell().setLocation(new Point(bounds.x, bounds.y));
        }
        if (bounds.width >= 0 || bounds.height >= 0) {
          getUiDialog().getShell().setSize(new Point(bounds.width, bounds.height));
        }
      }
    }
  }

}
