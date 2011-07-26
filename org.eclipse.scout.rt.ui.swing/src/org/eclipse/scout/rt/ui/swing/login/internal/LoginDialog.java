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
package org.eclipse.scout.rt.ui.swing.login.internal;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eclipse.scout.rt.ui.swing.ext.FlowLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JDialogEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;

public class LoginDialog extends JDialogEx {
  private static final long serialVersionUID = 1L;

  private JTextField m_userField;
  private JPasswordField m_passField;
  private JButton m_okButton;
  private JCheckBox m_saveCheckBox;
  //
  private AuthStatus m_status;

  public LoginDialog(AuthStatus status) {
    super();
    m_status = status;
    setTitle(UIManager.getString("LoginDialog.title"));
    createContents();
    pack();
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        // set initial focus
        setInitialFocus();
      }
    });
  }

  protected void createContents() {
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(new GridBagLayout());
    contentPane.setBorder(new EmptyBorder(10, 5, 10, 5));
    Insets insets = new Insets(1, 2, 1, 2);
    JLabel infoField = new JLabel();
    contentPane.add(infoField, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
    if (m_status.isProxy()) {
      infoField.setText("PROXY " + m_status.getUrl().getHost());
    }
    else {
      infoField.setText(m_status.getUrl().getHost());
    }
    JLabel userLabel = new JLabel();
    contentPane.add(userLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
    userLabel.setText(UIManager.getString("LoginDialog.username"));
    m_userField = new JTextField(10);
    contentPane.add(m_userField, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
    if (m_status.getUsername() != null) {
      m_userField.setText(m_status.getUsername());
    }
    m_userField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void changedUpdate(DocumentEvent e) {
        m_status.setUsername(m_userField.getText());
        updateOkButton();
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        m_status.setUsername(m_userField.getText());
        updateOkButton();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        m_status.setUsername(m_userField.getText());
        updateOkButton();
      }
    });
    m_userField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        m_userField.selectAll();
        m_userField.setSelectionStart(0);
      }
    });

    JLabel passLabel = new JLabel();
    contentPane.add(passLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
    passLabel.setText(UIManager.getString("LoginDialog.password"));

    m_passField = new JPasswordField(10);
    contentPane.add(m_passField, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
    if (m_status.getPassword() != null) {
      m_passField.setText(new String(m_status.getPassword()));
    }
    m_passField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void changedUpdate(DocumentEvent e) {
        m_status.setPassword(new String(m_passField.getPassword()));
        updateOkButton();
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        m_status.setPassword(new String(m_passField.getPassword()));
        updateOkButton();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        m_status.setPassword(new String(m_passField.getPassword()));
        updateOkButton();
      }
    });
    m_passField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        m_passField.selectAll();
        m_passField.setSelectionStart(0);
      }
    });

    if (InternalNetAuthenticator.NET_AUTHENTICATION_CACHE_ENABLED) {
      /*
       * ticket 80881: default false
       */
      m_saveCheckBox = new JCheckBox(UIManager.getString("LoginDialog.savePassword"), false);
      m_saveCheckBox.setOpaque(false);
      contentPane.add(m_saveCheckBox, new GridBagConstraints(1, 3, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
    }

    contentPane.add(new JPanelEx(), new GridBagConstraints(0, 10, 2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.BOTH, insets, 0, 0));

    JPanel buttonPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.RIGHT, 2, 1));
    contentPane.add(buttonPanel, new GridBagConstraints(0, 11, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
    m_okButton = new JButton();
    buttonPanel.add(m_okButton);
    m_okButton.setText(UIManager.getString("LoginDialog.ok"));
    m_okButton.setEnabled(false);
    m_okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // accept
        doOk();
      }
    });

    JButton cancel = new JButton();
    buttonPanel.add(cancel);
    cancel.setText(UIManager.getString("LoginDialog.cancel"));
    cancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        m_status.setUsername(null);
        m_status.setPassword(null);
        // cancel
        doCancel();
      }
    });
    updateOkButton();
  }

  private void doOk() {
    m_status.setOk();
    if (m_saveCheckBox != null) {
      m_status.setSavePassword(m_saveCheckBox.isSelected());
    }
    setVisible(false);
  }

  private void doCancel() {
    m_status.setCancel();
    setVisible(false);
  }

  public void setInitialFocus() {
    if (m_userField.getText() != null && m_userField.getText().length() == 0) {
      m_userField.requestFocus();
    }
    else {
      m_passField.requestFocus();
    }
    getRootPane().setDefaultButton(m_okButton);
  }

  private void updateOkButton() {
    if (m_status.getUsername() != null && m_status.getUsername().length() > 0 && m_status.getPassword() != null && m_status.getPassword().length() > 0) m_okButton.setEnabled(true);
    else m_okButton.setEnabled(false);
  }
}
