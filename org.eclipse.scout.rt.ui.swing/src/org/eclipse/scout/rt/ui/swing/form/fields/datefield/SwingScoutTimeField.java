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
import java.text.DateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.document.BasicDocumentFilter;
import org.eclipse.scout.rt.ui.swing.ext.IDropDownButtonListener;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextFieldWithTransparentIcon;
import org.eclipse.scout.rt.ui.swing.ext.calendar.TimeChooser;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;
import org.eclipse.scout.rt.ui.swing.window.popup.SwingScoutDropDownPopup;

/**
 * time field in combination with a date field to create a date/time field
 */
public class SwingScoutTimeField extends SwingScoutValueFieldComposite<IDateField> implements ISwingScoutDateField {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutTimeField.class);

  private boolean m_ignoreLabel;
  // cache
  private SwingScoutDropDownPopup m_proposalPopup;

  public void setIgnoreLabel(boolean ignoreLabel) {
    m_ignoreLabel = ignoreLabel;
  }

  public boolean isIgnoreLabel() {
    return m_ignoreLabel;
  }

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    if (!isIgnoreLabel()) {
      JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
      container.add(label);
      setSwingLabel(label);
    }
    JTextField timeField = createTimeField(container);
    Document doc = timeField.getDocument();
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
    container.add(timeField);
    // key mappings
    InputMap inputMap = timeField.getInputMap(JTextField.WHEN_FOCUSED);
    inputMap.put(SwingUtility.createKeystroke("F2"), "timeChooser");
    inputMap.put(SwingUtility.createKeystroke("UP"), "nextQuarterHour");
    inputMap.put(SwingUtility.createKeystroke("DOWN"), "prevQuarterHour");
    inputMap.put(SwingUtility.createKeystroke("shift UP"), "nextHour");
    inputMap.put(SwingUtility.createKeystroke("shift DOWN"), "prevHour");
    ActionMap actionMap = timeField.getActionMap();
    actionMap.put("timeChooser", new P_SwingTimeChooserAction());
    actionMap.put("nextQuarterHour", new P_SwingTimeShiftAction(0, 1));
    actionMap.put("prevQuarterHour", new P_SwingTimeShiftAction(0, -1));
    actionMap.put("nextHour", new P_SwingTimeShiftAction(1, 1));
    actionMap.put("prevHour", new P_SwingTimeShiftAction(1, -1));
    //
    setSwingContainer(container);
    setSwingField(timeField);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  /**
   * Create and add the text field to the container.
   * <p>
   * May add additional components to the container.
   */
  protected JTextField createTimeField(JComponent container) {
    JTextFieldWithTransparentIcon textField = new JTextFieldWithTransparentIcon();
    textField.setIconGroup(new IconGroup(getSwingEnvironment(), AbstractIcons.DateFieldTime));
    container.add(textField);
    textField.addDropDownButtonListener(new IDropDownButtonListener() {
      @Override
      public void iconClicked(Object source) {
        getSwingTimeField().requestFocus();
        handleSwingTimeChooserAction();
      }

      @Override
      public void menuClicked(Object source) {
      }
    });
    return textField;
  }

  public JTextField getSwingTimeField() {
    return (JTextField) getSwingField();
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    int swingAlign = SwingUtility.createHorizontalAlignment(scoutAlign);
    getSwingTimeField().setHorizontalAlignment(swingAlign);
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    IDateField f = getScoutObject();
    Date value = f.getValue();
    JTextComponent textField = getSwingTimeField();
    if (value == null) {
      textField.setText(s);
      textField.setCaretPosition(0);
      return;
    }
    DateFormat format = f.getIsolatedTimeFormat();
    if (format != null) {
      textField.setText(format.format(value));
      textField.setCaretPosition(0);
    }
  }

  @Override
  protected boolean handleSwingInputVerifier() {
    final String text = getSwingTimeField().getText();
    // only handle if text has changed
    if (CompareUtility.equals(text, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return true;
    }
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, false);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setTimeTextFromUI(text);
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
    JTextComponent swingField = getSwingTimeField();
    if (swingField.getDocument().getLength() > 0) {
      swingField.setCaretPosition(swingField.getDocument().getLength());
      swingField.moveCaretPosition(0);
    }
  }

  private void acceptProposalFromSwing(final Date newDate) {
    // close old
    closePopup();
    if (newDate != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setTimeFromUI(newDate);
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
      getSwingTimeField().getInputMap(JComponent.WHEN_FOCUSED).remove(SwingUtility.createKeystroke("ENTER"));
      getSwingTimeField().getInputMap(JComponent.WHEN_FOCUSED).remove(SwingUtility.createKeystroke("ESCAPE"));
    }
  }

  protected boolean isTimeChooserEnabled() {
    return getSwingTimeField() != null && getSwingTimeField().isEnabled();
  }

  protected void handleSwingTimeChooserAction() {
    // close old
    closePopup();
    if (isTimeChooserEnabled()) {
      //create chooser content and accept action
      JComponent popupContent;
      Action acceptAction;
      Date d = getScoutObject().getValue();
      if (d == null) d = new Date();
      //create date chooser
      final TimeChooser timeChooser = new TimeChooser();
      timeChooser.setTime(d);
      timeChooser.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          acceptProposalFromSwing(timeChooser.getTime());
        }
      });
      popupContent = timeChooser.getContainer();
      //
      acceptAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          closePopup();
          //save text that was entered, NOT popup selection
          handleSwingInputVerifier();
        }
      };
      Action escAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
          closePopup();
        }
      };
      //add enter and escape keys to text field
      JTextField textField = getSwingTimeField();
      textField.getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("ENTER"), "enter");
      textField.getActionMap().put("enter", acceptAction);
      textField.getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("ESCAPE"), "escape");
      textField.getActionMap().put("escape", escAction);
      //add enter and escape keys to popup
      popupContent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ENTER"), "enter");
      popupContent.getActionMap().put("enter", acceptAction);
      popupContent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ESCAPE"), "escape");
      popupContent.getActionMap().put("escape", escAction);
      //show popup (focusComponent must be null! to allow focus in popup window)
      m_proposalPopup = new SwingScoutDropDownPopup(getSwingEnvironment(), textField, textField, 100);
      m_proposalPopup.makeNonFocusable();
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

  private class P_SwingTimeChooserAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public void actionPerformed(ActionEvent e) {
      handleSwingTimeChooserAction();
    }
  }// end private class

  private class P_SwingTimeShiftAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private int m_level;
    private int m_value;

    public P_SwingTimeShiftAction(int level, int value) {
      m_level = level;
      m_value = value;
    }

    public void actionPerformed(ActionEvent e) {
      closePopup();
      if (getSwingTimeField().isVisible() && getSwingTimeField().isEditable()) {
        final String newDisplayText = getSwingTimeField().getText();
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            // store current (possibly changed) value
            if (!CompareUtility.equals(newDisplayText, getScoutObject().getDisplayText())) {
              getScoutObject().getUIFacade().setTimeTextFromUI(newDisplayText);
            }
            getScoutObject().getUIFacade().fireTimeShiftActionFromUI(m_level, m_value);
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }// end private class
}
