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

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.scout.rt.ui.swt.ext.DropDownButton;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
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

  @Override
  protected void initializeSwt(Composite parent) {
    super.initializeSwt(parent);
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle, getEnvironment());
    getEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);

    StyledText textField = getEnvironment().getFormToolkit().createStyledText(container, SWT.SINGLE | SWT.BORDER);
    m_fileChooserButton = new DropDownButton(container, SWT.DROP_DOWN);// getEnvironment().getFormToolkit().createButtonEx(container,
    // SWT.DROP_DOWN
    // |SWT.PUSH
    // |
    // SWT.LEFT);
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
    getSwtFileChooserButton().setDropdownEnabled(getScoutObject().hasMenus());
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
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
    getSwtField().setSelection(0, getSwtField().getText().length());
  }

  @Override
  protected void handleSwtFocusLost() {
    getSwtField().setSelection(0, 0);
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
}
