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
package org.eclipse.scout.rt.ui.rap.form.fields.filechooserfield;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.ext.DropDownButton;
import org.eclipse.scout.rt.ui.rap.ext.MenuAdapterEx;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.rap.internal.TextFieldEditableSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class RwtScoutFileChooserField extends RwtScoutValueFieldComposite<IFileChooserField> implements IRwtScoutFileChooserField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutFileChooserField.class);

  private Composite m_fileContainer;
  private DropDownButton m_browseButton;
  private Menu m_contextMenu;
  private TextFieldEditableSupport m_editableSupport;

  @Override
  protected void initializeUi(Composite parent) {
    super.initializeUi(parent);
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle);
    getUiEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);

    m_fileContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.BORDER);
    m_fileContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FILECHOOSER);
    StyledText textField = new StyledTextEx(m_fileContainer, SWT.SINGLE) {
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
    textField.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FILECHOOSER);
    m_browseButton = getUiEnvironment().getFormToolkit().createDropDownButton(m_fileContainer, SWT.DROP_DOWN);
    m_browseButton.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FILECHOOSER);
    // to ensure the text is validated on a context menu call this mouse
    // listener is used.
    m_browseButton.addMouseListener(new MouseAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void mouseDown(MouseEvent e) {
        handleUiInputVerifier(true);
      }
    });
    m_browseButton.addFocusListener(new FocusAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void focusGained(FocusEvent e) {
        getUiField().setFocus();
      }
    });

    setUiContainer(container);
    setUiLabel(label);
    setUiField(textField);
    // prevent the button from grabbing focus
    m_fileContainer.setTabList(new Control[]{textField});

    // context menu
    m_contextMenu = new Menu(m_browseButton.getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new P_ContextMenuListener());
    m_browseButton.setMenu(m_contextMenu);

    // listener
    P_RwtBrowseButtonListener browseButtonListener = new P_RwtBrowseButtonListener();
    getUiBrowseButton().addSelectionListener(browseButtonListener);

    // layout
    container.setLayout(new LogicalGridLayout(1, 0));

    m_fileContainer.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    m_fileContainer.setLayout(new FormLayout());

    final FormData textLayoutData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    textLayoutData.right = new FormAttachment(100, -20);
    textLayoutData.left = new FormAttachment(0, 0);
    textLayoutData.bottom = new FormAttachment(textField, -1, SWT.BOTTOM);
    textField.setLayoutData(textLayoutData);

    final FormData buttonLayoutData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    buttonLayoutData.left = new FormAttachment(textField, 0, SWT.RIGHT);
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

  /*
   * scout properties
   */
  @Override
  protected void attachScout() {
    super.attachScout();
    setFileIconIdFromScout(getScoutObject().getFileIconId());
    getUiBrowseButton().setDropdownEnabled(getScoutObject().hasMenus());
    if (getScoutObject().isFolderMode()) {
      String msg = "IFileChooserField.isFolderMode() == true is not possible in RAP";
      LOG.error(msg);
      getUiLabel().setStatus(new ProcessingStatus(msg, IProcessingStatus.ERROR));
    }
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    if (s == null) {
      s = "";
    }
    getUiField().setText(s);
    super.handleUiFocusGained();
    getUiField().setCaretOffset(0);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_browseButton.setButtonEnabled(b);
    getUiField().setEnabled(b);
    if (b) {
      m_fileContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FILECHOOSER);
    }
    else {
      m_fileContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FILECHOOSER_DISABLED);
    }
  }

  @Override
  protected void setFieldEnabled(Control field, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getUiField());
    }
    m_editableSupport.setEditable(enabled);
  }

  protected void setFileIconIdFromScout(String s) {
    LOG.warn("IFileChooserField.getConfiguredFileIconId(\"...\") is not possible in RAP. Changing FileIcon must be done via CSS.");
  }

  @Override
  protected void handleUiInputVerifier(boolean doit) {
    if (!doit) {
      return;
    }
    final String text = getUiField().getText();
    // only handle if text has changed
    if (CompareUtility.equals(text, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return;
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
    JobEx job = getUiEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    getUiEnvironment().dispatchImmediateUiJobs();
    // end notify
    return;
  }

  @Override
  protected void handleUiFocusGained() {
    super.handleUiFocusGained();
    getUiField().setSelection(0, getUiField().getText().length());
  }

  @Override
  protected void handleUiFocusLost() {
    getUiField().setSelection(0, 0);
  }

  protected void handleUiFileChooserAction() {
    if (getScoutObject().isVisible() && getScoutObject().isEnabled()) {
      Runnable scoutJob = new Runnable() {
        @Override
        public void run() {
          IFileChooser fc = getScoutObject().getFileChooser();
          final File[] files = fc.startChooser();

          Runnable uiJob = new Runnable() {
            @Override
            public void run() {
              if (files != null && files.length > 0) {
                getUiField().setText(files[0].getAbsolutePath());
                handleUiInputVerifier(true);
              }
            }
          };
          if (getUiEnvironment() != null) {
            getUiEnvironment().invokeUiLater(uiJob);
          }
        }
      };
      getUiEnvironment().invokeScoutLater(scoutJob, 0);
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IFileChooserField.PROP_FILE_ICON_ID)) {
      setFileIconIdFromScout((String) newValue);
    }
  }

  private class P_RwtBrowseButtonListener extends SelectionAdapter {
    private static final long serialVersionUID = 1L;

    @Override
    public void widgetSelected(SelectionEvent e) {
      getUiField().forceFocus();
      handleUiFileChooserAction();
    }
  } // end class P_RwtBrowseButtonListener

  private class P_ContextMenuListener extends MenuAdapterEx {

    private static final long serialVersionUID = 1L;

    public P_ContextMenuListener() {
      super(RwtScoutFileChooserField.this.getUiBrowseButton(), RwtScoutFileChooserField.this.getUiBrowseButton().getParent());
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
      JobEx job = RwtScoutFileChooserField.this.getUiEnvironment().invokeScoutLater(t, 1200);
      try {
        job.join(1200);
      }
      catch (InterruptedException ex) {
        //nop
      }
      // grab the actions out of the job, when the actions are providden within
      // the scheduled time the popup will be handled.
      if (scoutMenusRef.get() != null) {
        RwtMenuUtility.fillContextMenu(scoutMenusRef.get(), RwtScoutFileChooserField.this.getUiEnvironment(), m_contextMenu);
      }
    }
  } // end class P_ContextMenuListener
}
