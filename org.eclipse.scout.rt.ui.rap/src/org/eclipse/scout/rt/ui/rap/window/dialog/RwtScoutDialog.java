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
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.AbstractRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.window.DefaultFormBoundsProvider;
import org.eclipse.scout.rt.ui.rap.window.IFormBoundsProvider;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutFormFooter;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutFormHeader;
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
 * @since 3.8.0
 */
public class RwtScoutDialog extends AbstractRwtScoutPart {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutDialog.class);
  private static String VARIANT_DIALOG_SHELL = "dialog";

  private DialogImpl m_uiDialog;
  private Form m_uiForm;
  private Point m_uiInitialLocation;
  private Point m_uiInitialSize;
  private IFormBoundsProvider m_boundsProvider;
  private IRwtScoutForm m_formComposite;
  private IRwtScoutFormHeader m_formHeaderComposite;
  private IRwtScoutFormFooter m_formFooterComposite;

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

  protected IFormBoundsProvider createFormBoundsProvider(IForm scoutForm, IRwtEnvironment uiEnvironment) {
    return new DefaultFormBoundsProvider(scoutForm, uiEnvironment);
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

  /**
   * Sets the initial location of the dialog. <br/>
   * This property has no effect if {@link IForm#isCacheBounds()} is set because a {@link IFormBoundsProvider} is
   * used in that case.
   */
  public void setUiInitialLocation(Point initialLocation) {
    m_uiInitialLocation = initialLocation;
  }

  public Point getUiInitialLocation() {
    return m_uiInitialLocation;
  }

  public Point getUiInitialSize() {
    return m_uiInitialSize;
  }

  /**
   * Sets the initial size of the dialog. <br/>
   * This property has no effect if {@link IForm#isCacheBounds()} is set because a {@link IFormBoundsProvider} is
   * used in that case.
   */
  public void setUiInitialSize(Point uiInitialSize) {
    m_uiInitialSize = uiInitialSize;
  }

  protected int getDialogStyle() {
    return SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
  }

  public void createPart(IForm scoutForm, Shell parentShell, IRwtEnvironment uiEnvironment) {
    createPart(scoutForm, parentShell, getDialogStyle(), uiEnvironment);
  }

  public void createPart(IForm scoutForm, Shell parentShell, int style, IRwtEnvironment uiEnvironment) {
    super.createPart(scoutForm, uiEnvironment);

    if (scoutForm.isCacheBounds()) {
      m_boundsProvider = createFormBoundsProvider(scoutForm, getUiEnvironment());
      initInitialBounds(m_boundsProvider);
    }

    m_uiDialog = new DialogImpl((style & SWT.APPLICATION_MODAL) != 0 ? parentShell : null, style);
    m_uiDialog.create();
    m_uiDialog.getShell().setData(WidgetUtil.CUSTOM_VARIANT, getDialogShellVariant());
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

  private String getDialogShellVariant() {
    return VARIANT_DIALOG_SHELL;
  }

  protected Control createContentsDelegate(Composite parent) {
    m_uiForm = getUiEnvironment().getFormToolkit().createForm(parent);
    Composite contentPane = m_uiForm.getBody();
    //add form contents
    try {
      contentPane.setRedraw(false);
      m_formHeaderComposite = getUiEnvironment().createFormHeader(contentPane, getScoutObject());
      m_formComposite = getUiEnvironment().createForm(contentPane, getScoutObject());
      m_formFooterComposite = getUiEnvironment().createFormFooter(contentPane, getScoutObject());

      initLayout(contentPane);
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

  protected void initLayout(Composite container) {
    GridLayout layout = RwtLayoutUtility.createGridLayoutNoSpacing(1, true);
    container.setLayout(layout);

    Composite header = null;
    if (m_formHeaderComposite != null) {
      header = m_formHeaderComposite.getUiContainer();
    }
    Composite body = null;
    if (m_formComposite != null) {
      body = m_formComposite.getUiContainer();
    }
    Composite footer = null;
    if (m_formFooterComposite != null) {
      footer = m_formFooterComposite.getUiContainer();
    }

    if (header != null) {
      GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
      if (getFormHeaderHeightHint() != null) {
        gridData.heightHint = getFormHeaderHeightHint();
      }
      if (!header.isVisible()) {
        gridData.exclude = true;
      }
      header.setLayoutData(gridData);
    }

    if (body != null) {
      GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
      body.setLayoutData(gridData);
    }

    if (footer != null) {
      GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
      if (getFormFooterHeightHint() != null) {
        gridData.heightHint = getFormFooterHeightHint();
      }
      if (!footer.isVisible()) {
        gridData.exclude = true;
      }      
      footer.setLayoutData(gridData);
    }

  }

  public Integer getFormHeaderHeightHint() {
    if (m_formHeaderComposite == null) {
      return null;
    }

    return m_formHeaderComposite.getHeightHint();
  }

  public Integer getFormFooterHeightHint() {
    if (m_formFooterComposite == null) {
      return null;
    }

    return m_formFooterComposite.getHeightHint();
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

  protected Dialog getUiDialog() {
    return m_uiDialog;
  }

  protected IFormBoundsProvider getBoundsProvider() {
    return m_boundsProvider;
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
