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
package org.eclipse.scout.rt.ui.swing.form.fields.filechooserfield;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JButtonEx;
import org.eclipse.scout.rt.ui.swing.ext.JDropDownButton;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextFieldEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;

public class SwingScoutFileChooserField extends SwingScoutValueFieldComposite<IFileChooserField> implements ISwingScoutFileChooserField {
  private static final long serialVersionUID = 1L;

  private JDropDownButton m_dropDownButton;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    //
    JTextComponent textField = createTextField(container);
    Document doc = textField.getDocument();
    doc.addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        setInputDirty(true);
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        setInputDirty(true);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        setInputDirty(true);
      }
    });
    // key mappings
    InputMap inputMap = textField.getInputMap(JTextField.WHEN_FOCUSED);
    inputMap.put(SwingUtility.createKeystroke("F2"), "fileChooser");
    ActionMap actionMap = textField.getActionMap();
    actionMap.put("fileChooser", new P_SwingFileChooserAction());
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(textField);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  /**
   * Create and add the text field to the container.
   * <p>
   * May add additional components to the container.
   */
  protected JTextComponent createTextField(JComponent container) {
    JTextFieldEx textField = new JTextFieldEx();
    container.add(textField);
    JButtonEx pushButton = new JButtonEx();
    m_dropDownButton = new JDropDownButton(pushButton);
    m_dropDownButton.getPushButton().setContentAreaFilled(false);
    m_dropDownButton.getMenuButton().setContentAreaFilled(false);
    JButton menuButton = m_dropDownButton.getMenuButton();
    m_dropDownButton.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createButton1(getSwingEnvironment()));
    pushButton.setRequestFocusEnabled(false);
    pushButton.setFocusable(false);
    pushButton.setHorizontalAlignment(SwingConstants.CENTER);
    pushButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getSwingTextField().requestFocus();
        handleSwingFileChooserAction();
      }
    });
    menuButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handleSwingPopup((JComponent) e.getSource());
      }
    });
    SwingLayoutUtility.setIconButtonWithPopupSizes(getSwingEnvironment(), m_dropDownButton);
    container.add(m_dropDownButton);
    return textField;
  }

  public JTextComponent getSwingTextField() {
    return (JTextComponent) getSwingField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IFileChooserField f = getScoutObject();
    if (m_dropDownButton != null) {
      m_dropDownButton.getMenuButton().setEnabled(f.hasMenus());
    }
    setFileIconIdFromScout(f.getFileIconId());
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    JTextComponent swingField = getSwingTextField();
    swingField.setText(s);
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    if (getSwingTextField() instanceof JTextField) {
      int swingAlign = SwingUtility.createHorizontalAlignment(scoutAlign);
      ((JTextField) getSwingTextField()).setHorizontalAlignment(swingAlign);
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    if (m_dropDownButton != null) {
      m_dropDownButton.setEnabled(b);
    }
  }

  protected void setFileIconIdFromScout(String s) {
    if (m_dropDownButton != null) {
      Icon browseIcon = getSwingEnvironment().getIcon(s);
      m_dropDownButton.getPushButton().setIcon(browseIcon);
    }
  }

  @Override
  protected boolean handleSwingInputVerifier() {
    final String text = getSwingTextField().getText();
    // only handle if text has changed
    if (CompareUtility.equals(text, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return true;
    }
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, false);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setTextFromUI(text);
        result.setValue(b);
      }
    };
    JobEx job = getSwingEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    // end notify
    getSwingEnvironment().dispatchImmediateSwingJobs();
    return true;// continue always
  }

  @Override
  protected void handleSwingFocusGained() {
    super.handleSwingFocusGained();
    JTextComponent swingField = getSwingTextField();
    if (swingField.getDocument().getLength() > 0) {
      swingField.setCaretPosition(swingField.getDocument().getLength());
      swingField.moveCaretPosition(0);
    }
  }

  protected boolean isFileChooserEnabled() {
    if (m_dropDownButton != null) {
      return (m_dropDownButton.getPushButton().isVisible() && m_dropDownButton.getPushButton().isEnabled());
    }
    return false;
  }

  protected void handleSwingFileChooserAction() {
    if (isFileChooserEnabled()) {
      Runnable scoutJob = new Runnable() {
        @Override
        public void run() {
          IFileChooser fc = getScoutObject().getFileChooser();
          final File[] files = fc.startChooser();

          Runnable swingJob = new Runnable() {
            @Override
            public void run() {
              if (files != null && files.length > 0) {
                getSwingTextField().setText(files[0].getAbsolutePath());
                handleSwingInputVerifier();
              }
            }
          };
          if (getSwingEnvironment() != null) {
            getSwingEnvironment().invokeSwingLater(swingJob);
          }
        }
      };
      getSwingEnvironment().invokeScoutLater(scoutJob, 0);
    }
  }

  protected void handleSwingPopup(final JComponent target) {
    if (getScoutObject().hasMenus()) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          IMenu[] menus = getScoutObject().getUIFacade().firePopupFromUI();
          // call swing menu
          new SwingPopupWorker(getSwingEnvironment(), target, new Point(0, target.getHeight()), menus).enqueue();
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 5678);
      // end notify
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IFileChooserField.PROP_FILE_ICON_ID)) {
      setFileIconIdFromScout((String) newValue);
    }
  }

  /**
   * Swing action
   */
  private class P_SwingFileChooserAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public void actionPerformed(ActionEvent e) {
      handleSwingFileChooserAction();
    }
  }// end private class

}
