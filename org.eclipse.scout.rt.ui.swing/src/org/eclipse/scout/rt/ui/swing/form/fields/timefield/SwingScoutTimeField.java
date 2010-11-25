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
package org.eclipse.scout.rt.ui.swing.form.fields.timefield;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.form.fields.timefield.ITimeField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.document.BasicDocumentFilter;
import org.eclipse.scout.rt.ui.swing.ext.JButtonEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextFieldEx;
import org.eclipse.scout.rt.ui.swing.ext.calendar.TimeChooser;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;
import org.eclipse.scout.rt.ui.swing.window.popup.SwingScoutDropDownPopup;

public class SwingScoutTimeField extends SwingScoutValueFieldComposite<ITimeField> implements ISwingScoutTimeField {
  private static final long serialVersionUID = 1L;

  private JButtonEx m_timeChooserButton;
  // cache
  private SwingScoutDropDownPopup m_proposalPopup;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    //
    JTextComponent textField = createTextField(container);
    Document doc = textField.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new BasicDocumentFilter(60));
    }
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
    inputMap.put(SwingUtility.createKeystroke("F2"), "timeChooser");
    ActionMap actionMap = textField.getActionMap();
    actionMap.put("timeChooser", new P_SwingTimeChooserAction());
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
    m_timeChooserButton = new JButtonEx();
    m_timeChooserButton.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createButton1(getSwingEnvironment()));
    m_timeChooserButton.setFocusable(false);
    m_timeChooserButton.setHorizontalAlignment(SwingConstants.CENTER);
    m_timeChooserButton.setContentAreaFilled(false);
    m_timeChooserButton.setName("DropDownButton");
    m_timeChooserButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            getSwingTextField().requestFocus();
            handleSwingTimeChooserAction();
          }
        }
        );
    SwingLayoutUtility.setIconButtonSizes(getSwingEnvironment(), m_timeChooserButton);
    container.add(m_timeChooserButton);
    //
    return textField;
  }

  public JTextComponent getSwingTextField() {
    return (JTextComponent) getSwingField();
  }

  @Override
  protected void setForegroundFromScout(String scoutColor) {
    JComponent fld = getSwingField();
    if (fld != null && scoutColor != null && fld instanceof JTextComponent) {
      setDisabledTextColor(SwingUtility.createColor(scoutColor), (JTextComponent) fld);
    }
    super.setForegroundFromScout(scoutColor);
  }

  /*
   * scout properties
   */

  @Override
  protected void attachScout() {
    super.attachScout();
    ITimeField f = getScoutObject();
    setTimeIconIdFromScout(f.getTimeIconId());
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    JTextComponent swingField = getSwingTextField();
    swingField.setText(s);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    if (m_timeChooserButton != null) {
      m_timeChooserButton.setEnabled(b);
    }
  }

  protected void setTimeIconIdFromScout(String s) {
    if (m_timeChooserButton != null) {
      m_timeChooserButton.setIcon(getSwingEnvironment().getIcon(s));
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
    return true; // continue always
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

  protected boolean isTimeChooserEnabled() {
    if (m_timeChooserButton != null) {
      return (m_timeChooserButton.isVisible() && m_timeChooserButton.isEnabled());
    }
    else {
      return false;
    }
  }

  protected void handleSwingTimeChooserAction() {
    // close old
    closePopup();
    if (isTimeChooserEnabled()) {
      final TimeChooser cal = new TimeChooser();
      Double d = getScoutObject().getValue();
      if (d == null) {
        d = new Double(8.0 / 24.0);
      }
      cal.setTimeInMinutes((int) (d.doubleValue() * 24.0 * 60.0 + 0.5));
      //
      Action acceptAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
          acceptProposalFromSwing(cal.getTimeInMinutes());
        }
      };
      Action escAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
          closePopup();
        }
      };
      //add enter and escape keys to text field
      getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("ENTER"), "enter");
      getSwingTextField().getActionMap().put("enter", acceptAction);
      getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("ESCAPE"), "escape");
      getSwingTextField().getActionMap().put("escape", escAction);
      //add enter and escape keys to popup
      JComponent popupContent = cal.getContainer();
      popupContent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ENTER"), "enter");
      popupContent.getActionMap().put("enter", acceptAction);
      popupContent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ESCAPE"), "escape");
      popupContent.getActionMap().put("escape", escAction);
      cal.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (e.getActionCommand() == TimeChooser.ACTION_CLOCK_CLICKED || e.getActionCommand() == TimeChooser.ACTION_TEXT_TYPED) {
            acceptProposalFromSwing(cal.getTimeInMinutes());
          }
        }
      });
      //show popup (focusComponent must be null! to allow focus in popup window)
      m_proposalPopup = new SwingScoutDropDownPopup(getSwingEnvironment(), getSwingTextField(), null);
      m_proposalPopup.addSwingScoutViewListener(new SwingScoutViewListener() {
        public void viewChanged(SwingScoutViewEvent e) {
          if (e.getType() == SwingScoutViewEvent.TYPE_CLOSED) {
            closePopup();
          }
        }
      });
      m_proposalPopup.getSwingContentPane().add(popupContent);
      m_proposalPopup.openView();
    }
  }

  private void acceptProposalFromSwing(int newTimeInMinutes) {
    // close old
    closePopup();
    //
    Calendar c = Calendar.getInstance();
    c.set(Calendar.HOUR_OF_DAY, (newTimeInMinutes / 60) % 24);
    c.set(Calendar.MINUTE, newTimeInMinutes % 60);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    final String newTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
    if (newTime != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setTextFromUI(newTime);
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  private void closePopup() {
    if (m_proposalPopup != null) {
      m_proposalPopup.closeView();
      m_proposalPopup = null;
      getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).remove(SwingUtility.createKeystroke("ENTER"));
      getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).remove(SwingUtility.createKeystroke("ESCAPE"));
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ITimeField.PROP_TIME_ICON_ID)) {
      setTimeIconIdFromScout((String) newValue);
    }
  }

  /*
   * Swing actions
   */

  private class P_SwingTimeChooserAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public void actionPerformed(ActionEvent e) {
      handleSwingTimeChooserAction();
    }
  }// end private class

}
