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
package org.eclipse.scout.rt.ui.rap.login.internal;

import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginDialog extends Dialog {

  private static final long serialVersionUID = 1L;
  private AuthStatus m_status;
  private Button m_saveCheckbox;
  private Button m_okButton;

  public LoginDialog(Shell parent, int style, AuthStatus status) {
    super(parent, style | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    setText(RwtUtility.getNlsText(Display.getCurrent(), "Login"));
    m_status = status;
  }

  public void open() {
    Shell parent = getParent();
    parent.setText(getText());
    createContents(parent);
    updateOkButton();
    parent.setLocation(parent.getBounds().width / 2, parent.getBounds().height / 2);
    parent.pack();
    parent.open();
    Display display = getParent().getDisplay();
    while (!parent.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
  }

  private void createContents(final Shell parent) {

    GridLayout gridLayout = new GridLayout(3, true);
    GridData data;
    gridLayout.marginLeft = 10;
    gridLayout.marginRight = 10;
    gridLayout.marginTop = 10;
    gridLayout.marginBottom = 10;
    gridLayout.makeColumnsEqualWidth = true;

    parent.setLayout(gridLayout);

    Label urlLabel = new Label(parent, SWT.NONE);
    data = new GridData(GridData.HORIZONTAL_ALIGN_END);
    data.horizontalSpan = 1;
    urlLabel.setLayoutData(data);

    Label url = new Label(parent, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING);
    data.horizontalSpan = 2;
    data.widthHint = 120;
    url.setLayoutData(data);
    if (m_status.isProxy()) {
      url.setText("PROXY " + m_status.getUrl().getHost());
    }
    else {
      url.setText(m_status.getUrl().getHost());
    }
    Label userLabel = new Label(parent, SWT.NONE);
    userLabel.setText(RwtUtility.getNlsText(Display.getCurrent(), "Username")); //$NON-NLS-1$
    data = new GridData(GridData.HORIZONTAL_ALIGN_END);
    data.horizontalSpan = 1;
    userLabel.setLayoutData(data);

    final Text user = new Text(parent, SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING);
    data.horizontalSpan = 2;
    data.widthHint = 120;
    user.setLayoutData(data);
    if (m_status.getUsername() != null) {
      user.setText(m_status.getUsername());
    }
    user.addModifyListener(new ModifyListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void modifyText(ModifyEvent e) {
        m_status.setUsername(user.getText());
        updateOkButton();
      }
    });

    Label passLabel = new Label(parent, SWT.NONE);
    passLabel.setText(RwtUtility.getNlsText(Display.getCurrent(), "Password")); //$NON-NLS-1$
    data = new GridData(GridData.HORIZONTAL_ALIGN_END);
    data.horizontalSpan = 1;
    passLabel.setLayoutData(data);

    final Text pass = new Text(parent, SWT.BORDER | SWT.PASSWORD);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING);
    data.horizontalSpan = 2;
    data.widthHint = 120;
    pass.setLayoutData(data);
    if (m_status.getPassword() != null) {
      pass.setText(m_status.getPassword());
    }
    pass.addModifyListener(new ModifyListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void modifyText(ModifyEvent e) {
        m_status.setPassword(pass.getText());
        updateOkButton();
      }
    });

    if (InternalNetAuthenticator.NET_AUTHENTICATION_CACHE_ENABLED) {
      Label saveLabel = new Label(parent, SWT.NONE);
      data = new GridData(GridData.HORIZONTAL_ALIGN_END);
      data.horizontalSpan = 1;
      saveLabel.setLayoutData(data);

      m_saveCheckbox = new Button(parent, SWT.CHECK);
      m_saveCheckbox.setText(RwtUtility.getNlsText(Display.getCurrent(), "SavePassword"));
      m_saveCheckbox.setSelection(false);
      m_saveCheckbox.setVisible(m_status.isAllowSavePassword());
      data = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING);
      data.horizontalSpan = 2;
      data.widthHint = 120;
      m_saveCheckbox.setLayoutData(data);
    }

    m_okButton = new Button(parent, SWT.PUSH);
    m_okButton.setText(RwtUtility.getNlsText(Display.getCurrent(), "Ok")); //$NON-NLS-1$
    data = new GridData(SWT.FILL | GridData.HORIZONTAL_ALIGN_END);
    data.horizontalSpan = 2;
    data.widthHint = 60;
    m_okButton.setLayoutData(data);
    m_okButton.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent event) {
        if (m_saveCheckbox != null) {
          m_status.setSavePassword(m_saveCheckbox.getSelection());
        }
        m_status.setOk();
        m_status = null;
        parent.close();
      }
    });
    m_okButton.setEnabled(false);

    Button cancel = new Button(parent, SWT.PUSH);
    cancel.setText(RwtUtility.getNlsText(Display.getCurrent(), "Cancel")); //$NON-NLS-1$
    data = new GridData(SWT.FILL | GridData.HORIZONTAL_ALIGN_END);
    data.horizontalSpan = 1;
    data.widthHint = 60;
    cancel.setLayoutData(data);
    cancel.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent event) {
        m_status.setCancel();
        m_status = null;
        parent.close();
      }
    });

    parent.setDefaultButton(m_okButton);
  }

  private void updateOkButton() {
    if (m_status.getUsername() != null && m_status.getUsername().length() > 0 && m_status.getPassword() != null && m_status.getPassword().length() > 0) {
      m_okButton.setEnabled(true);
    }
    else {
      m_okButton.setEnabled(false);
    }
  }
}
