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
package org.eclipse.scout.rt.ui.swing.form.fields.datefield;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.document.BasicDocumentFilter;
import org.eclipse.scout.rt.ui.swing.ext.JButtonEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextFieldEx;
import org.eclipse.scout.rt.ui.swing.ext.calendar.DateChooser;
import org.eclipse.scout.rt.ui.swing.ext.calendar.DateTimeChooser;
import org.eclipse.scout.rt.ui.swing.ext.calendar.TimeChooser;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;
import org.eclipse.scout.rt.ui.swing.window.popup.SwingScoutDropDownPopup;

public class SwingScoutDateField extends SwingScoutValueFieldComposite<IDateField> implements ISwingScoutDateField {
  private static final long serialVersionUID = 1L;

  private JButtonEx m_dateChooserButton;
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
    container.add(textField);
    // key mappings
    InputMap inputMap = textField.getInputMap(JTextField.WHEN_FOCUSED);
    inputMap.put(SwingUtility.createKeystroke("F2"), "dateChooser");
    inputMap.put(SwingUtility.createKeystroke("UP"), "nextDay");
    inputMap.put(SwingUtility.createKeystroke("DOWN"), "prevDay");
    inputMap.put(SwingUtility.createKeystroke("shift UP"), "nextMonth");
    inputMap.put(SwingUtility.createKeystroke("shift DOWN"), "prevMonth");
    inputMap.put(SwingUtility.createKeystroke("ctrl UP"), "nextYear");
    inputMap.put(SwingUtility.createKeystroke("ctrl DOWN"), "prevYear");
    ActionMap actionMap = textField.getActionMap();
    actionMap.put("dateChooser", new P_SwingDateChooserAction());
    actionMap.put("nextDay", new P_SwingDateShiftAction(0, 1));
    actionMap.put("prevDay", new P_SwingDateShiftAction(0, -1));
    actionMap.put("nextMonth", new P_SwingDateShiftAction(1, 1));
    actionMap.put("prevMonth", new P_SwingDateShiftAction(1, -1));
    actionMap.put("nextYear", new P_SwingDateShiftAction(2, 1));
    actionMap.put("prevYear", new P_SwingDateShiftAction(2, -1));
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
    //
    m_dateChooserButton = new JButtonEx();
    m_dateChooserButton.setFocusable(false);
    m_dateChooserButton.setHorizontalAlignment(SwingConstants.CENTER);
    m_dateChooserButton.setContentAreaFilled(false);
    m_dateChooserButton.setName("DropDownButton");
    m_dateChooserButton.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createButton1(getSwingEnvironment()));
    m_dateChooserButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            getSwingTextField().requestFocus();
            handleSwingDateChooserAction();
          }
        }
        );
    SwingLayoutUtility.setIconButtonSizes(getSwingEnvironment(), m_dateChooserButton);
    container.add(m_dateChooserButton);
    //
    return textField;
  }

  public JTextField getSwingTextField() {
    return (JTextField) getSwingField();
  }

  /*
   * scout properties
   */

  @Override
  protected void attachScout() {
    super.attachScout();
    IDateField f = getScoutObject();
    setDateIconIdFromScout(f.getDateIconId());
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    int swingAlign = SwingUtility.createHorizontalAlignment(scoutAlign);
    getSwingTextField().setHorizontalAlignment(swingAlign);
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    JTextComponent swingField = getSwingTextField();
    swingField.setText(s);
    swingField.setCaretPosition(0);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    if (m_dateChooserButton != null) {
      m_dateChooserButton.setEnabled(b);
    }
  }

  protected void setDateIconIdFromScout(String s) {
    if (m_dateChooserButton != null) {
      m_dateChooserButton.setIcon(getSwingEnvironment().getIcon(s));
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

  private void acceptProposalFromSwing(final Date newDate, final boolean includesTime) {
    // close old
    closePopup();
    if (newDate != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setDateFromUI(newDate, includesTime);
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

  protected boolean isDateChooserEnabled() {
    if (m_dateChooserButton != null) {
      return (m_dateChooserButton.isVisible() && m_dateChooserButton.isEnabled());
    }
    else {
      return false;
    }
  }

  protected void handleSwingDateChooserAction() {
    // close old
    closePopup();
    if (isDateChooserEnabled()) {
      //create chooser content and accept action
      JComponent popupContent;
      Action acceptAction;
      Date d = getScoutObject().getValue();
      if (d == null) d = new Date();
      if (getScoutObject().isHasTime()) {
        final DateTimeChooser dateTimeChooser = new DateTimeChooser();
        dateTimeChooser.getDateChooser().setDate(d);
        dateTimeChooser.getTimeChooser().setDate(d);
        //apply button
        dateTimeChooser.getApplyButton().addMouseListener(new MouseAdapter() {
          @Override
          public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
              dateTimeChooser.getTimeChooser().finishEditing();
              Date mergedDate = DateUtility.createDateTime(dateTimeChooser.getDateChooser().getDate(), dateTimeChooser.getTimeChooser().getDate());
              acceptProposalFromSwing(mergedDate, true);
            }
          }
        });
        //double click on clock
        dateTimeChooser.getTimeChooser().addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand() == TimeChooser.ACTION_CLOCK_DOUBLE_CLICKED) {
              Date mergedDate = DateUtility.createDateTime(dateTimeChooser.getDateChooser().getDate(), dateTimeChooser.getTimeChooser().getDate());
              acceptProposalFromSwing(mergedDate, true);
            }
          }
        });
        //double click on calendar
        dateTimeChooser.getDateChooser().getContainer().addMouseListener(new MouseAdapter() {
          @Override
          public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
              Date mergedDate = DateUtility.createDateTime(dateTimeChooser.getDateChooser().getDate(), dateTimeChooser.getTimeChooser().getDate());
              acceptProposalFromSwing(mergedDate, true);
            }
          }
        });
        popupContent = dateTimeChooser.getContainer();
        //
        acceptAction = new AbstractAction() {
          private static final long serialVersionUID = 1L;

          @Override
          public void actionPerformed(ActionEvent e) {
            Date mergedDate = DateUtility.createDateTime(dateTimeChooser.getDateChooser().getDate(), dateTimeChooser.getTimeChooser().getDate());
            acceptProposalFromSwing(mergedDate, true);
          }
        };
      }
      else {
        final DateChooser dateChooser = new DateChooser();
        dateChooser.setDate(d);
        dateChooser.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            acceptProposalFromSwing(dateChooser.getDate(), false);
          }
        });
        popupContent = dateChooser.getContainer();
        //
        acceptAction = new AbstractAction() {
          private static final long serialVersionUID = 1L;

          @Override
          public void actionPerformed(ActionEvent e) {
            acceptProposalFromSwing(dateChooser.getDate(), false);
          }
        };
      }
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
      popupContent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ENTER"), "enter");
      popupContent.getActionMap().put("enter", acceptAction);
      popupContent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ESCAPE"), "escape");
      popupContent.getActionMap().put("escape", escAction);
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

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IDateField.PROP_DATE_ICON_ID)) {
      setDateIconIdFromScout((String) newValue);
    }
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
   * Swing actions
   */

  private class P_SwingDateChooserAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public void actionPerformed(ActionEvent e) {
      handleSwingDateChooserAction();
    }
  }// end private class

  private class P_SwingDateShiftAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private int m_level;
    private int m_value;

    public P_SwingDateShiftAction(int level, int value) {
      m_level = level;
      m_value = value;
    }

    public void actionPerformed(ActionEvent e) {
      if (getSwingTextField().isVisible() && getSwingTextField().isEditable()) {
        final String newDisplayText = getSwingTextField().getText();
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            // store current (possibly changed) value
            if (!CompareUtility.equals(newDisplayText, getScoutObject().getDisplayText())) {
              getScoutObject().getUIFacade().setTextFromUI(newDisplayText);
            }
            getScoutObject().getUIFacade().fireDateShiftActionFromUI(m_level, m_value);
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }// end private class
}
