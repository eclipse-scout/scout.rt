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
package org.eclipse.scout.rt.ui.rap.window.desktop;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBox;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.AbstractRwtScoutPart;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Form;

/**
 * @author Andreas Hoegger
 * @since 3.8.0
 */
public class RwtScoutDesktopForm extends AbstractRwtScoutPart {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutDesktopForm.class);

  private Composite m_container;
  private IRwtScoutViewStack m_stackComposite;
  private IRwtScoutFormHeader m_formHeaderComposite;
  private Composite m_formBodyComposite;
  private IRwtScoutFormFooter m_formFooterComposite;
  private ViewStackTabButton m_button;

  public RwtScoutDesktopForm() {
  }

  public void createPart(IRwtScoutViewStack stackComposite, Composite parent, ViewStackTabButton button, IForm scoutForm, IRwtEnvironment uiEnvironment) {
    m_stackComposite = stackComposite;
    m_button = button;

    super.createPart(scoutForm, uiEnvironment);

    m_container = getUiEnvironment().getFormToolkit().createComposite(parent);
    try {
      parent.setRedraw(false);

      m_formHeaderComposite = getUiEnvironment().createFormHeader(m_container, getScoutObject());
      m_formBodyComposite = createFormBody(m_container);
      m_formFooterComposite = getUiEnvironment().createFormFooter(m_container, getScoutObject());

      initLayout(m_container);
      attachScout();
    }
    finally {
      parent.setRedraw(true);
    }
  }

  /**
   * Creates a {@link IRwtScoutForm} and embeds it in either a {@link Form} or a regular {@link Composite}, depending on
   * the
   * flag {@link #isEclipseFormUsed()}.
   */
  private Composite createFormBody(Composite parent) {
    Composite body = null;
    Composite actualBody = null;
    if (isEclipseFormUsed()) {
      Form eclipseForm = getUiEnvironment().getFormToolkit().createForm(parent);
      body = eclipseForm;
      actualBody = eclipseForm.getBody();
    }
    else {
      body = getUiEnvironment().getFormToolkit().createComposite(parent);
      actualBody = body;
    }

    actualBody.setLayout(new FillLayout());
    getUiEnvironment().createForm(actualBody, getScoutObject());

    return body;
  }

  @Override
  public Form getUiForm() {
    if (m_formBodyComposite instanceof Form) {
      return (Form) m_formBodyComposite;
    }

    return null;
  }

  @Override
  public Composite getUiContainer() {
    return m_container;
  }

  /**
   * @return true to use a {@link Form} as container of the {@link IRwtScoutForm}. False to use a regular
   *         {@link Composite}.
   */
  public boolean isEclipseFormUsed() {
    return true;
  }

  protected void initLayout(Composite container) {
    GridLayout layout = RwtLayoutUtility.createGridLayoutNoSpacing(1, true);
    container.setLayout(layout);

    Composite header = null;
    if (m_formHeaderComposite != null) {
      header = m_formHeaderComposite.getUiContainer();
    }
    Composite body = m_formBodyComposite;
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
    //nop
  }

  @Override
  protected void closePartImpl() {
    detachScout();
    if (m_button != null) {
      m_button.dispose();
    }
    if (m_container != null && !m_container.isDisposed()) {
      m_container.dispose();
    }
    m_stackComposite = null;
  }

  /**
   * @return the button
   */
  public ViewStackTabButton getTabButton() {
    return m_button;
  }

  @Override
  public boolean isActive() {
    if (!isVisible()) {
      return false;
    }
    Control c = getUiEnvironment().getDisplay().getFocusControl();
    if (c == null) {
      return true;
    }
    return (RwtUtility.isAncestorOf(getUiContainer(), c));
  }

  @Override
  public void activate() {
    if (m_stackComposite == null) {
      LOG.error("stack composite is null");
      return;
    }
    m_stackComposite.setPartVisible(this);
  }

  @Override
  public boolean isVisible() {
    if (m_stackComposite == null) {
      LOG.error("stack composite is null");
      return false;
    }
    return m_stackComposite.isPartVisible(this);
  }

  @Override
  public boolean setStatusLineMessage(Image image, final String message) {
    getUiEnvironment().invokeScoutLater(new Runnable() {
      @Override
      public void run() {
        MessageBox mbox = new MessageBox("Info", message, null);
        mbox.setAutoCloseMillis(2500);
        mbox.startMessageBox();
      }
    }, 3000);
    return true;
  }

  @Override
  protected void setImageFromScout() {
    String iconId = getScoutObject().getIconId();
    Image img = getUiEnvironment().getIcon(iconId);
    if (m_button != null && !m_button.isDisposed()) {
      m_button.setImage(img);
    }
    if (getUiForm() != null && !getUiForm().isDisposed()) {
      String sub = getScoutObject().getSubTitle();
      if (sub != null) {
        getUiForm().setImage(img);
      }
      else {
        getUiForm().setImage(null);
      }
    }
  }

  @Override
  protected void setTitleFromScout() {
    IForm f = getScoutObject();
    //
    String s = f.getBasicTitle();
    if (m_button != null && !m_button.isDisposed()) {
      m_button.setLabel(StringUtility.removeNewLines(s != null ? s : ""));
    }
    //
    s = f.getSubTitle();
    if (getUiForm() != null && !getUiForm().isDisposed()) {
      if (s != null) {
        getUiForm().setText(RwtUtility.escapeMnemonics(StringUtility.removeNewLines(s != null ? s : "")));
      }
      else {
        getUiForm().setText(null);
      }
    }
  }

  @Override
  protected void setCloseEnabledFromScout(boolean defaultValue) {
    if (m_button != null && !m_button.isDisposed()) {
      m_button.setShowClose(defaultValue);
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
  }
}
