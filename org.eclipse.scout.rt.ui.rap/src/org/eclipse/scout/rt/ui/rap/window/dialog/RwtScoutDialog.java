/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.AbstractRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.window.IFormBoundsProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;

/**
 * <h3>RwtScoutDialog</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutDialog extends AbstractRwtScoutPart {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutDialog.class);

  private DialogImpl m_uiDialog;
  private Form m_uiForm;
  private Point m_uiInitialLocation;
  private Point m_uiInitialSize;
  private IFormBoundsProvider m_boundsProvider;

  public RwtScoutDialog(IFormBoundsProvider boundsProvider) {
    m_boundsProvider = boundsProvider;
    initInitialBounds(m_boundsProvider);
  }

  public RwtScoutDialog() {
  }

  protected void initInitialBounds(IFormBoundsProvider boundsProvider) {
    if (boundsProvider == null) {
      return;
    }

    Rectangle bounds = boundsProvider.getBounds();
    if (bounds != null) {
      setUiInitialLocation(new Point(bounds.x, bounds.y));
      setUiInitialSize(new Point(bounds.width, bounds.height));
    }
  }

  @Override
  public void setBusy(boolean b) {
    getUiForm().setBusy(b);
    getUiForm().layout(true);
  }

  @Override
  public Form getUiForm() {
    return m_uiForm;
  }

  public void setUiInitialLocation(Point initialLocation) {
    m_uiInitialLocation = initialLocation;
  }

  public Point getUiInitialLocation() {
    return m_uiInitialLocation;
  }

  public Point getUiInitialSize() {
    return m_uiInitialSize;
  }

  public void setUiInitialSize(Point uiInitialSize) {
    m_uiInitialSize = uiInitialSize;
  }

  public void createPart(IForm scoutForm, Shell parentShell, IRwtEnvironment uiEnvironment) {
    createPart(scoutForm, parentShell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL, uiEnvironment);
  }

  public void createPart(IForm scoutForm, Shell parentShell, int style, IRwtEnvironment uiEnvironment) {
    super.createPart(scoutForm, uiEnvironment);
    m_uiDialog = new DialogImpl((style & SWT.APPLICATION_MODAL) != 0 ? parentShell : null, style);
    m_uiDialog.create();
    m_uiDialog.getShell().addShellListener(new ShellListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void shellDeactivated(ShellEvent e) {
        getUiEnvironment().invokeScoutLater(new Runnable() {
          @Override
          public void run() {
            getScoutObject().toBack();
          }
        }, 5432);
      }

      @Override
      public void shellClosed(ShellEvent e) {
      }

      @Override
      public void shellActivated(ShellEvent e) {
        getUiEnvironment().invokeScoutLater(new Runnable() {
          @Override
          public void run() {
            getScoutObject().toFront();
          }
        }, 5432);
      }
    });
  }

  protected Control createContentsDelegate(Composite parent) {
    m_uiForm = getUiEnvironment().getFormToolkit().createForm(parent);
    Composite contentPane = m_uiForm.getBody();
    //add form contents
    try {
      contentPane.setRedraw(false);
      IRwtScoutForm rwtForm = getUiEnvironment().createForm(contentPane, getScoutObject());
      GridData d = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
      rwtForm.getUiContainer().setLayoutData(d);
      attachScout();
    }
    finally {
      contentPane.setRedraw(true);
    }
    //set layout and parent data
    GridLayout gridLayout = new GridLayout();
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    contentPane.setLayout(gridLayout);
    GridData d = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    m_uiForm.setLayoutData(d);
    return m_uiForm;
  }

  @Override
  protected void showPartImpl() {
    //never block on open
    m_uiDialog.setBlockOnOpen(false);
    m_uiDialog.open();
  }

  @Override
  protected void closePartImpl() {
    try {
      if (m_boundsProvider != null) {
        m_boundsProvider.storeBounds(m_uiDialog.getShell().getBounds());
      }

      // ensure the traversal is done to write eventually changes to model
      Control focusControl = m_uiDialog.getShell().getDisplay().getFocusControl();
      if (focusControl != null && !focusControl.isDisposed()) {
        //XXX rap       focusControl.traverse(SWT.TRAVERSE_TAB_NEXT);
      }
      detachScout();
    }
    finally {
      m_uiDialog.closePhysically();
    }
  }

  @Override
  protected void setImageFromScout() {
    String iconId = getScoutObject().getIconId();
    Image img = getUiEnvironment().getIcon(iconId);
    m_uiDialog.getShell().setImage(img);
    String sub = getScoutObject().getSubTitle();
    if (sub != null) {
      getUiForm().setImage(img);
    }
    else {
      getUiForm().setImage(null);
    }
  }

  @Override
  protected void setTitleFromScout() {
    IForm f = getScoutObject();
    //
    String s = f.getBasicTitle();
    m_uiDialog.getShell().setText(StringUtility.removeNewLines(s != null ? s : ""));
    //
    s = f.getSubTitle();
    if (s != null) {
      getUiForm().setText(RwtUtility.escapeMnemonics(StringUtility.removeNewLines(s != null ? s : "")));
    }
    else {
      getUiForm().setText(null);
    }
  }

  @Override
  public void activate() {
    m_uiDialog.getShell().setActive();
  }

  @Override
  public boolean isActive() {
    return m_uiDialog.getShell() == getUiEnvironment().getDisplay().getActiveShell();
  }

  @Override
  public boolean isVisible() {
    return m_uiDialog.getShell().isVisible();
  }

  @Override
  public boolean setStatusLineMessage(Image image, String message) {
    // void here
    return false;
  }

  private class DialogImpl extends Dialog {
    private static final long serialVersionUID = 1L;

    public DialogImpl(Shell parentShell, int style) {
      super(parentShell);
      setShellStyle(style);
    }

    @Override
    protected Control createContents(Composite parent) {
      return createContentsDelegate(parent);
    }

    @Override
    protected final Control createButtonBar(Composite parent) {
      // suppress default eclipse button bar
      return null;
    }

    @Override
    public boolean close() {
      //override and delegate to scout model
      Runnable job = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireFormClosingFromUI();
        }
      };
      getUiEnvironment().invokeScoutLater(job, 0);
      return false;
    }

    public boolean closePhysically() {
      return super.close();
    }

    @Override
    protected Point getInitialLocation(Point initialSize) {
      if (m_uiInitialLocation != null) {
        return m_uiInitialLocation;
      }
      return super.getInitialLocation(initialSize);
    }

    @Override
    protected Point getInitialSize() {
      if (m_uiInitialSize != null) {
        return m_uiInitialSize;
      }
      return super.getInitialSize();
    }
  }

}
