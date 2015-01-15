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
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.action.menu.MenuPositionCorrectionListener;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtScoutContextMenu;
import org.eclipse.scout.rt.ui.swt.action.menu.text.StyledTextAccess;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SwtScoutFileChooserField extends SwtScoutValueFieldComposite<IFileChooserField> implements ISwtScoutFileChooserField {

  private Button m_fileChooserButton;
  private TextFieldEditableSupport m_editableSupport;
  private SwtContextMenuMarkerComposite m_menuMarkerComposite;
  private SwtScoutContextMenu m_contextMenu;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  @Override
  protected void initializeSwt(Composite parent) {
    super.initializeSwt(parent);
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

    StyledText textField = getEnvironment().getFormToolkit().createStyledText(m_menuMarkerComposite, SWT.SINGLE);
    textField.setMargins(2, 2, 2, 2);
    m_fileChooserButton = getEnvironment().getFormToolkit().createButton(container, "", SWT.PUSH);
    // listener is used.
    m_fileChooserButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        handleSwtInputVerifier();
      }

      @Override
      public void mouseUp(MouseEvent e) {
        // check left click
        if (e.button == 1) {
          handleSwtFileChooserAction();
        }
      }
    });
    m_fileChooserButton.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        getSwtField().setFocus();
      }
    });

    container.setTabList(new Control[]{m_menuMarkerComposite});
    //
    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(textField);

    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));
    m_menuMarkerComposite.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    m_fileChooserButton.setLayoutData(LogicalGridDataBuilder.createButton1());
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
    getSwtFileChooserButton().setMenu(m_contextMenu.getSwtMenu());

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
  public Button getSwtFileChooserButton() {
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
    installContextMenu();
  }

  @Override
  protected void detachScout() {
    uninstallContextMenu();
    super.detachScout();
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
          final List<File> files = fc.startChooser();

          Runnable swtJob = new Runnable() {
            @Override
            public void run() {
              if (CollectionUtility.hasElements(files)) {
                getSwtField().setText(CollectionUtility.firstElement(files).getAbsolutePath());
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
}
