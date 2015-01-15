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
package org.eclipse.scout.rt.ui.swt.form.fields.smartfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldProposalForm;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.action.menu.MenuPositionCorrectionListener;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtScoutContextMenu;
import org.eclipse.scout.rt.ui.swt.action.menu.text.StyledTextAccess;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.swt.keystroke.SwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.util.SwtLayoutUtility;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.window.popup.SwtScoutDropDownPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 * Implementation of the {@link ISmartField} in SWT-UI.
 */
public class SwtScoutSmartField extends SwtScoutValueFieldComposite<IContentAssistField<?, ?>> implements ISwtScoutSmartField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutSmartField.class);

  private Button m_browseButton;
  private P_PendingProposalJob m_pendingProposalJob;
  private final Object m_pendingProposalJobLock;
  // popup
  private SwtScoutDropDownPopup m_proposalPopup;
  private final Object m_popupLock = new Object();
  private TextFieldEditableSupport m_editableSupport;

  private SwtContextMenuMarkerComposite m_menuMarkerComposite;
  private SwtScoutContextMenu m_contextMenu;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  public SwtScoutSmartField() {
    m_pendingProposalJobLock = new Object();
  }

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    m_menuMarkerComposite = new SwtContextMenuMarkerComposite(container, getEnvironment());
    getEnvironment().getFormToolkit().adapt(m_menuMarkerComposite);
    m_menuMarkerComposite.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        getSwtField().setFocus();
        m_contextMenu.getSwtMenu().setVisible(true);
      }
    });
    final StyledText textField = getEnvironment().getFormToolkit().createStyledText(m_menuMarkerComposite, SWT.SINGLE);
    textField.setAlignment(SwtUtility.getHorizontalAlignment(getScoutObject().getGridData().horizontalAlignment));
    textField.setMargins(2, 2, 2, 2);
    textField.setWrapIndent(textField.getIndent());
    m_browseButton = getEnvironment().getFormToolkit().createButton(container, "", SWT.PUSH);
    m_browseButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        textField.setFocus(); // make the textfield the focus owner so that the user can immediately start narrowing the search.
        handleSwtBrowseAction();
      }
    });

    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(textField);

    // prevent the button from grabbing focus
    container.setTabList(new Control[]{m_menuMarkerComposite});

    // F2 key stroke
    getEnvironment().addKeyStroke(getSwtContainer(), new P_F2KeyStroke());

    // listeners
    P_UiFieldListener listener = new P_UiFieldListener();
    getSwtField().addListener(SWT.KeyDown, listener);
    getSwtField().addListener(SWT.Modify, listener);
    getSwtField().addListener(SWT.Traverse, listener);

    // layout
    container.setLayout(new LogicalGridLayout(1, 0));
    m_menuMarkerComposite.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    m_browseButton.setLayoutData(LogicalGridDataBuilder.createButton1());
  }

  protected void installContextMenu() {
    m_menuMarkerComposite.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    m_contextMenuVisibilityListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
          final boolean markerVisible = getScoutObject().getContextMenu().isVisible();
          getEnvironment().invokeSwtLater(new Runnable() {
            @Override
            public void run() {
              m_menuMarkerComposite.setMarkerVisible(markerVisible);
            }
          });
        }
      }
    };
    getScoutObject().getContextMenu().addPropertyChangeListener(m_contextMenuVisibilityListener);

    m_contextMenu = new SwtScoutContextMenu(getSwtField().getShell(), getScoutObject().getContextMenu(), getEnvironment());
    getSwtBrowseButton().setMenu(m_contextMenu.getSwtMenu());

    SwtScoutContextMenu fieldMenu = new SwtScoutContextMenu(getSwtField().getShell(), getScoutObject().getContextMenu(), getEnvironment(),
        getScoutObject().isAutoAddDefaultMenus() ? new StyledTextAccess(getSwtField()) : null, getScoutObject().isAutoAddDefaultMenus() ? getSwtField() : null);
    getSwtField().setMenu(fieldMenu.getSwtMenu());

    // correction of menu position
    getSwtField().addListener(SWT.MenuDetect, new MenuPositionCorrectionListener(getSwtField()));
  }

  protected void uninstallContextMenu() {
    if (m_contextMenuVisibilityListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(m_contextMenuVisibilityListener);
      m_contextMenuVisibilityListener = null;
    }
  }

  @Override
  public Button getSwtBrowseButton() {
    return m_browseButton;
  }

  @Override
  public StyledText getSwtField() {
    return (StyledText) super.getSwtField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    setIconIdFromScout(getScoutObject().getIconId());
    setProposalFormFromScout(getScoutObject().getProposalForm());
    installContextMenu();
  }

  @Override
  protected void detachScout() {
    // workaround since disposeFieldInternal in AbstractSmartField is never called.
    hideProposalPopup();
    uninstallContextMenu();
    super.detachScout();
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    if (!CompareUtility.equals(s, getSwtField().getText())) {
      if (s == null) {
        s = "";
      }
      StyledText swtField = getSwtField();
      swtField.setText(s);
      swtField.setCaretOffset(0);
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_browseButton.setEnabled(b);
  }

  @Override
  protected void setFieldEnabled(Control swtField, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getSwtField());
    }
    m_editableSupport.setEditable(enabled);
  }

  protected void setIconIdFromScout(String s) {
    boolean iconVisible = s != null;
    boolean invalidateLayout = false;
    if (m_browseButton.isVisible() != iconVisible) {
      invalidateLayout = true;
    }
    m_browseButton.setVisible(iconVisible);
    m_browseButton.setImage(getEnvironment().getIcon(s));
    if (invalidateLayout && isConnectedToScout()) {
      SwtLayoutUtility.invalidateLayout(m_browseButton);
    }
  }

  @Override
  protected void handleSwtFocusGained() {
    super.handleSwtFocusGained();
    if (m_proposalPopup == null) {
      scheduleSelectAll();
    }
    if (getScoutObject().getErrorStatus() != null && getSwtField().getEditable() && getSwtField().isVisible()) {
      requestProposalSupportFromSwt(getScoutObject().getDisplayText(), false);
    }
  }

  @Override
  protected void handleSwtFocusLost() {
    super.handleSwtFocusLost();
    if (!getSwtField().isDisposed()) {
      getSwtField().setSelection(0, 0);
    }
  }

  protected void scheduleSelectAll() {
    getEnvironment().getDisplay().asyncExec(new Runnable() {

      @Override
      public void run() {
        if (getSwtField().isDisposed()) {
          return;
        }

        getSwtField().setSelection(0, getSwtField().getText().length());
      }

    });

  }

  protected void setProposalFormFromScout(IContentAssistFieldProposalForm form) {
    synchronized (m_pendingProposalJobLock) {
      if (m_pendingProposalJob != null) {
        m_pendingProposalJob.cancel();
        m_pendingProposalJob = null;
      }
    }
    if (form != null) {
      showProposalPopup(form);
    }
    else {
      hideProposalPopup();
    }
  }

  protected void showProposalPopup(final IContentAssistFieldProposalForm form) {
    // close old
    if (m_proposalPopup != null) {
      if (m_proposalPopup.isVisible()) {
        m_proposalPopup.closePart();
      }
      m_proposalPopup = null;
    }
    // show new
    if (form != null) {
      if (getSwtField().isFocusControl()) {
        m_proposalPopup = new SwtScoutDropDownPopup(getEnvironment(), getSwtField(), false, SWT.RESIZE);

        m_proposalPopup.setMaxHeightHint(getScoutObject().getProposalFormHeight());
        m_proposalPopup.addShellListener(new ShellAdapter() {

          @Override
          public void shellDeactivated(ShellEvent e) {
            hideProposalPopup(); // Hide the proposal popup if being the active Shell and the user activated another Shell (e.g. the owner Shell or switched the application).
          }
        });
        m_proposalPopup.getShell().addShellListener(new ShellAdapter() {
          @Override
          public void shellClosed(ShellEvent e) {
            e.doit = false;
          }
        });
        m_proposalPopup.showForm(form);

        //add a listener whenever the form changes
        form.addFormListener(new FormListener() {
          @Override
          public void formChanged(FormEvent e) throws ProcessingException {
            switch (e.getType()) {
              case FormEvent.TYPE_STRUCTURE_CHANGED:
                Runnable job = new Runnable() {
                  @Override
                  public void run() {
                    if (m_proposalPopup != null) {
                      m_proposalPopup.autoAdjustBounds();
                    }
                  }
                };
                getEnvironment().invokeSwtLater(job);
                break;
            }
          }
        });
        //enqueue a later display job since there may be waiting display tasks in the queue that change the table/tree
        getSwtField().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            if (m_proposalPopup != null) {
              m_proposalPopup.autoAdjustBounds();
            }
          }
        });
      }
      else {
        Runnable t = new Runnable() {

          @Override
          public void run() {
            try {
              form.doClose();
            }
            catch (ProcessingException e) {
              LOG.error("Failed to close smartfield form.", e);
            }
          }
        };
        getEnvironment().invokeScoutLater(t, 0);
      }
    }
  }

  protected void hideProposalPopup() {
    synchronized (m_popupLock) {
      if (m_proposalPopup != null && m_proposalPopup.isVisible()) {
        m_proposalPopup.closePart();
      }
      m_proposalPopup = null;
    }
  }

  protected void requestProposalSupportFromSwt(String text, boolean selectCurrentValue) {
    synchronized (m_pendingProposalJobLock) {
      if (m_pendingProposalJob == null) {
        m_pendingProposalJob = new P_PendingProposalJob();
      }
      else {
        m_pendingProposalJob.cancel();
      }
      m_pendingProposalJob.update(text, selectCurrentValue);
      int delay = 200;
      if (m_proposalPopup == null) {
        delay = 0;
      }
      m_pendingProposalJob.schedule(delay);
    }
  }

  private void acceptProposalFromSwt() {
    synchronized (m_pendingProposalJobLock) {
      if (m_pendingProposalJob != null) {
        m_pendingProposalJob.cancel();
        m_pendingProposalJob = null;
      }
    }
    final String text = getSwtField().getText();
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setTextFromUI(text);
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
  }

  @Override
  protected boolean filterKeyEvent(Event e) {
    if (m_proposalPopup != null && m_proposalPopup.isVisible()) {
      if (e.keyCode == SWT.ESC) {
        hideProposalPopup();
        return false;
      }
      else if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
        acceptProposalFromSwt();
        return false;
      }
    }
    return super.filterKeyEvent(e);
  }

  @Override
  protected boolean handleSwtInputVerifier() {
    synchronized (m_pendingProposalJobLock) {
      if (m_pendingProposalJob != null) {
        m_pendingProposalJob.cancel();
        m_pendingProposalJob = null;
      }
    }
    final String text = getSwtField().getText();
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, true);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setTextFromUI(text);
        result.setValue(b);
      }
    };
    JobEx job = getEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    boolean processed = job.getState() == JobEx.NONE;
    // end notify
    getEnvironment().dispatchImmediateSwtJobs();
    if (processed && (!result.getValue())) {
      // keep focus
      return false;
    }
    else {
      // advance focus
      return true;
    }
  }

  protected void handleSwtBrowseAction() {
    if (getSwtBrowseButton().isVisible() && getSwtBrowseButton().isEnabled()) {
      requestProposalSupportFromSwt(IContentAssistField.BROWSE_ALL_TEXT, true);
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

  protected void handleTextModifiedFromUi(Event event) {
    if (getUpdateSwtFromScoutLock().isAcquired()) {
      return;
    }
    if (getSwtField().isVisible() && getSwtField().isFocusControl()) {
      String text = getSwtField().getText();
      if (text == null || text.length() == 0) {
        // allow empty field without proposal
        if (m_proposalPopup != null) {
          requestProposalSupportFromSwt(text, false);
        }
        else {
          // notify Scout
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutObject().getUIFacade().setTextFromUI(null);
            }
          };
          getEnvironment().invokeScoutLater(t, 0);
          // end notify
        }
      }
      else {
        requestProposalSupportFromSwt(text, false);
      }
    }
  }

  protected void handleKeyDownFromUI(Event event) {
    switch (event.keyCode) {
      case SWT.ARROW_DOWN:
      case SWT.ARROW_UP:
      case SWT.PAGE_DOWN:
      case SWT.PAGE_UP:
        if (getSwtField().isEnabled() && getSwtField().getEditable() && getSwtField().isVisible()) {
          if (m_proposalPopup == null) {
            requestProposalSupportFromSwt(IContentAssistField.BROWSE_ALL_TEXT, true);
          }
          else {
            Widget c = null;
            if (c == null) {
              c = SwtUtility.findChildComponent(m_proposalPopup.getShellContentPane(), Table.class);
            }
            if (c == null) {
              c = SwtUtility.findChildComponent(m_proposalPopup.getShellContentPane(), Tree.class);
            }
            SwtUtility.handleNavigationKey(c, event.keyCode);
          }
        }
        break;
    }
  }

  protected void handleTraverseFromUi(Event event) {
    switch (event.keyCode) {
      case SWT.ESC:
        if (m_proposalPopup != null) {
          event.doit = false;
        }
        break;
    }
  }

  private class P_UiFieldListener implements Listener {

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Modify:
          handleTextModifiedFromUi(event);
          break;
        case SWT.KeyDown:
          handleKeyDownFromUI(event);
          break;
        case SWT.Traverse:
          handleTraverseFromUi(event);
          break;
      }
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
      getEnvironment().getDisplay().asyncExec(this);
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
      if (!getSwtField().isDisposed() && getSwtField().isFocusControl()) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().openProposalFromUI(m_text, m_selectCurrentValue);
          }
        };
        getEnvironment().invokeScoutLater(t, 0);
      }
    }

    public void update(String text, boolean selectCurrentValue) {
      m_text = text;
      m_selectCurrentValue = selectCurrentValue;
    }
  }

  private class P_F2KeyStroke extends SwtKeyStroke {
    public P_F2KeyStroke() {
      super(SWT.F2);
    }

    @Override
    public void handleSwtAction(Event e) {
      handleSwtBrowseAction();
    }
  } // end class P_F2KeyStroke

}
