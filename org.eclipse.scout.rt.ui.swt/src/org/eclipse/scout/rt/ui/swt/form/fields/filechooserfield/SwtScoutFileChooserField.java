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
package org.eclipse.scout.rt.ui.swt.form.fields.filechooserfield;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.scout.rt.ui.swt.action.AbstractSwtScoutActionPropertyChangeListener;
import org.eclipse.scout.rt.ui.swt.ext.DropDownButton;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class SwtScoutFileChooserField extends SwtScoutValueFieldComposite<IFileChooserField> implements ISwtScoutFileChooserField {

  private DropDownButton m_fileChooserButton;
  private Menu m_contextMenu;
  private TextFieldEditableSupport m_editableSupport;
  private P_ScoutActionPropertyChangeListener m_scoutActionPropertyListener;

  @Override
  protected void initializeSwt(Composite parent) {
    super.initializeSwt(parent);
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    StyledText textField = getEnvironment().getFormToolkit().createStyledText(container, SWT.SINGLE | SWT.BORDER);
    m_fileChooserButton = new DropDownButton(container, SWT.DROP_DOWN);
    m_fileChooserButton.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        getSwtField().setFocus();
      }
    });
    container.setTabList(new Control[]{textField});
    //
    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(textField);

    // context menu
    m_contextMenu = new Menu(m_fileChooserButton.getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new P_ContextMenuListener());
    m_fileChooserButton.setMenu(m_contextMenu);
    // listener
    P_SwtFileChooserButtonListener swtBrowseButtonListener = new P_SwtFileChooserButtonListener();
    getSwtFileChooserButton().addSelectionListener(swtBrowseButtonListener);

    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));
    m_fileChooserButton.setLayoutData(LogicalGridDataBuilder.createSmartButton());
  }

  @Override
  public DropDownButton getSwtFileChooserButton() {
    return m_fileChooserButton;
  }

  @Override
  public StyledText getSwtField() {
    return (StyledText) super.getSwtField();
  }

  /*
   * scout properties
   */
  @Override
  protected void attachScout() {
    super.attachScout();
    setFileIconIdFromScout(getScoutObject().getFileIconId());
    if (m_scoutActionPropertyListener == null) {
      m_scoutActionPropertyListener = new P_ScoutActionPropertyChangeListener();
    }
    m_scoutActionPropertyListener.attachToScoutMenus(getScoutObject().getMenus());
    getSwtFileChooserButton().setDropdownEnabled(calculateDropDownButtonEnabled());
  }

  @Override
  protected void detachScout() {
    if (m_scoutActionPropertyListener != null) {
      m_scoutActionPropertyListener.detachFromScoutMenus(getScoutObject().getMenus());
      m_scoutActionPropertyListener = null;
    }
    super.detachScout();
  }

  private boolean calculateDropDownButtonEnabled() {
    final AtomicBoolean hasValidMenus = new AtomicBoolean(false);
    Runnable t = new Runnable() {
      @Override
      public void run() {
        hasValidMenus.set(getScoutObject().getUIFacade().hasValidMenusFromUI());
      }
    };
    JobEx job = getEnvironment().invokeScoutLater(t, 1200);
    try {
      job.join(1200);
    }
    catch (InterruptedException ex) {
      //nop
    }
    return hasValidMenus.get();
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    getSwtFileChooserButton().setDropdownEnabled(calculateDropDownButtonEnabled());
    if (s == null) {
      s = "";
    }
    getSwtField().setText(s);
    super.handleSwtFocusGained();
    getSwtField().setCaretOffset(0);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_fileChooserButton.setEnabled(b);
    m_fileChooserButton.setDropdownEnabled(calculateDropDownButtonEnabled());
  }

  @Override
  protected void setFieldEnabled(Control swtField, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getSwtField());
    }
    m_editableSupport.setEditable(enabled);
  }

  protected void setFileIconIdFromScout(String s) {
    m_fileChooserButton.setImage(getEnvironment().getIcon(s));
  }

  @Override
  protected boolean handleSwtInputVerifier() {
    final String text = getSwtField().getText();
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
    JobEx job = getEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    getEnvironment().dispatchImmediateSwtJobs();
    // end notify
    return true;// continue always
  }

  @Override
  protected void handleSwtFocusGained() {
    super.handleSwtFocusGained();

    scheduleSelectAll();
  }

  @Override
  protected void handleSwtFocusLost() {
    getSwtField().setSelection(0, 0);
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

  protected void handleSwtFileChooserAction() {
    if (getScoutObject().isVisible() && getScoutObject().isEnabled()) {
      Runnable scoutJob = new Runnable() {
        @Override
        public void run() {
          IFileChooser fc = getScoutObject().getFileChooser();
          final File[] files = fc.startChooser();

          Runnable swtJob = new Runnable() {
            @Override
            public void run() {
              if (files != null && files.length > 0) {
                getSwtField().setText(files[0].getAbsolutePath());
                handleSwtInputVerifier();
              }
            }
          };
          if (getEnvironment() != null) {
            getEnvironment().invokeSwtLater(swtJob);
          }
        }
      };
      getEnvironment().invokeScoutLater(scoutJob, 0);
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IFileChooserField.PROP_FILE_ICON_ID)) {
      setFileIconIdFromScout((String) newValue);
    }
  }

  protected void handleScoutActionPropertyChange(String name, Object newValue) {
    if (IAction.PROP_INHERIT_ACCESSIBILITY.equals(name) || IAction.PROP_EMPTY_SPACE.equals(name) ||
        IAction.PROP_SINGLE_SELECTION.equals(name) || IAction.PROP_VISIBLE.equals(name)) {
      handleDropDownButtonEnabled();
    }
  }

  protected void handleDropDownButtonEnabled() {
    getSwtFileChooserButton().setDropdownEnabled(calculateDropDownButtonEnabled());
  }

  private class P_SwtFileChooserButtonListener extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent e) {
      handleSwtFileChooserAction();
    }
  }

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

  private class P_ScoutActionPropertyChangeListener extends AbstractSwtScoutActionPropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          if (isHandleScoutPropertyChangeSwtThread()) {
            try {
              getUpdateSwtFromScoutLock().acquire();
              //
              handleScoutActionPropertyChange(e.getPropertyName(), e.getNewValue());
            }
            finally {
              getUpdateSwtFromScoutLock().release();
            }
          }
        }
      };
      getEnvironment().invokeSwtLater(t);
    }
  } // end class P_ScoutActionPropertyChangeListener
}
