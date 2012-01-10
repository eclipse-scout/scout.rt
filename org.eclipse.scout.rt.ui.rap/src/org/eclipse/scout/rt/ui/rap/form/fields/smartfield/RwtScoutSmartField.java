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
package org.eclipse.scout.rt.ui.rap.form.fields.smartfield;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.CollectionUtility;
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
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartFieldProposalForm;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.ext.DropDownButton;
import org.eclipse.scout.rt.ui.rap.ext.MenuAdapterEx;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.ext.table.TableEx;
import org.eclipse.scout.rt.ui.rap.ext.tree.TreeEx;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.fields.IPopupSupport;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.rap.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.RwtScoutPartEvent;
import org.eclipse.scout.rt.ui.rap.window.RwtScoutPartListener;
import org.eclipse.scout.rt.ui.rap.window.popup.RwtScoutDropDownPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 * <h3>RwtScoutSmartField</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutSmartField extends RwtScoutValueFieldComposite<ISmartField<?>> implements IRwtScoutSmartField, IPopupSupport {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutSmartField.class);

  private DropDownButton m_browseButton;
  private P_PendingProposalJob m_pendingProposalJob;
  private Object m_pendingProposalJobLock;
  private Composite m_smartContainer;
  // popup
  private RwtScoutDropDownPopup m_proposalPopup;
  private final Object m_popupLock = new Object();
  private Menu m_contextMenu;
  private TextFieldEditableSupport m_editableSupport;

  private Set<IPopupSupportListener> m_popupEventListeners;
  private Object m_popupEventListenerLock;

  public RwtScoutSmartField() {
    m_pendingProposalJobLock = new Object();
  }

  @Override
  protected void initializeUi(Composite parent) {
    m_popupEventListeners = new HashSet<IPopupSupportListener>();
    m_popupEventListenerLock = new Object();

    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle);
    getUiEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);

    m_smartContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.BORDER);
    m_smartContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_SMARTFIELD);
    StyledText textField = new StyledTextEx(m_smartContainer, SWT.SINGLE) {
      private static final long serialVersionUID = 1L;

      @Override
      public void setBackground(Color color) {
        super.setBackground(color);
        if (m_browseButton != null) {
          m_browseButton.setBackground(color);
        }
      }
    };
    getUiEnvironment().getFormToolkit().adapt(textField, false, false);
    // correction to look like a normal text
    textField.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_SMARTFIELD);

    m_browseButton = getUiEnvironment().getFormToolkit().createDropDownButton(m_smartContainer, SWT.DROP_DOWN | SWT.NO_FOCUS);
    m_browseButton.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_SMARTFIELD);
    // mouseDown-handler to ensure the text is validated on a context menu call is not
    // necessary as handleUiInputVerifier is already triggered by FocusOut-event in
    // org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite.P_RwtFieldListener<T>.

    setUiContainer(container);
    setUiLabel(label);
    setUiField(textField);
    // prevent the button from grabbing focus
    m_smartContainer.setTabList(new Control[]{textField});

    m_browseButton.addFocusListener(new FocusAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void focusGained(FocusEvent e) {
        getUiField().setFocus();
      }
    });

    // context menu
    m_contextMenu = new Menu(m_browseButton.getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new P_ContextMenuListener(getUiBrowseButton(), getUiField()));
    m_browseButton.setMenu(m_contextMenu);

    // F2 key stroke
    getUiEnvironment().addKeyStroke(getUiContainer(), new P_F2KeyStroke());

    // listeners
    P_UiFieldListener listener = new P_UiFieldListener();
    getUiField().addListener(SWT.Modify, listener);
    getUiField().addListener(SWT.Traverse, listener);
    getUiField().addListener(SWT.FocusOut, listener);
    getUiEnvironment().addKeyStroke(getUiField(), new P_KeyListener(SWT.ARROW_DOWN));
    getUiEnvironment().addKeyStroke(getUiField(), new P_KeyListener(SWT.ARROW_UP));
    getUiEnvironment().addKeyStroke(getUiField(), new P_KeyListener(SWT.PAGE_DOWN));
    getUiEnvironment().addKeyStroke(getUiField(), new P_KeyListener(SWT.PAGE_UP));

    P_RwtBrowseButtonListener browseButtonListener = new P_RwtBrowseButtonListener();
    getUiBrowseButton().addSelectionListener(browseButtonListener);

    // layout
    container.setLayout(new LogicalGridLayout(1, 0));

    m_smartContainer.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    m_smartContainer.setLayout(new FormLayout());

    final FormData textLayoutData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    textLayoutData.right = new FormAttachment(100, -20);
    textLayoutData.left = new FormAttachment(0, 0);
    textLayoutData.bottom = new FormAttachment(textField, 0, SWT.BOTTOM);
    textField.setLayoutData(textLayoutData);

    final FormData buttonLayoutData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    buttonLayoutData.left = new FormAttachment(textField, -1, SWT.RIGHT);
    buttonLayoutData.bottom = new FormAttachment(m_browseButton, 1, SWT.BOTTOM);
    m_browseButton.setLayoutData(buttonLayoutData);
  }

  @Override
  public DropDownButton getUiBrowseButton() {
    return m_browseButton;
  }

  @Override
  public StyledText getUiField() {
    return (StyledText) super.getUiField();
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
    if (!CompareUtility.equals(s, getUiField().getText())) {
      getUiBrowseButton().setDropdownEnabled(getScoutObject().hasMenus());
      if (s == null) {
        s = "";
      }
      StyledText field = getUiField();
      field.setText(s);
      field.setCaretOffset(0);
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_browseButton.setButtonEnabled(b);
    getUiField().setEnabled(b);
    if (b) {
      m_smartContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_SMARTFIELD);
    }
    else {
      m_smartContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_SMARTFIELD_DISABLED);
    }
  }

  @Override
  protected void setFieldEnabled(Control field, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getUiField());
    }
    m_editableSupport.setEditable(enabled);
  }

  protected void setIconIdFromScout(String s) {
//    m_browseButton.setImage(getEnvironment().getIcon(s));//FIXME
  }

  @Override
  protected void handleUiFocusGained() {
    super.handleUiFocusGained();
    if (m_proposalPopup == null) {
      getUiField().setSelection(0, getUiField().getText().length());
    }
    if (getScoutObject().getErrorStatus() != null) {
      requestProposalSupportFromUi(getScoutObject().getDisplayText(), false);
    }
  }

  @Override
  protected void handleUiFocusLost() {
    super.handleUiFocusLost();
    if (!getUiField().isDisposed()) {
      getUiField().setSelection(0, 0);
    }
  }

  protected void setProposalFormFromScout(ISmartFieldProposalForm form) {
    synchronized (m_pendingProposalJobLock) {
      if (m_pendingProposalJob != null) {
        UICallBack.deactivate(m_pendingProposalJob.getClass().getName() + m_pendingProposalJob.hashCode());
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

  protected void showProposalPopup(final ISmartFieldProposalForm form) {
    // close old
    if (m_proposalPopup != null) {
      if (m_proposalPopup.isVisible()) {
        m_proposalPopup.closePart();
      }
      m_proposalPopup = null;
    }
    // show new
    if (getUiField().isFocusControl()) {
      m_proposalPopup = new RwtScoutDropDownPopup();
      m_proposalPopup.setMaxHeightHint(280);
      m_proposalPopup.createPart(form, m_smartContainer, getUiField(), SWT.RESIZE, getUiEnvironment());
      m_proposalPopup.addRwtScoutPartListener(new RwtScoutPartListener() {
        @Override
        public void partChanged(RwtScoutPartEvent e) {
          switch (e.getType()) {
            case RwtScoutPartEvent.TYPE_OPENING: {
              notifyPopupEventListeners(IPopupSupportListener.TYPE_OPENING);
              break;
            }
            case RwtScoutPartEvent.TYPE_CLOSING: {
              hideProposalPopup();
              break;
            }
            case RwtScoutPartEvent.TYPE_CLOSED: {
              if (m_proposalPopup != null) {
                m_proposalPopup = null;
                notifyPopupEventListeners(IPopupSupportListener.TYPE_CLOSED);
              }
              break;
            }
          }
        }
      });
      m_proposalPopup.getShell().addShellListener(new ShellAdapter() {
        private static final long serialVersionUID = 1L;

        @Override
        public void shellClosed(ShellEvent e) {
          e.doit = false;
        }
      });
      m_proposalPopup.makeNonFocusable();
      //
      try {
        // adjust size of popup every time the table/tree changes in the model
        m_proposalPopup.getShell().setSize(new Point(getUiField().getSize().x, 200));
        final TableEx proposalTable = RwtUtility.findChildComponent(m_proposalPopup.getUiContentPane(), TableEx.class);
        final TreeEx proposalTree = RwtUtility.findChildComponent(m_proposalPopup.getUiContentPane(), TreeEx.class);
        if (proposalTree != null || proposalTable != null) {
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
                      getUiEnvironment().invokeUiLater(t);
                      break;
                    }
                  }
                }
              });
          //enqueue a later display job since there may be waiting display tasks in the queue that change the table/tree
          getUiEnvironment().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
              optimizePopupSize(m_proposalPopup, proposalTable, proposalTree);
            }
          });
          //only if table or tree is already filled up, optimize size, otherwise nop
          if ((proposalTree != null && proposalTree.getItemCount() > 0) || (proposalTable != null && proposalTable.getItemCount() > 0)) {
            optimizePopupSize(m_proposalPopup, proposalTable, proposalTree);
          }
        }
        m_proposalPopup.showPart();
      }
      catch (Throwable e1) {
        LOG.error(e1.getLocalizedMessage(), e1);
      }
    }
  }

  private void optimizePopupSize(RwtScoutDropDownPopup popup, TableEx proposalTable, TreeEx proposalTree) {
    if (popup == null) {
      return;
    }
    int minFormWidth = 0;
    if (getScoutObject().isActiveFilterEnabled()) {
      minFormWidth = 175;
    }
    if (proposalTable != null) {
      int scrollbarSize = proposalTable.getVerticalBar() != null ? proposalTable.getVerticalBar().getSize().x + 10 : 0;
      Point d = proposalTable.getPreferredContentSize(1000);
      d.x += scrollbarSize;
      d.x = Math.max(Math.max(getUiField().getSize().x, minFormWidth), Math.min(d.x, 400));
      d.y = Math.max(UiDecorationExtensionPoint.getLookAndFeel().getLogicalGridLayoutRowHeight(), Math.min(d.y, 280));
      popup.getShell().setSize(d);
      Composite c = proposalTable;
      while (c != null && !(c instanceof Shell)) {
        c.layout(true);
        c = c.getParent();
      }
    }
    else if (proposalTree != null) {
      int scrollbarSize = proposalTree.getVerticalBar() != null ? proposalTree.getVerticalBar().getSize().x + 10 : 0;
      Point d = proposalTree.getPreferredContentSize(1000, null, 0);
      d.x += scrollbarSize;
      d.x = Math.max(Math.max(getUiField().getSize().x, minFormWidth), Math.min(d.x, 400));
      d.y = Math.max(UiDecorationExtensionPoint.getLookAndFeel().getLogicalGridLayoutRowHeight(), Math.min(d.x, 280));
      popup.getShell().setSize(d);
      Composite c = proposalTree;
      while (c != null && !(c instanceof Shell)) {
        c.layout(true);
        c = c.getParent();
      }
    }
    if (popup.getShell() != null && popup.getShell().isVisible()) {
      popup.autoAdjustBounds();
    }
  }

  protected boolean hideProposalPopup() {
    synchronized (m_popupLock) {
      if (m_proposalPopup != null && m_proposalPopup.isVisible()) {
        m_proposalPopup.closePart();
        m_proposalPopup = null;
        return true;
      }
      return false;
    }
  }

  protected void requestProposalSupportFromUi(String text, boolean selectCurrentValue) {
    synchronized (m_pendingProposalJobLock) {
      if (m_pendingProposalJob == null) {
        m_pendingProposalJob = new P_PendingProposalJob();
        UICallBack.activate(m_pendingProposalJob.getClass().getName() + m_pendingProposalJob.hashCode());
      }
      else {
        m_pendingProposalJob.cancel();
      }
      m_pendingProposalJob.update(text, selectCurrentValue);
      long delay = 400;
      if (m_proposalPopup == null) {
        delay = 0;
      }
      m_pendingProposalJob.schedule(delay);
    }
  }

  private void acceptProposalFromUi() {
    synchronized (m_pendingProposalJobLock) {
      if (m_pendingProposalJob != null) {
        UICallBack.deactivate(m_pendingProposalJob.getClass().getName() + m_pendingProposalJob.hashCode());
        m_pendingProposalJob.cancel();
        m_pendingProposalJob = null;
      }
    }
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().acceptProposalFromUI();
      }
    };
    getUiEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  @Override
  protected IRwtKeyStroke[] getUiKeyStrokes() {
    List<IRwtKeyStroke> strokes = CollectionUtility.copyList(Arrays.asList(super.getUiKeyStrokes()));

    strokes = CollectionUtility.appendList(strokes, new RwtKeyStroke(SWT.ESC) {
      @Override
      public void handleUiAction(Event e) {
        if (hideProposalPopup()) {
          e.doit = false;
        }
      }
    });

    strokes = CollectionUtility.appendList(strokes, new RwtKeyStroke(SWT.CR) {
      @Override
      public void handleUiAction(Event e) {
        if (m_proposalPopup != null) {
          acceptProposalFromUi();
          e.doit = false;
        }
      }
    });

    return CollectionUtility.toArray(strokes, IRwtKeyStroke.class);
  }

  @Override
  protected void handleUiInputVerifier(boolean doit) {
    if (!doit) {
      return;
    }
    handleSwtTraverseVerifier();
  }

  protected boolean handleSwtTraverseVerifier() {
    synchronized (m_pendingProposalJobLock) {
      if (m_pendingProposalJob != null) {
        m_pendingProposalJob.cancel();
        m_pendingProposalJob = null;
      }
    }
    final String text = getUiField().getText();
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, true);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setTextFromUI(text);
        result.setValue(b);
      }
    };
    JobEx job = getUiEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    boolean processed = job.getState() == JobEx.NONE;
    // end notify
    getUiEnvironment().dispatchImmediateUiJobs();
    if (processed && (!result.getValue())) {
      // keep focus
      return false;
    }
    else {
      // advance focus
      return true;
    }
  }

  protected void handleUiBrowseAction() {
    if (getUiBrowseButton().isVisible() && getUiBrowseButton().isEnabled()) {
      requestProposalSupportFromUi(ISmartField.BROWSE_ALL_TEXT, true);
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

  protected void handleTextModifiedFromUi(Event event) {
    if (getUpdateUiFromScoutLock().isAcquired()) {
      return;
    }
    if (getUiField().isVisible() && getUiField().isFocusControl()) {
      String text = getUiField().getText();
      if (text == null || text.length() == 0) {
        // allow empty field without proposal
        if (m_proposalPopup != null) {
          requestProposalSupportFromUi(text, false);
        }
      }
      else {
        requestProposalSupportFromUi(text, false);
      }
    }
  }

  protected void handleTraverseFromUi(Event event) {
    switch (event.keyCode) {
      case SWT.ARROW_DOWN:
      case SWT.ARROW_UP:
      case SWT.ARROW_LEFT:
      case SWT.ARROW_RIGHT:
      case SWT.HOME:
      case SWT.END:
      case SWT.PAGE_DOWN:
      case SWT.PAGE_UP:
      case SWT.CR:
        // void break
        break;
      case SWT.ESC:
        if (m_proposalPopup != null) {
          event.doit = false;
        }
        break;
      default:
        event.doit = handleSwtTraverseVerifier();

    }
  }

  private void notifyPopupEventListeners(int eventType) {
    IPopupSupportListener[] listeners;
    synchronized (m_popupEventListenerLock) {
      listeners = m_popupEventListeners.toArray(new IPopupSupportListener[m_popupEventListeners.size()]);
    }
    for (IPopupSupportListener listener : listeners) {
      listener.handleEvent(eventType);
    }
  }

  @Override
  public void addPopupEventListener(IPopupSupportListener listener) {
    synchronized (m_popupEventListenerLock) {
      m_popupEventListeners.add(listener);
    }
  }

  @Override
  public void removePopupEventListener(IPopupSupportListener listener) {
    synchronized (m_popupEventListenerLock) {
      m_popupEventListeners.remove(listener);
    }
  }

  private class P_UiFieldListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Modify:
          handleTextModifiedFromUi(event);
          break;
        case SWT.Traverse:
          handleTraverseFromUi(event);
          break;
      }
    }
  }

  private class P_KeyListener extends RwtKeyStroke {
    public P_KeyListener(int keyCode) {
      super(keyCode);
    }

    @Override
    public void handleUiAction(Event e) {
      if (m_proposalPopup == null) {
        requestProposalSupportFromUi(ISmartField.BROWSE_ALL_TEXT, true);
      }
      else {
        Widget c = null;
        if (c == null) {
          c = RwtUtility.findChildComponent(m_proposalPopup.getUiContentPane(), Table.class);
        }
        if (c == null) {
          c = RwtUtility.findChildComponent(m_proposalPopup.getUiContentPane(), Tree.class);
        }
        RwtUtility.handleNavigationKey(c, e.keyCode);
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
      getUiEnvironment().getDisplay().asyncExec(this);
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
      if (getUiField().isFocusControl()) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().openProposalFromUI(m_text, m_selectCurrentValue);
          }
        };
        getUiEnvironment().invokeScoutLater(t, 0);
      }
    }

    public void update(String text, boolean selectCurrentValue) {
      m_text = text;
      m_selectCurrentValue = selectCurrentValue;
    }
  }

  private class P_ContextMenuListener extends MenuAdapterEx {
    private static final long serialVersionUID = 1L;

    public P_ContextMenuListener(Control menuControl, Control keyStrokeWidget) {
      super(menuControl, keyStrokeWidget);
    }

    @Override
    protected Menu getContextMenu() {
      return m_contextMenu;
    }

    @Override
    protected void setContextMenu(Menu contextMenu) {
      m_contextMenu = contextMenu;
    }

    @Override
    public void menuShown(MenuEvent e) {
      super.menuShown(e);

      final AtomicReference<IMenu[]> scoutMenusRef = new AtomicReference<IMenu[]>();
      Runnable t = new Runnable() {
        @Override
        public void run() {
          IMenu[] scoutMenus = getScoutObject().getUIFacade().firePopupFromUI();
          scoutMenusRef.set(scoutMenus);
        }
      };
      JobEx job = RwtScoutSmartField.this.getUiEnvironment().invokeScoutLater(t, 1200);
      try {
        job.join(1200);
      }
      catch (InterruptedException ex) {
        //nop
      }
      // grab the actions out of the job, when the actions are providden within
      // the scheduled time the popup will be handled.
      if (scoutMenusRef.get() != null) {
        RwtMenuUtility.fillContextMenu(scoutMenusRef.get(), RwtScoutSmartField.this.getUiEnvironment(), m_contextMenu);
      }
    }
  } // end class P_ContextMenuListener

  private class P_RwtBrowseButtonListener extends SelectionAdapter {
    private static final long serialVersionUID = 1L;

    @Override
    public void widgetSelected(SelectionEvent e) {
      getUiField().forceFocus();
      handleUiBrowseAction();
    }
  } // end class P_RwtBrowseButtonListener

  private class P_F2KeyStroke extends RwtKeyStroke {
    public P_F2KeyStroke() {
      super(SWT.F2);
    }

    @Override
    public void handleUiAction(Event e) {
      handleUiBrowseAction();
    }
  } // end class P_F2KeyStroke
}
