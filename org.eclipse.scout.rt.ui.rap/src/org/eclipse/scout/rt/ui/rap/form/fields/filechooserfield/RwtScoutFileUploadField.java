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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rap.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.fileupload.FileUploadEvent;
import org.eclipse.rap.fileupload.FileUploadHandler;
import org.eclipse.rap.fileupload.FileUploadListener;
import org.eclipse.rap.fileupload.FileUploadReceiver;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.ScoutFieldStatus;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtScoutContextMenu;
import org.eclipse.scout.rt.ui.rap.ext.DropDownFileUpload;
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;

public class RwtScoutFileUploadField extends RwtScoutValueFieldComposite<IFileChooserField> implements IRwtScoutFileUploadField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutFileUploadField.class);

  private Composite m_fileContainer;
  private DropDownFileUpload m_browseButton;
  private ProgressBar m_progressBar;
  private TextFieldEditableSupport m_editableSupport;

  private FileUploadHandler m_handler;
  private P_FileUploadListener m_uploadListener;
  private File m_uploadedFile = null;
  private String m_originalVariant = "";

  private RwtContextMenuMarkerComposite m_menuMarkerComposite;
  private RwtScoutContextMenu m_uiContextMenu;
  private P_ContextMenuPropertyListener m_contextMenuPropertyListener;

  public RwtScoutFileUploadField() {
    initializeFileUpload();
  }

  @Override
  protected void initializeUi(Composite parent) {
    super.initializeUi(parent);
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    m_fileContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.BORDER);
    m_fileContainer.setData(RWT.CUSTOM_VARIANT, VARIANT_FILECHOOSER);

    m_menuMarkerComposite = new RwtContextMenuMarkerComposite(m_fileContainer, getUiEnvironment(), SWT.NONE);
    getUiEnvironment().getFormToolkit().adapt(m_menuMarkerComposite);
    m_menuMarkerComposite.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        if (getUiContextMenu() != null) {
          Menu uiMenu = getUiContextMenu().getUiMenu();
          if (e.widget instanceof Control) {
            Point loc = ((Control) e.widget).toDisplay(e.x, e.y);
            uiMenu.setLocation(RwtMenuUtility.getMenuLocation(getScoutObject().getContextMenu().getChildActions(), uiMenu, loc, getUiEnvironment()));
          }
          uiMenu.setVisible(true);
        }
      }
    });

    StyledText textField = new StyledTextEx(m_menuMarkerComposite, SWT.SINGLE | getUiEnvironment().getFormToolkit().getOrientation()) {
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
    textField.setData(RWT.CUSTOM_VARIANT, VARIANT_FILECHOOSER);
    //textfield must be disabled. We can't upload the file from it for now.
    textField.setEnabled(false);

    setUiContainer(container);
    setUiLabel(label);
    setUiField(textField);

    createBrowseButton();

    // prevent the button from grabbing focus
    m_fileContainer.setTabList(new Control[]{m_menuMarkerComposite});

    // layout
    container.setLayout(new LogicalGridLayout(1, 0));

    m_fileContainer.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    m_fileContainer.setLayout(new FormLayout());

    final FormData textLayoutData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    textLayoutData.left = new FormAttachment(0, 0);
    textLayoutData.right = new FormAttachment(100, -20);
    textLayoutData.bottom = new FormAttachment(m_menuMarkerComposite, -1, SWT.BOTTOM);
    m_menuMarkerComposite.setLayoutData(textLayoutData);
  }

  private void createBrowseButton() {
    m_browseButton = getUiEnvironment().getFormToolkit().createDropDownFileUpload(m_fileContainer, SWT.DROP_DOWN);
    m_browseButton.setData(RWT.CUSTOM_VARIANT, VARIANT_FILECHOOSER);
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
        if (filename == null) {
          return;
        }

        getUiField().setText(filename);
        resetUpload();
        handleUpload();
      }
    });

    final FormData buttonLayoutData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    buttonLayoutData.left = new FormAttachment(m_menuMarkerComposite, 0, SWT.RIGHT);
    buttonLayoutData.bottom = new FormAttachment((Control) getUiBrowseButton(), 0, SWT.BOTTOM);
    buttonLayoutData.height = 20;
    buttonLayoutData.width = 20;
    getUiBrowseButton().setLayoutData(buttonLayoutData);

    setEnabledFromScout(getScoutObject().isEnabled());
