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
package org.eclipse.scout.rt.ui.swing.form.fields.smartfield;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldProposalForm;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.menu.SwingScoutContextMenu;
import org.eclipse.scout.rt.ui.swing.basic.ColorUtility;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.document.BasicDocumentFilter;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTableEx;
import org.eclipse.scout.rt.ui.swing.ext.JTreeEx;
import org.eclipse.scout.rt.ui.swing.ext.decoration.ContextMenuDecorationItem;
import org.eclipse.scout.rt.ui.swing.ext.decoration.DecorationGroup;
import org.eclipse.scout.rt.ui.swing.ext.decoration.DropDownDecorationItem;
import org.eclipse.scout.rt.ui.swing.ext.decoration.IDecorationGroup;
import org.eclipse.scout.rt.ui.swing.ext.decoration.JTextFieldWithDecorationIcons;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swing.window.popup.SwingScoutDropDownPopup;
import org.eclipse.scout.rt.ui.swing.window.popup.SwingScoutPopup;

/**
 * Proposal support feature: typing up/down key selects up and down in proposal
 * popup typing space just AFTER up/down key selects the currently selected row
 * in the proposal popup
 */
public class SwingScoutSmartField extends SwingScoutValueFieldComposite<IContentAssistField<?, ?>> implements ISwingScoutSmartField {
  private static final long serialVersionUID = 1L;

  // proposal support
  private SwingScoutDropDownPopup m_proposalPopup;
  private P_PendingProposalJob m_pendingProposalJob;
  private Object m_pendingProposalJobLock;

