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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rap.rwt.supplemental.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadEvent;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadHandler;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadReceiver;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.ScoutFieldStatus;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.ext.DropDownFileUpload;
import org.eclipse.scout.rt.ui.rap.ext.MenuAdapterEx;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.rap.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;

public class RwtScoutFileChooserField extends RwtScoutValueFieldComposite<IFileChooserField> implements IRwtScoutFileChooserField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutFileChooserField.class);

  private Composite m_fileContainer;
  private DropDownFileUpload m_browseButton;
  private ProgressBar m_progressBar;
  private Menu m_contextMenu;
  private TextFieldEditableSupport m_editableSupport;

  private FileUploadHandler m_handler;
  private FileUploadListener m_uploadListener;
  private File m_uploadedFile = null;

  public RwtScoutFileChooserField() {
    initializeFileUpload();
  }

  @Override
  protected void initializeUi(Composite parent) {
    super.initializeUi(parent);
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    m_fileContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.BORDER);
    m_fileContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FILECHOOSER);

    StyledText textField = new StyledTextEx(m_fileContainer, SWT.SINGLE | getUiEnvironment().getFormToolkit().getOrientation()) {
      private static final long serialVersionUID = 1L;

      @Override
      public void setBackground(Color color) {
        if (getUiProgressBar() != null && !getUiProgressBar().isDisposed()) {
          getUiProgressBar().setBackground(color);
        }
        if (getUiBrowseButton() != null && !getUiBrowseButton().isDisposed()) {
          getUiBrowseButton().setBackground(color);
        }
      }
    };
    getUiEnvironment().getFormToolkit().adapt(textField, false, false);
    // correction to look like a normal text
    textField.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FILECHOOSER);
    //textfield must be disabled. We can't upload the file from it for now.
    textField.setEnabled(false);

    setUiContainer(container);
    setUiLabel(label);
    setUiField(textField);

    createProgressBar();
    createBrowseButton();

    // prevent the button from grabbing focus
    m_fileContainer.setTabList(new Control[]{textField});

    // context menu
    m_contextMenu = new Menu(getUiBrowseButton().getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new P_ContextMenuListener());
    getUiBrowseButton().setMenu(m_contextMenu);

    // listener
