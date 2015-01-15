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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.menu.SwingScoutContextMenu;
import org.eclipse.scout.rt.ui.swing.basic.ColorUtility;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.document.BasicDocumentFilter;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.calendar.DateChooser;
import org.eclipse.scout.rt.ui.swing.ext.decoration.ContextMenuDecorationItem;
import org.eclipse.scout.rt.ui.swing.ext.decoration.DecorationGroup;
import org.eclipse.scout.rt.ui.swing.ext.decoration.DropDownDecorationItem;
import org.eclipse.scout.rt.ui.swing.ext.decoration.IDecorationGroup;
import org.eclipse.scout.rt.ui.swing.ext.decoration.JTextFieldWithDecorationIcons;
import org.eclipse.scout.rt.ui.swing.ext.decoration.JTextFieldWithDecorationIcons.Region;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutBasicFieldComposite;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;
import org.eclipse.scout.rt.ui.swing.window.popup.SwingScoutDropDownPopup;

public class SwingScoutDateField extends SwingScoutBasicFieldComposite<IDateField> implements ISwingScoutDateField {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutDateField.class);

  private boolean m_ignoreLabel;
  private boolean m_dateTimeCompositeMember;
  private String m_displayTextToVerify;
  // cache
  private SwingScoutDropDownPopup m_proposalPopup;

  private ContextMenuDecorationItem m_contextMenuMarker;
  private DropDownDecorationItem m_dropdownIcon;
  private SwingScoutContextMenu m_contextMenu;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  public boolean isIgnoreLabel() {
    return m_ignoreLabel;
  }

  public void setIgnoreLabel(boolean ignoreLabel) {
    m_ignoreLabel = ignoreLabel;
  }

  public boolean isDateTimeCompositeMember() {
    return m_dateTimeCompositeMember;
  }

  public void setDateTimeCompositeMember(boolean dateTimeCompositeMember) {
    m_dateTimeCompositeMember = dateTimeCompositeMember;
  }

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    if (!isIgnoreLabel()) {
      JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
      container.add(label);
      setSwingLabel(label);
    }
    JTextFieldWithDecorationIcons dateField = createDateField(container);

    Document doc = dateField.getDocument();
    addInputListenersForBasicField(dateField, doc);
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
    container.add(dateField);
    // key mappings
    InputMap inputMap = dateField.getInputMap(JTextField.WHEN_FOCUSED);
    inputMap.put(SwingUtility.createKeystroke("F2"), "dateChooser");
    inputMap.put(SwingUtility.createKeystroke("UP"), "nextDay");
    inputMap.put(SwingUtility.createKeystroke("DOWN"), "prevDay");
    inputMap.put(SwingUtility.createKeystroke("shift UP"), "nextMonth");
    inputMap.put(SwingUtility.createKeystroke("shift DOWN"), "prevMonth");
    inputMap.put(SwingUtility.createKeystroke("ctrl UP"), "nextYear");
    inputMap.put(SwingUtility.createKeystroke("ctrl DOWN"), "prevYear");
    ActionMap actionMap = dateField.getActionMap();
    actionMap.put("dateChooser", new P_SwingDateChooserAction());
    actionMap.put("nextDay", new P_SwingDateShiftAction(0, 1));
    actionMap.put("prevDay", new P_SwingDateShiftAction(0, -1));
    actionMap.put("nextMonth", new P_SwingDateShiftAction(1, 1));
    actionMap.put("prevMonth", new P_SwingDateShiftAction(1, -1));
    actionMap.put("nextYear", new P_SwingDateShiftAction(2, 1));
    actionMap.put("prevYear", new P_SwingDateShiftAction(2, -1));
    //
    setSwingContainer(container);
    setSwingField(dateField);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  /**
   * Create and add the text field to the container.
   * <p>
   * May add additional components to the container.
   */
  protected JTextFieldWithDecorationIcons createDateField(JComponent container) {
    JTextFieldWithDecorationIcons textField = new JTextFieldWithDecorationIcons();
    initializeDateField(textField);
    container.add(textField);
    return textField;

  }

  protected void initializeDateField(JTextFieldWithDecorationIcons textField) {
    textField.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        JTextFieldWithDecorationIcons text = (JTextFieldWithDecorationIcons) e.getComponent();
        // ensure click not on decorations
        if (text.getRegion(e.getPoint()) == Region.Text && e.getButton() == MouseEvent.BUTTON1) {
          handleSwingDateChooserAction();
        }
      }
    });
    IDecorationGroup decorationGroup = new DecorationGroup(textField, getSwingEnvironment());
    // context menu marker
    m_contextMenuMarker = new ContextMenuDecorationItem(getScoutObject().getContextMenu(), textField, getSwingEnvironment());
    m_contextMenuMarker.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        m_contextMenu.showSwingPopup(e.getX(), e.getY(), false);
      }
    });
    decorationGroup.addDecoration(m_contextMenuMarker);

    // dropdown decoration
    m_dropdownIcon = new DropDownDecorationItem(textField, getSwingEnvironment());
    m_dropdownIcon.setIconGroup(new IconGroup(getSwingEnvironment(), AbstractIcons.DateFieldDate));
    m_dropdownIcon.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
          m_contextMenu.showSwingPopup(e.getX(), e.getY(), false);
        }
        else {
          getSwingDateField().requestFocus();
          handleSwingDateChooserAction();
        }
      }
    });
    decorationGroup.addDecoration(m_dropdownIcon);

    textField.setDecorationIcon(decorationGroup);
  }

  protected void installContextMenu() {
    m_contextMenuVisibilityListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
          m_contextMenuMarker.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
        }
      }
    };
    getScoutObject().getContextMenu().addPropertyChangeListener(m_contextMenuVisibilityListener);
    m_contextMenuMarker.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    m_contextMenu = SwingScoutContextMenu.installContextMenuWithSystemMenus(getSwingDateField(), getScoutObject().getContextMenu(), getSwingEnvironment());
  }

  protected void uninstallContextMenu() {
    if (m_contextMenuVisibilityListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(m_contextMenuVisibilityListener);
      m_contextMenuVisibilityListener = null;
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    installContextMenu();
  }

  @Override
  protected void detachScout() {
    if (m_contextMenuMarker != null) {
      m_contextMenuMarker.destroy();
    }
    uninstallContextMenu();
    super.detachScout();
  }

  public JTextFieldWithDecorationIcons getSwingDateField() {
    return (JTextFieldWithDecorationIcons) getSwingField();
  }

  public DropDownDecorationItem getDropdownIcon() {
    return m_dropdownIcon;
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_dropdownIcon.setEnabled(b);
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    int swingAlign = SwingUtility.createHorizontalAlignment(scoutAlign);
    getSwingDateField().setHorizontalAlignment(swingAlign);
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    m_displayTextToVerify = s;
    IDateField f = getScoutObject();
    Date value = f.getValue();
    JTextComponent dateField = getSwingDateField();
    if (value == null || !f.isHasTime()) {
      //only date field
      dateField.setText(m_displayTextToVerify);
      dateField.setCaretPosition(0);
      return;
    }
    DateFormat format = f.getIsolatedDateFormat();
    m_displayTextToVerify = format.format(value);
    dateField.setText(m_displayTextToVerify);
    dateField.setCaretPosition(0);
  }

  @Override
  protected boolean handleSwingInputVerifier() {
    final String text = getSwingDateField().getText();
    // only handle if text has changed
    if (CompareUtility.equals(text, m_displayTextToVerify) && (isDateTimeCompositeMember() || getScoutObject().getErrorStatus() == null)) {
      return true;
    }
    m_displayTextToVerify = text;
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, false);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setDateTextFromUI(text);
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

  private void acceptProposalFromSwing(final Date newDate) {
    // close old
    closePopup();
    if (newDate != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setDateFromUI(newDate);
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
      getSwingDateField().getInputMap(JComponent.WHEN_FOCUSED).remove(SwingUtility.createKeystroke("ENTER"));
      getSwingDateField().getInputMap(JComponent.WHEN_FOCUSED).remove(SwingUtility.createKeystroke("ESCAPE"));
    }
  }

  protected boolean isDateChooserEnabled() {
    return getSwingDateField() != null && getSwingDateField().isEditable();
  }

  protected void handleSwingDateChooserAction() {
    // close old
    closePopup();
    if (isDateChooserEnabled()) {
      //create chooser content and accept action
      JComponent popupContent;
      Action acceptAction;
      Date d = getScoutObject().getValue();
      if (d == null) {
        d = new Date();
      }
      //create date chooser
      final DateChooser dateChooser = new DateChooser();
      dateChooser.setDate(d);
      dateChooser.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          acceptProposalFromSwing(dateChooser.getDate());
        }
      });
      popupContent = dateChooser.getContainer();
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

        @Override
        public void actionPerformed(ActionEvent e) {
          closePopup();
        }
      };
      JTextField textField = getSwingDateField();
      //add enter and escape keys to text field
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
      m_proposalPopup = new SwingScoutDropDownPopup(getSwingEnvironment(), textField, textField);
      m_proposalPopup.makeNonFocusable();
      m_proposalPopup.addSwingScoutViewListener(new SwingScoutViewListener() {
        @Override
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
      setDisabledTextColor(ColorUtility.createColor(scoutColor), (JTextComponent) fld);
    }
    super.setForegroundFromScout(scoutColor);
  }

  @Override
  protected void setSelectionFromSwing() {
    //Nothing to do: Selection is not stored in model for DateField.
  }

  @Override
  protected boolean isSelectAllOnFocusInScout() {
    return true; //No such property in Scout for DateField.
  }

  /*
   * Swing actions
   */

  private class P_SwingDateChooserAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    @Override
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

    @Override
    public void actionPerformed(ActionEvent e) {
      closePopup();
      if (getSwingDateField().isVisible() && getSwingDateField().isEditable()) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireDateShiftActionFromUI(m_level, m_value);
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }
}
