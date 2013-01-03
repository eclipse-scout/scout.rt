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
package org.eclipse.scout.rt.ui.rap.mobile.busy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog which shows a waiting message and a button to abort waiting.
 * 
 * @since 3.9.0
 */
public class BusyBlockDialog extends Dialog {
  private static final long serialVersionUID = 1L;
  private static final String DIALOG_VARIANT = "busy-dialog";

  private IRwtEnvironment m_uiEnvironment;
  private IProgressMonitor m_progressMonitor;
  private Label m_messageLabel;

  public BusyBlockDialog(Shell parentShell, IRwtEnvironment uiEnvironment, IProgressMonitor progressMonitor) {
    super(parentShell);
    m_uiEnvironment = uiEnvironment;
    m_progressMonitor = progressMonitor;
    setShellStyle(SWT.APPLICATION_MODAL);
    setBlockOnOpen(false);
  }

  protected String getDialogVariant() {
    return DIALOG_VARIANT;
  }

  @Override
  public void create() {
    super.create();

    getShell().setData(RwtUtility.EXTENDED_STYLE, SWT.POP_UP);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    getShell().setData(WidgetUtil.CUSTOM_VARIANT, getDialogVariant());

    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    container.setData(WidgetUtil.CUSTOM_VARIANT, getDialogVariant());

    GridLayout dialogAreaLayout = RwtLayoutUtility.createGridLayoutNoSpacing(1, false);
    dialogAreaLayout.marginTop = 12;
    dialogAreaLayout.marginBottom = 8;
    dialogAreaLayout.marginWidth = 12;
    container.setLayout(dialogAreaLayout);

    m_messageLabel = getUiEnvironment().getFormToolkit().createLabel(container, RwtUtility.getNlsText(Display.getCurrent(), "MobileBusyBlockingMessage"), SWT.WRAP | SWT.CENTER);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    m_messageLabel.setLayoutData(gridData);
    m_messageLabel.setData(WidgetUtil.CUSTOM_VARIANT, getDialogVariant());
    return container;
  }

  @Override
  protected Control createButtonBar(Composite parent) {
    Composite buttonArea = getUiEnvironment().getFormToolkit().createComposite(parent);
    buttonArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

    GridLayout layout = RwtLayoutUtility.createGridLayoutNoSpacing(1, false);
    layout.marginHeight = 8;
    buttonArea.setLayout(layout);

    Button cancelButton = createButton(buttonArea, RwtUtility.getNlsText(Display.getCurrent(), "MobileBusyBlockingAbort"), null);
    cancelButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

    return buttonArea;
  }

  protected Button createButton(Composite parent, String text, String iconId) {
    Button b = getUiEnvironment().getFormToolkit().createButton(parent, text, SWT.PUSH);
    if (iconId != null) {
      b.setImage(getUiEnvironment().getIcon(iconId));
    }
    b.addSelectionListener(new P_RwtButtonListener());
    return b;
  }

  private IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  private void handleUiButtonSelection() {
    m_progressMonitor.setCanceled(true);
  }

  private class P_RwtButtonListener extends SelectionAdapter {
    private static final long serialVersionUID = 1L;

    public P_RwtButtonListener() {
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      handleUiButtonSelection();
    }

  }

}