  private ContextMenuDecorationItem m_contextMenuMarker;
  private SwingScoutContextMenu m_contextMenu;
  private DropDownDecorationItem m_dropdownIcon;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  @Override
  @SuppressWarnings("serial")
  protected void initializeSwing() {
    m_pendingProposalJobLock = new Object();
    //
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    JTextComponent textField = createTextField(container);
    Document doc = textField.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new P_SwingDocumentFilter());
    }
    doc.addDocumentListener(
        new DocumentListener() {
          @Override
          public void changedUpdate(DocumentEvent e) {
            handleSwingDocumentChanged(e);
          }

          @Override
          public void insertUpdate(DocumentEvent e) {
            handleSwingDocumentChanged(e);
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            handleSwingDocumentChanged(e);
          }
        }
        );
    //
    installSwingKeyStrokeDelegate(textField, SwingUtility.createKeystroke("UP"), "upArrow");
    installSwingKeyStrokeDelegate(textField, SwingUtility.createKeystroke("DOWN"), "downArrow");
    installSwingKeyStrokeDelegate(textField, SwingUtility.createKeystroke("PAGE_UP"), "pageUp");
    installSwingKeyStrokeDelegate(textField, SwingUtility.createKeystroke("PAGE_DOWN"), "pageDown");
    //
    // key mappings
    InputMap inputMap = textField.getInputMap(JTextField.WHEN_FOCUSED);
    inputMap.put(SwingUtility.createKeystroke("F2"), "smartChooser");
    ActionMap actionMap = textField.getActionMap();
    actionMap.put(
        "smartChooser",
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleSwingSmartChooserAction(0);
          }
        }
        );
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
    JTextFieldWithDecorationIcons textField = new JTextFieldWithDecorationIcons();
    container.add(textField);
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

    // smart chooser decoration
    m_dropdownIcon = new DropDownDecorationItem(textField, getSwingEnvironment());
    m_dropdownIcon.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
          m_contextMenu.showSwingPopup(e.getX(), e.getY(), false);
        }
        else {
          getSwingTextField().requestFocus();
          handleSwingSmartChooserAction(0);
        }
      }
    });
    decorationGroup.addDecoration(m_dropdownIcon);

    textField.setDecorationIcon(decorationGroup);
    return textField;

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
    m_contextMenu = SwingScoutContextMenu.installContextMenuWithSystemMenus(getSwingTextField(), getScoutObject().getContextMenu(), getSwingEnvironment());
  }

  protected void uninstallContextMenu() {
    if (m_contextMenuVisibilityListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(m_contextMenuVisibilityListener);
      m_contextMenuVisibilityListener = null;
    }
  }

  @Override
  public JTextComponent getSwingTextField() {
    return (JTextComponent) getSwingField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IContentAssistField f = getScoutObject();
    setIconIdFromScout(f.getIconId());
    setProposalFormFromScout(f.getProposalForm());
    installContextMenu();
  }

  @Override
  protected void detachScout() {
    hideProposalPopup();
    if (m_contextMenuMarker != null) {
      m_contextMenuMarker.destroy();
    }
    uninstallContextMenu();
    super.detachScout();
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    if (getSwingTextField() instanceof JTextField) {
      int swingAlign = SwingUtility.createHorizontalAlignment(scoutAlign);
      ((JTextField) getSwingTextField()).setHorizontalAlignment(swingAlign);
    }
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    JTextComponent swingField = getSwingTextField();
    if (!CompareUtility.equals(swingField.getText(), s)) {
      swingField.setText(s);
      // ticket 77424
      if (swingField.hasFocus() && swingField.getDocument().getLength() > 0) {
        swingField.setCaretPosition(swingField.getDocument().getLength());
        swingField.moveCaretPosition(0);
      }
      else {
        swingField.setCaretPosition(0);
      }
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_dropdownIcon.setEnabled(b);
  }

  protected void setIconIdFromScout(String iconId) {
    m_dropdownIcon.setIconGroup(new IconGroup(getSwingEnvironment(), iconId));
  }

  protected void setProposalFormFromScout(IContentAssistFieldProposalForm form) {
    if (form != null) {
      showProposalPopup(form);
    }
    else {
      hideProposalPopup();
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

  protected void handleSwingDocumentChanged(DocumentEvent e) {
    setInputDirty(true);
    if (getUpdateSwingFromScoutLock().isReleased()) {
      if (getSwingTextField().isShowing() && getSwingTextField().isFocusOwner()) {
        String text = getSwingTextField().getText();
        if (text == null || text.length() == 0) {
          // allow empty field without proposal
          if (m_proposalPopup != null) {
            requestProposalSupportFromSwing(text, false, 0);
          }
        }
        else {
          requestProposalSupportFromSwing(text, false, m_proposalPopup != null ? 200 : 0);
        }
      }
    }
  }

  /**
   * install a key stroke delegate from the textfield to the popup table/tree
   */
  protected void installSwingKeyStrokeDelegate(JComponent textField, final KeyStroke k, String name) {
    textField.getInputMap(JComponent.WHEN_FOCUSED).put(k, name);
    textField.getActionMap().put(name, new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        if (getSwingTextField().isVisible() && getSwingTextField().isEditable()) {
          if (m_proposalPopup == null) {
            requestProposalSupportFromSwing(IContentAssistField.BROWSE_ALL_TEXT, true, 0);
          }
          else {
            JComponent c = SwingUtility.findChildComponent(m_proposalPopup.getSwingContentPane(), JTable.class);
            if (c == null) {
              c = SwingUtility.findChildComponent(m_proposalPopup.getSwingContentPane(), JTree.class);
            }
            if (c != null) {
              ActionListener a = c.getActionForKeyStroke(k);
              if (a != null) {
                a.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, null));
              }
            }
          }
        }
      }
    });
  }

  private void requestProposalSupportFromSwing(String text, boolean selectCurrentValue, long initialDelay) {
    synchronized (m_pendingProposalJobLock) {
      if (m_pendingProposalJob == null) {
        m_pendingProposalJob = new P_PendingProposalJob();
      }
      else {
        m_pendingProposalJob.cancel();
      }
      m_pendingProposalJob.update(text, selectCurrentValue);
      m_pendingProposalJob.schedule(initialDelay);
    }
  }

  private void acceptProposalFromSwing() {
    synchronized (m_pendingProposalJobLock) {
      if (m_pendingProposalJob != null) {
        m_pendingProposalJob.cancel();
        m_pendingProposalJob = null;
      }
    }
    final String text = getSwingTextField().getText();
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setTextFromUI(text);
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  private void showProposalPopup(final IContentAssistFieldProposalForm form) {
    setInputDirty(true);

    // check is needed, because for inline editing tables, the swing field might be already disposed
    if (!getSwingField().isDisplayable()) {
      getSwingEnvironment().invokeScoutLater(new Runnable() {
        @Override
        public void run() {
          /*
           * The display text of the smartfield must be set to the initial value,
           * because otherwise the typed (invalid) text is still there. (same behavior as for smartfields in forms directly)
           * In order to work, set to null is required because smartfield will not
           * refresh display text if value is null
           */
          getScoutObject().setDisplayText(null);
          getScoutObject().refreshDisplayText();

          /*
           * Unregister proposal form from UI
           * If not, accessing the same smartfield will not open the proposal form again
           */
          getScoutObject().getUIFacade().unregisterProposalFormFromUI(form);
        }
      }, 0);
      return;
    }

    // close old
    if (m_proposalPopup != null) {
      if (m_proposalPopup.isVisible()) {
        m_proposalPopup.closeView();
      }
      m_proposalPopup = null;
    }
    // show new
    getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("ENTER"), "enter");
    getSwingTextField().getActionMap().put("enter", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        acceptProposalFromSwing();
      }
    });
    getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("ESCAPE"), "escape");
    getSwingTextField().getActionMap().put("escape", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        if (m_proposalPopup != null) {
          m_proposalPopup.closeView();
        }
      }
    });
    //
    m_proposalPopup = new SwingScoutDropDownPopup(getSwingEnvironment(), getSwingTextField(), getSwingTextField());
    getSwingEnvironment().createForm(m_proposalPopup, form);
    m_proposalPopup.makeNonFocusable();
    // adjust size of popup every time the table/tree changes in the model
    final JTableEx proposalTable = SwingUtility.findChildComponent(m_proposalPopup.getSwingContentPane(), JTableEx.class);
    if (proposalTable != null) {
      proposalTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
          if (proposalTable.getSelectedRowCount() == 0) {
            proposalTable.getSelectionModel().setAnchorSelectionIndex(-1);
            proposalTable.getSelectionModel().setLeadSelectionIndex(-1);
          }
        }
      });
    }
    final JTreeEx proposalTree = SwingUtility.findChildComponent(m_proposalPopup.getSwingContentPane(), JTreeEx.class);
    if (proposalTree != null) {
      proposalTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
          if (proposalTree.getSelectionCount() == 0) {
            proposalTree.setAnchorSelectionPath(null);
            proposalTree.setLeadSelectionPath(null);
          }
        }
      });
    }
    //set size to initial width and default height
    m_proposalPopup.getSwingWindow().setSize(new Dimension(getSwingTextField().getWidth(), getScoutObject().getProposalFormHeight()));
    if (proposalTree != null || proposalTable != null) {
      //add a listener whenever the form changes
      form.addFormListener(
          new FormListener() {
            @Override
            public void formChanged(FormEvent e) throws ProcessingException {
              switch (e.getType()) {
                case FormEvent.TYPE_STRUCTURE_CHANGED: {
                  Runnable t = new Runnable() {
                    @Override
                    public void run() {
                      optimizePopupSize(m_proposalPopup, proposalTable, proposalTree);
                    }
                  };
                  getSwingEnvironment().invokeSwingLater(t);
                  break;
                }
              }
            }
          }
          );
      //enqueue a later swing job since there may be waiting swing tasks in the queue that change the table
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          optimizePopupSize(m_proposalPopup, proposalTable, proposalTree);
        }
      });
      //only if table or tree is already filled up, optimize size, otherwise nop
      if ((proposalTree != null && proposalTree.getRowCount() > 0) || (proposalTable != null && proposalTable.getRowCount() > 0)) {
        optimizePopupSize(m_proposalPopup, proposalTable, proposalTree);
      }
    }
    m_proposalPopup.openView();
  }

  private void optimizePopupSize(SwingScoutPopup popup, JTableEx proposalTable, JTreeEx proposalTree) {
    if (proposalTable != null) {
      JViewport viewPort = (proposalTable.getParent() instanceof JViewport ? (JViewport) proposalTable.getParent() : null);
      JScrollPane scrollPane = (viewPort != null && viewPort.getParent() instanceof JScrollPane ? (JScrollPane) viewPort.getParent() : null);
      int scrollbarSize = scrollPane != null ? scrollPane.getVerticalScrollBar().getPreferredSize().width + 2 : 0;
      Dimension d = proposalTable.getPreferredContentSize(1000);
      d.width += scrollbarSize;
      d.width = Math.max(getSwingTextField().getWidth(), Math.min(d.width, 400));
      d.height = Math.max(getSwingEnvironment().getFormRowHeight(), Math.min(d.height, 280));
      Insets insets = proposalTable.getInsets();
      if (insets != null) {
        d.width += insets.left + insets.right;
        d.height += insets.top + insets.bottom;
      }
      proposalTable.setPreferredScrollableViewportSize(d);
      Component c = proposalTable;
      while (c != null && !(c instanceof Window)) {
        c.invalidate();
        c = c.getParent();
      }
      if (popup != null && popup.getSwingWindow() != null && popup.getSwingWindow().isShowing()) {
        popup.autoAdjustBounds();
      }
    }
    else if (proposalTree != null) {
      JViewport viewPort = (proposalTree.getParent() instanceof JViewport ? (JViewport) proposalTree.getParent() : null);
      JScrollPane scrollPane = (viewPort != null && viewPort.getParent() instanceof JScrollPane ? (JScrollPane) viewPort.getParent() : null);
      int scrollbarSize = scrollPane != null ? scrollPane.getVerticalScrollBar().getPreferredSize().width + 2 : 0;
      Dimension d = proposalTree.getPreferredContentSize(1000);
      d.width += scrollbarSize;
      d.width = Math.max(getSwingTextField().getWidth(), Math.min(d.width, 400));
      d.height = Math.max(getSwingEnvironment().getFormRowHeight(), Math.min(d.height, 280));
      Insets insets = proposalTree.getInsets();
      if (insets != null) {
        d.width += insets.left + insets.right;
        d.height += insets.top + insets.bottom;
      }
      proposalTree.setPreferredScrollableViewportSize(d);
      Component c = proposalTree;
      while (c != null && !(c instanceof Window)) {
        c.invalidate();
        c = c.getParent();
      }
      if (popup != null && popup.getSwingWindow() != null && popup.getSwingWindow().isShowing()) {
        popup.autoAdjustBounds();
      }
    }
  }

  private void hideProposalPopup() {
    getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).remove(SwingUtility.createKeystroke("ENTER"));
    getSwingTextField().getActionMap().remove("enter");
    getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).remove(SwingUtility.createKeystroke("ESCAPE"));
    getSwingTextField().getActionMap().remove("escape");
    if (m_proposalPopup != null) {
      m_proposalPopup.closeView();
      m_proposalPopup = null;
    }
  }

  @Override
  protected boolean handleSwingInputVerifier() {
    synchronized (m_pendingProposalJobLock) {
      if (m_pendingProposalJob != null) {
        m_pendingProposalJob.cancel();
        m_pendingProposalJob = null;
      }
    }
    final String text = getSwingTextField().getText();
    final BooleanHolder result = new BooleanHolder(true);
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
    boolean processed = job.getState() == JobEx.NONE;
    // end notify
    getSwingEnvironment().dispatchImmediateSwingJobs();
    if (processed && (!result.getValue())) {
      // keep focus
      return false;
    }
    else {
      // advance focus
      return true;
    }
  }

  @Override
  protected void handleSwingFocusGained() {
    super.handleSwingFocusGained();
    JTextComponent swingField = getSwingTextField();
    if (!isMenuOpened() && swingField.getDocument().getLength() > 0) {
      swingField.setCaretPosition(swingField.getDocument().getLength());
      swingField.moveCaretPosition(0);
    }
    if (getScoutObject().getErrorStatus() != null && getSwingTextField().isEditable() && getSwingTextField().isVisible()) {
      requestProposalSupportFromSwing(getScoutObject().getDisplayText(), false, 0);
    }
    setMenuOpened(false);
  }

  protected boolean isSmartChooserEnabled() {
    return m_dropdownIcon.isEnabled();
  }

  protected void handleSwingSmartChooserAction(long initialDelay) {
    if (isSmartChooserEnabled()) {
      requestProposalSupportFromSwing(IContentAssistField.BROWSE_ALL_TEXT, true, initialDelay);
    }
  }

  protected void handleSwingPopup(final JComponent target) {
    if (getScoutObject().getContextMenu().hasChildActions()) {

      // <bsh 2010-10-08>
      // The default implementation positions the popup menu on the left side of the
      // "target" component. This is no longer correct in Rayo. So we use the target's
      // width and subtract a certain amount.
      int x = 0;
      if (target instanceof JTextComponent) {
        JTextComponent tf = (JTextComponent) target;
        x = tf.getWidth() - tf.getMargin().right;
      }
      final Point point = new Point(x, target.getHeight());
      // </bsh>

      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          // call swing menu
          new SwingPopupWorker(getSwingEnvironment(), target, point, getScoutObject().getContextMenu()).enqueue();
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 5678);
      // end notify
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IContentAssistField.PROP_ICON_ID)) {
      setIconIdFromScout((String) newValue);
    }
    else if (name.equals(IContentAssistField.PROP_PROPOSAL_FORM)) {
      setProposalFormFromScout((IContentAssistFieldProposalForm) newValue);
    }
  }

  private class P_PendingProposalJob extends JobEx implements Runnable {

    private String m_text;
    private boolean m_selectCurrentValue;

    public P_PendingProposalJob() {
      super("");
      setSystem(true);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      if (monitor.isCanceled()) {
        return Status.CANCEL_STATUS;
      }
      SwingUtilities.invokeLater(this);
      return Status.OK_STATUS;
    }

    @Override
    public void run() {
      synchronized (m_pendingProposalJobLock) {
        if (m_pendingProposalJob == this) {
          m_pendingProposalJob = null;
        }
        else {
          return;
        }
      }
      if (getSwingField().isFocusOwner()) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().openProposalFromUI(m_text, m_selectCurrentValue);
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 0);
      }
    }

    public void update(String text, boolean selectCurrentValue) {
      m_text = text;
      m_selectCurrentValue = selectCurrentValue;
    }
  }

  private String getLinesOfStackTrace(StackTraceElement[] elements, int lines) {
    StringBuilder b = new StringBuilder();
    int lineCount = Math.min(lines, elements.length);
    for (int i = 0; i < lineCount; i++) {
      b.append("  " + elements[i].toString());
      if (i < lineCount - 1) {
        b.append("\n");
      }
    }
    return b.toString();
  }

  private static final class P_SwingDocumentFilter extends BasicDocumentFilter {

    public P_SwingDocumentFilter() {
      super(2000);
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String s, AttributeSet a) throws BadLocationException {
      checkStringTooLong(fb, s, fb.getDocument().getLength() + s.length());
      s = ensureConfiguredTextFormat(s);
      super.insertString(fb, offset, s, a);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String s, AttributeSet a) throws BadLocationException {
      checkStringTooLong(fb, s, fb.getDocument().getLength() + s.length() - length);
      s = ensureConfiguredTextFormat(s);
      super.replace(fb, offset, length, s, a);
    }

    private String ensureConfiguredTextFormat(String s) {
      s = StringUtility.trimNewLines(s);
      // replace newlines by spaces
      return s.replaceAll("\r\n", " ").replaceAll("[\r\n]", " ");
    }
  }
}
