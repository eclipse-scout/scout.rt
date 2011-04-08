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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartFieldProposalForm;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.scout.rt.ui.swt.ext.DropDownButton;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.swt.keystroke.SwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.window.SwtScoutPartEvent;
import org.eclipse.scout.rt.ui.swt.window.SwtScoutPartListener;
import org.eclipse.scout.rt.ui.swt.window.popup.SwtScoutSmartFieldPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 * <h3>SwtScoutSmartField</h3> ...
 * 
 * @since 1.0.0 10.04.2008
 */
public class SwtScoutSmartField extends SwtScoutValueFieldComposite<ISmartField<?>> implements ISwtScoutSmartField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutSmartField.class);

  private DropDownButton m_browseButton;
  private P_PendingProposalJob m_pendingProposalJob;
  private Object m_pendingProposalJobLock;
  // popup
  private SwtScoutSmartFieldPopup m_proposalPopup;
  private final Object m_popupLock = new Object();
  private Listener m_popupDelegateListener;
  // private Composite m_buttonArea;
  private Menu m_contextMenu;
  private TextFieldEditableSupport m_editableSupport;

  public SwtScoutSmartField() {
    m_pendingProposalJobLock = new Object();
    m_popupDelegateListener = new P_PopupDelegateListener();
  }

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle, getEnvironment());
    getEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);
    StyledText textField = getEnvironment().getFormToolkit().createStyledText(container, SWT.SINGLE | SWT.BORDER);
    m_browseButton = new DropDownButton(container, SWT.DROP_DOWN);
    // to ensure the text is validated on a context menu call this mouse
    // listener is used.
    m_browseButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        handleSwtInputVerifier();
      }
    });
    m_browseButton.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        getSwtField().setFocus();
      }
    });
    setSwtContainer(container);

    setSwtLabel(label);
    setSwtField(textField);
    // prevent the button from grabbing focus
    container.setTabList(new Control[]{textField});

    // context menu
    m_contextMenu = new Menu(m_browseButton.getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new P_ContextMenuListener());
    m_browseButton.setMenu(m_contextMenu);

    // F2 key stroke
    getEnvironment().addKeyStroke(getSwtContainer(), new P_F2KeyStroke());

    // listeners
    P_SwtTextModifiedListener swtTextFieldListener = new P_SwtTextModifiedListener();
    getSwtField().addListener(SWT.KeyDown, swtTextFieldListener);
    getSwtField().addListener(SWT.Modify, swtTextFieldListener);
    P_SwtBrowseButtonListener swtBrowseButtonListener = new P_SwtBrowseButtonListener();
    getSwtBrowseButton().addSelectionListener(swtBrowseButtonListener);

    // layout
    container.setLayout(new LogicalGridLayout(1, 0));
    m_browseButton.setLayoutData(LogicalGridDataBuilder.createSmartButton());
  }

  public DropDownButton getSwtBrowseButton() {
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
  }

  @Override
  protected void detachScout() {
    // workaround since disposeFieldInternal in AbstractSmartField is never called.
    hideProposalPopup();
    super.detachScout();
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    getSwtBrowseButton().setDropdownEnabled(getScoutObject().hasMenus());
    if (s == null) {
      s = "";
    }
    StyledText swtField = getSwtField();
    swtField.setText(s);
    swtField.setCaretOffset(0);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_browseButton.setButtonEnabled(b);
  }

  @Override
  protected void setFieldEnabled(Control swtField, boolean enabled) {
    if (UiDecorationExtensionPoint.getLookAndFeel().isEnabledAsReadOnly()) {
      if (m_editableSupport == null) {
        m_editableSupport = new TextFieldEditableSupport(getSwtField());
      }
      m_editableSupport.setEditable(enabled);
    }
    else {
      super.setFieldEnabled(swtField, enabled);
    }
  }

  protected void setIconIdFromScout(String s) {
    m_browseButton.setImage(getEnvironment().getIcon(s));
  }

  @Override
  protected void handleSwtFocusGained() {
    super.handleSwtFocusGained();
    if (m_proposalPopup == null) {
      getSwtField().setSelection(0, getSwtField().getText().length());
    }
    if (getScoutObject().getErrorStatus() != null) {
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

  protected void handleSwtDocumentChanged() {
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
      }
      else {
        requestProposalSupportFromSwt(text, false);
      }
    }
  }

  protected void setProposalFormFromScout(ISmartFieldProposalForm form) {
    synchronized (m_pendingProposalJobLock) {
      m_pendingProposalJob = null;
    }
    if (form != null) {
      showProposalPopup(form);
    }
    else {
      hideProposalPopup();
    }
  }

  protected void showProposalPopup(ISmartFieldProposalForm form) {
    // close old
    if (m_proposalPopup != null) {
      if (m_proposalPopup.isVisible()) {
        try {
          m_proposalPopup.closePart();
        }
        catch (ProcessingException e) {
          LOG.warn("could not close popup.", e);
        }
      }
      m_proposalPopup = null;
    }
    // show new
    if (getSwtField().isFocusControl()) {
      m_proposalPopup = new SwtScoutSmartFieldPopup(getEnvironment(), getSwtField(), getSwtField());
      m_proposalPopup.addSwtScoutPartListener(new SwtScoutPartListener() {
        public void partChanged(SwtScoutPartEvent e) {
          switch (e.getType()) {
            case SwtScoutPartEvent.TYPE_CLOSED:
              if (m_proposalPopup != null) {
                m_proposalPopup = null;
                getSwtField().removeListener(SWT.Verify, m_popupDelegateListener);
                getSwtField().removeListener(SWT.Traverse, m_popupDelegateListener);
              }
              break;
            case SwtScoutPartEvent.TYPE_CLOSING:
              hideProposalPopup();
              break;
          }
        }
      });
      m_proposalPopup.getShell().addShellListener(new ShellAdapter() {
        @Override
        public void shellClosed(ShellEvent e) {
          e.doit = false;
        }
      });
      m_proposalPopup.makeNonFocusable();
      try {
        m_proposalPopup.showForm(form);
        getSwtField().addListener(SWT.Traverse, m_popupDelegateListener);
        getSwtField().addListener(SWT.Verify, m_popupDelegateListener);
        form.addFormListener(new FormListener() {
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

              default:
                break;
            }
          }
        });
      }
      catch (ProcessingException e1) {
        LOG.error(e1.getMessage(), e1);
      }
    }
  }

  protected void hideProposalPopup() {
    synchronized (m_popupLock) {
      if (m_proposalPopup != null) {
        try {
          m_proposalPopup.closePart();
        }
        catch (ProcessingException e) {
          LOG.warn("could not close popup.", e);
        }
        getSwtField().removeListener(SWT.Traverse, m_popupDelegateListener);
        getSwtField().removeListener(SWT.Verify, m_popupDelegateListener);
        m_proposalPopup = null;
      }
    }
  }

  protected void requestProposalSupportFromSwt(String text, boolean selectCurrentValue) {
    synchronized (m_pendingProposalJobLock) {
      boolean created = false;
      if (m_pendingProposalJob == null) {
        m_pendingProposalJob = new P_PendingProposalJob();
        created = true;
      }
      m_pendingProposalJob.update(text, selectCurrentValue);
      if (created) {
        m_pendingProposalJob.startTimer();
      }
    }
  }

  protected void acceptProposalFromSwt() {
    synchronized (m_pendingProposalJobLock) {
      m_pendingProposalJob = null;
    }
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().acceptProposalFromUI();
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
    // end notify
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
      m_pendingProposalJob = null;
    }
    final String text = getSwtField().getText();
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, true);
    //
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
    getEnvironment().dispatchImmediateSwtJobs();
    boolean processed = job.getState() == JobEx.NONE;
    // end notify
    // open popup
    if (processed && !result.getValue()) {
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
      requestProposalSupportFromSwt(ISmartField.BROWSE_ALL_TEXT, true);
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ISmartField.PROP_ICON_ID)) {
      setIconIdFromScout((String) newValue);
    }
    else if (name.equals(ISmartField.PROP_PROPOSAL_FORM)) {
      setProposalFormFromScout((ISmartFieldProposalForm) newValue);
    }
  }

  protected void delegateSwtProposalPopupKey(int keyCode) {
    if (m_proposalPopup == null) {
      requestProposalSupportFromSwt(ISmartField.BROWSE_ALL_TEXT, true);
    }
    else {
      Widget c = null;
      if (c == null) {
        c = SwtUtility.findChildComponent(m_proposalPopup.getSwtContentPane(), Table.class);
      }
      if (c == null) {
        c = SwtUtility.findChildComponent(m_proposalPopup.getSwtContentPane(), Tree.class);
      }
      SwtUtility.handleNavigationKey(c, keyCode);
    }
  }

  private class P_SwtTextModifiedListener implements Listener {
    public void handleEvent(Event event) {
      if (getSwtField().isEnabled() && getSwtField().getEditable() && getSwtField().isVisible()) {
        switch (event.type) {
          case SWT.KeyDown: {
            switch (event.keyCode) {
              case SWT.ARROW_DOWN:
                delegateSwtProposalPopupKey(event.keyCode);
                break;
              case SWT.ARROW_UP:
                delegateSwtProposalPopupKey(event.keyCode);
                break;
              case SWT.PAGE_DOWN:
                delegateSwtProposalPopupKey(event.keyCode);
                break;
              case SWT.PAGE_UP:
                delegateSwtProposalPopupKey(event.keyCode);
                break;
            }
            break;
          }
          case SWT.Modify: {
            handleSwtDocumentChanged();
          }
        }
      }
    }
  } // P_SwtTextModifiedListener

  private class P_PendingProposalJob implements Runnable {
    private String m_text;
    private boolean m_selectCurrentValue;
    private long m_lastChanged = System.currentTimeMillis();

    public void update(String text, boolean selectCurrentValue) {
      m_text = text;
      m_selectCurrentValue = selectCurrentValue;
      m_lastChanged = System.currentTimeMillis();
    }

    /*
     * this runnable runs in swt
     */
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
      if (getSwtField().isFocusControl()) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().openProposalFromUI(m_text, m_selectCurrentValue);
          }
        };
        getEnvironment().invokeScoutLater(t, 0);
      }
    }

    /*
     * Timer that waits 400ms for further typed keys before sending to scout
     */
    public void startTimer() {
      final long delay = 400;
      Thread t = new Thread() {
        /*
         * this runnable runs in timer thread
         */
        @Override
        public void run() {
          while (m_pendingProposalJob == P_PendingProposalJob.this) {
            long dt = m_lastChanged + delay - System.currentTimeMillis();
            if (dt > 0) {
              try {
                sleep(dt);
              }
              catch (InterruptedException ie) {
              }
            }
            else {
              break;
            }
          }
          synchronized (m_pendingProposalJobLock) {
            if (m_pendingProposalJob == P_PendingProposalJob.this) {
              getEnvironment().getDisplay().asyncExec(P_PendingProposalJob.this);
            }
          }
        }
      };
      t.setDaemon(true);
      t.start();
    }
  }// end private class

  private class P_PopupDelegateListener implements Listener {
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Traverse: {
          switch (event.keyCode) {
            // avoid closing on escape
            case SWT.ESC:
              event.doit = false;
              return;
          }
          break;
        }
      }
    }
  } // end class P_PopupDelegateListener

  private class P_ContextMenuListener extends MenuAdapter {
    @Override
    public void menuShown(MenuEvent e) {
      for (MenuItem item : m_contextMenu.getItems()) {
        disposeMenuItem(item);
      }
      final AtomicReference<IMenu[]> scoutMenusRef = new AtomicReference<IMenu[]>();
      Runnable t = new Runnable() {
        @Override
        public void run() {
          IMenu[] scoutMenus = getScoutObject().getUIFacade().firePopupFromUI();
          scoutMenusRef.set(scoutMenus);
        }
      };
      JobEx job = getEnvironment().invokeScoutLater(t, 1200);
      try {
        job.join(1200);
      }
      catch (InterruptedException ex) {
        //nop
      }
      // grab the actions out of the job, when the actions are providden within
      // the scheduled time the popup will be handled.
      if (scoutMenusRef.get() != null) {
        SwtMenuUtility.fillContextMenu(scoutMenusRef.get(), m_contextMenu, getEnvironment());
      }
    }

    private void disposeMenuItem(MenuItem item) {
      Menu menu = item.getMenu();
      if (menu != null) {
        for (MenuItem childItem : menu.getItems()) {
          disposeMenuItem(childItem);
        }
        menu.dispose();
      }
      item.dispose();
    }

  } // end class P_ContextMenuListener

  private class P_SwtBrowseButtonListener extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent e) {
      handleSwtBrowseAction();
    }

  } // end class P_SwtBrowseButtonListener

  private class P_F2KeyStroke extends SwtKeyStroke {
    public P_F2KeyStroke() {
      super(SWT.F2);
    }

    public void handleSwtAction(Event e) {
      handleSwtBrowseAction();
    }
  } // end class P_F2KeyStroke

}