//    P_RwtBrowseButtonListener browseButtonListener = new P_RwtBrowseButtonListener();
//    getUiBrowseButton().addSelectionListener(browseButtonListener);

    // layout
    container.setLayout(new LogicalGridLayout(1, 0));

    m_fileContainer.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    m_fileContainer.setLayout(new FormLayout());

    final FormData textLayoutData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    textLayoutData.right = new FormAttachment(100, -20);
    textLayoutData.left = new FormAttachment(0, 0);
    textLayoutData.bottom = new FormAttachment(textField, -1, SWT.BOTTOM);
    textField.setLayoutData(textLayoutData);
  }

  private void createBrowseButton() {
    m_browseButton = getUiEnvironment().getFormToolkit().createDropDownFileUpload(m_fileContainer, SWT.DROP_DOWN);
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
    setBackgroundFromScout(getScoutObject().getBackgroundColor());

    getUiBrowseButton().addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent event) {
        String filename = getUiBrowseButton().getFileName();
        getUiField().setText(filename);
        handleUpload();
      }
    });

    final FormData buttonLayoutData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    buttonLayoutData.left = new FormAttachment(getUiField(), -4, SWT.RIGHT);
    buttonLayoutData.bottom = new FormAttachment(getUiBrowseButton(), -6, SWT.BOTTOM);
    getUiBrowseButton().setLayoutData(buttonLayoutData);

    setEnabledFromScout(getScoutObject().isEnabled());
    m_fileContainer.layout();
  }

  private void createProgressBar() {
    m_progressBar = new ProgressBar(m_fileContainer, SWT.HORIZONTAL | SWT.SMOOTH | getUiEnvironment().getFormToolkit().getOrientation());
    getUiEnvironment().getFormToolkit().adapt(m_progressBar, true, true);
    setBackgroundFromScout(getScoutObject().getBackgroundColor());

    final FormData progressLayoutData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    progressLayoutData.right = new FormAttachment(100, -20);
    progressLayoutData.left = new FormAttachment(0, 0);
    progressLayoutData.top = new FormAttachment(2, 0);
    progressLayoutData.bottom = new FormAttachment(m_progressBar, 4, SWT.BOTTOM);
    m_progressBar.setLayoutData(progressLayoutData);

    m_fileContainer.layout();
  }

  @Override
  public void disposeImpl() {
    super.dispose();
    if (m_uploadListener != null) {
      m_handler.removeUploadListener(m_uploadListener);
      m_uploadListener = null;
    }
    if (m_handler != null) {
      m_handler.dispose();
      m_handler = null;
    }
    if (m_browseButton != null) {
      m_browseButton.dispose();
      m_browseButton = null;
    }
  }

  private void initializeFileUpload() {
    FileUploadReceiver receiver = new DiskFileUploadReceiver();
    m_handler = new FileUploadHandler(receiver);
  }

  @Override
  public DropDownFileUpload getUiBrowseButton() {
    return m_browseButton;
  }

  @Override
  public ProgressBar getUiProgressBar() {
    return m_progressBar;
  }

  @Override
  public StyledText getUiField() {
    return (StyledText) super.getUiField();
  }

  @Override
  protected IRwtKeyStroke[] getUiKeyStrokes() {
    List<IRwtKeyStroke> strokes = CollectionUtility.copyList(Arrays.asList(super.getUiKeyStrokes()));

    strokes = CollectionUtility.appendList(strokes, new RwtKeyStroke(SWT.ESC) {
      @Override
      public void handleUiAction(Event e) {
        if (cancelUpload()) {
          e.doit = false;
        }
      }
    });

    strokes = CollectionUtility.appendList(strokes, new RwtKeyStroke(SWT.CR) {
      @Override
      public void handleUiAction(Event e) {
        handleUiInputVerifier(e.doit);
      }
    });

    return CollectionUtility.toArray(strokes, IRwtKeyStroke.class);
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
    getUiBrowseButton().setButtonEnabled(b);
    //textfield must be disabled. We can't upload the file from it for now.
    getUiField().setEnabled(false);
    if (b) {
      m_fileContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FILECHOOSER);
      getUiBrowseButton().setImage(getUiEnvironment().getIcon("filechooserfield_file.png"));
    }
    else {
      m_fileContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FILECHOOSER_DISABLED);
      getUiBrowseButton().setImage(getUiEnvironment().getIcon("filechooserfield_file_disabled.png"));
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
    // only handle if text has changed
    if (CompareUtility.equals(m_uploadedFile, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return;
    }
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, false);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setTextFromUI(m_uploadedFile.getAbsolutePath());
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

  private void handleUpload() {
    m_uploadedFile = null;
    String url = m_handler.getUploadUrl();
    if (m_uploadListener == null) {
      m_uploadListener = new P_FileUploadListener();
    }
    m_handler.addUploadListener(m_uploadListener);
    getUiBrowseButton().submit(url);
  }

  private boolean cancelUpload() {
    if (m_uploadedFile != null) {
      return false;
    }
    m_handler.removeUploadListener(m_uploadListener);
    m_handler.dispose();
    getUiBrowseButton().dispose();
    getUiProgressBar().dispose();
    getUiField().setText("");

    initializeFileUpload();

    createProgressBar();
    createBrowseButton();
    return true;
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IFileChooserField.PROP_FILE_ICON_ID)) {
      setFileIconIdFromScout((String) newValue);
    }
  }

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

  private class P_FileUploadListener implements FileUploadListener {

    @Override
    public void uploadProgress(final FileUploadEvent uploadEvent) {
      getUiEnvironment().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          double fraction = uploadEvent.getBytesRead() / (double) uploadEvent.getContentLength();
          int percent = (int) Math.floor(fraction * 100);
          if (getUiProgressBar() != null && !getUiProgressBar().isDisposed()) {
            getUiProgressBar().setSelection(percent);
            getUiProgressBar().setToolTipText("Upload progress: " + percent + "%");
          }
        }
      });
    }

    @Override
    public void uploadFinished(final FileUploadEvent uploadEvent) {
      DiskFileUploadReceiver receiver = (DiskFileUploadReceiver) m_handler.getReceiver();
      m_uploadedFile = receiver.getTargetFile();
      getUiEnvironment().getDisplay().asyncExec(new Runnable() {

        @Override
        public void run() {
          if (m_uploadedFile != null) {
            handleUiInputVerifier(true);
          }
        }
      });
      getUiEnvironment().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          int percent = 0;
          if (getUiProgressBar() != null && !getUiProgressBar().isDisposed()) {
            getUiProgressBar().setSelection(percent);
            getUiProgressBar().setToolTipText("");
          }
          m_handler.removeUploadListener(m_uploadListener);
        }
      });
    }

    @Override
    public void uploadFailed(final FileUploadEvent uploadEvent) {
      getUiEnvironment().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          if (getUiLabel() != null) {
            getUiLabel().setStatus(new ScoutFieldStatus(uploadEvent.getException().getMessage(), IStatus.ERROR));
          }
          m_handler.removeUploadListener(m_uploadListener);
        }
      });
    }
  }
}