//    m_browseButton.setDropdownEnabled(calculateDropDownButtonEnabled());
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
  public void handleUiDispose() {
    super.handleUiDispose();
    if (m_uploadListener != null) {
      m_uploadListener.cancelUpload();
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

  public RwtScoutContextMenu getUiContextMenu() {
    return m_uiContextMenu;
  }

  @Override
  protected IRwtKeyStroke[] getUiKeyStrokes() {
    return new IRwtKeyStroke[]{
        new RwtKeyStroke(SWT.ESC) {
          @Override
          public void handleUiAction(Event e) {
            if (cancelUpload()) {
              e.doit = false;
            }
          }
        },
        new RwtKeyStroke(SWT.CR) {
          @Override
          public void handleUiAction(Event e) {
            handleUiInputVerifier(e.doit);
          }
        }};
  }

  /*
   * scout properties
   */
  @Override
  protected void attachScout() {
    super.attachScout();
    setFileIconIdFromScout(getScoutObject().getFileIconId());
    if (getScoutObject().isFolderMode()) {
      String msg = "IFileChooserField.isFolderMode() == true is not possible in RAP";
      LOG.error(msg);
      getUiLabel().setStatus(new ProcessingStatus(msg, IProcessingStatus.ERROR));
    }
    // context menu
    updateContextMenuVisibilityFromScout();
    if (getScoutObject().getContextMenu() != null && m_contextMenuPropertyListener == null) {
      m_contextMenuPropertyListener = new P_ContextMenuPropertyListener();
      getScoutObject().getContextMenu().addPropertyChangeListener(IContextMenu.PROP_VISIBLE, m_contextMenuPropertyListener);
    }
  }

  @Override
  protected void detachScout() {
    // context menu listener
    if (m_contextMenuPropertyListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(IContextMenu.PROP_VISIBLE, m_contextMenuPropertyListener);
      m_contextMenuPropertyListener = null;
    }
    super.detachScout();
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
//    getUiBrowseButton().setDropdownEnabled(calculateDropDownButtonEnabled());
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

    if (!StringUtility.hasText(m_originalVariant)) {
      m_originalVariant = (String) m_fileContainer.getData(RWT.CUSTOM_VARIANT);
    }
    String customVariant = b ? m_originalVariant : m_originalVariant + VARIANT_DISABLED_SUFFIX;
    m_fileContainer.setData(RWT.CUSTOM_VARIANT, customVariant);
  }

  @Override
  protected void setFieldEnabled(Control field, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getUiField());
    }
    m_editableSupport.setEditable(enabled);
  }

  protected void setFileIconIdFromScout(String s) {
    m_originalVariant = s;
    m_fileContainer.setData(RWT.CUSTOM_VARIANT, s);
    getUiField().setData(RWT.CUSTOM_VARIANT, s);
    getUiBrowseButton().setData(RWT.CUSTOM_VARIANT, s);
  }

  protected void updateContextMenuVisibilityFromScout() {
    m_menuMarkerComposite.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    if (getScoutObject().getContextMenu().isVisible()) {
      if (m_uiContextMenu == null) {
        m_uiContextMenu = new RwtScoutContextMenu(getUiField().getShell(), getScoutObject().getContextMenu(), getUiEnvironment());
        if (getUiBrowseButton() != null) {
          getUiBrowseButton().setMenu(m_uiContextMenu.getUiMenu());
        }
      }
    }
    else {
      if (getUiBrowseButton() != null) {
        getUiBrowseButton().setMenu(null);
      }
      if (m_uiContextMenu != null) {
        m_uiContextMenu.dispose();
      }
      m_uiContextMenu = null;
    }
  }

  @Override
  protected void handleUiInputVerifier(boolean doit) {
    if (!doit) {
      return;
    }
    if (m_uploadedFile == null) {
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
    m_uploadListener.startUpload();
    createProgressBar();
    getUiBrowseButton().submit(url);
  }

  private void resetUpload() {
    disposeHandler();
    disposeProgressBar();
    initializeFileUpload();
  }

  private boolean cancelUpload() {
    disposeHandler();
    disposeBrowseButton();
    disposeProgressBar();
    getUiField().setText("");

    initializeFileUpload();

    createBrowseButton();

    return true;
  }


  private void disposeBrowseButton() {
    DropDownFileUpload uiBrowseButton = getUiBrowseButton();
    if (uiBrowseButton != null && !uiBrowseButton.isDisposed()) {
      uiBrowseButton.dispose();
    }
  }

  private void disposeProgressBar() {
    ProgressBar uiProgressBar = getUiProgressBar();
    if (uiProgressBar != null && !uiProgressBar.isDisposed()) {
      uiProgressBar.dispose();
    }
  }

  private void disposeHandler() {
    if (m_uploadListener != null) {
      m_handler.removeUploadListener(m_uploadListener);
      m_uploadListener.cancelUpload();
      m_uploadListener = null;
    }
    m_handler.dispose();
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IFileChooserField.PROP_FILE_ICON_ID)) {
      setFileIconIdFromScout((String) newValue);
    }
  }

  private class P_FileUploadListener implements FileUploadListener {

    private final ServerPushSession m_pushSession;
    private int m_oldPercentage = 0;

    public P_FileUploadListener() {
      m_pushSession = new ServerPushSession();
    }

    public void startUpload() {
      m_pushSession.start();
    }

    public void cancelUpload() {
      m_pushSession.stop();
    }

    private int getPercentage(FileUploadEvent uploadEvent) {
      double bytesRead = uploadEvent.getBytesRead();
      double contentLength = uploadEvent.getContentLength();
      double fraction = bytesRead / contentLength;
      return (int) Math.floor(fraction * 100);
    }

    @Override
    public void uploadProgress(final FileUploadEvent uploadEvent) {
      getUiEnvironment().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          int percent = getPercentage(uploadEvent);
          if (percent != m_oldPercentage && getUiProgressBar() != null && !getUiProgressBar().isDisposed()) {
            m_oldPercentage = percent;
            getUiProgressBar().setSelection(percent);
            getUiProgressBar().setToolTipText("Upload progress: " + percent + "%");
          }
        }
      });
    }

    @Override
    public void uploadFinished(final FileUploadEvent uploadEvent) {
      DiskFileUploadReceiver receiver = (DiskFileUploadReceiver) m_handler.getReceiver();
      File[] uploadedFiles = receiver.getTargetFiles();
      if (uploadedFiles != null && uploadedFiles.length > 0) {
        m_uploadedFile = uploadedFiles[0]; // only supports one file upload.
      }
      else {
        m_uploadedFile = null;
      }
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
          if (getUiProgressBar() != null && !getUiProgressBar().isDisposed()) {
            getUiProgressBar().dispose();
          }
          m_handler.removeUploadListener(m_uploadListener);
        }
      });
      m_pushSession.stop();
    }

    @Override
    public void uploadFailed(final FileUploadEvent uploadEvent) {
      getUiEnvironment().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          if (getUiLabel() != null) {
            getUiLabel().setStatus(new ScoutFieldStatus(uploadEvent.getException().getMessage(), IStatus.ERROR));
          }
          if (getUiProgressBar() != null && !getUiProgressBar().isDisposed()) {
            getUiProgressBar().dispose();
          }
          m_handler.removeUploadListener(m_uploadListener);
        }
      });
      m_pushSession.stop();
    }
  }

  private class P_ContextMenuPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (IContextMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
        // synchronize
        getUiEnvironment().invokeUiLater(new Runnable() {
          @Override
          public void run() {
            updateContextMenuVisibilityFromScout();
          }
        });
      }
    }
  }
}
