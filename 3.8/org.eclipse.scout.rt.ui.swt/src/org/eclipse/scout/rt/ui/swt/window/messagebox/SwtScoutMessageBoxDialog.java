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
package org.eclipse.scout.rt.ui.swt.window.messagebox;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxEvent;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxListener;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.groupbox.layout.ButtonBarLayout;
import org.eclipse.scout.rt.ui.swt.form.fields.groupbox.layout.ButtonBarLayoutData;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>MessageBoxDialog</h3> ...
 * 
 * @since 1.0.0 13.04.2008
 */
public class SwtScoutMessageBoxDialog extends Dialog {

  private P_ScoutMessageBoxListener m_scoutMessageBoxListener;

  private final ISwtEnvironment m_environment;
  private final IMessageBox m_scoutObject;

  private Label m_imageLabel;

  private Label m_introLabel;

  private StyledText m_actionText;

  public SwtScoutMessageBoxDialog(Shell parentShell, IMessageBox scoutObject, ISwtEnvironment environment) {
    super(parentShell);
    m_scoutObject = scoutObject;
    m_environment = environment;
    int dialogStyle = SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;
    setShellStyle(dialogStyle);
    setBlockOnOpen(false);
  }

  @Override
  public void create() {
    super.create();
    attachScout();
    getShell().addDisposeListener(new P_DisposeListener());
  }

  void attachScout() {
    if (m_scoutMessageBoxListener == null) {
      m_scoutMessageBoxListener = new P_ScoutMessageBoxListener();
      getScoutObject().addMessageBoxListener(m_scoutMessageBoxListener);
    }
    setTitleTextFromScout(getScoutObject().getTitle());
    setIntoTextFromScout(getScoutObject().getIntroText());
    setActionTextFromScout(getScoutObject().getActionText());
    setImageFromScout(getScoutObject().getIconId());
    setSeverityFromScout(getScoutObject().getSeverity());
  }

  protected void setImageFromScout(String iconId) {
    Image icon = getEnvironment().getIcon(iconId);
    setImageInternal(icon);
  }

  private void setImageInternal(Image img) {
    boolean exclude = false;
    if (img == null) {
      exclude = true;
    }
    m_imageLabel.setImage(img);
    if (m_imageLabel.getLayoutData() instanceof GridData) {
      if (((GridData) m_imageLabel.getLayoutData()).exclude != exclude) {
        GridData gridData = ((GridData) m_imageLabel.getLayoutData());
        if (gridData.exclude != exclude) {
          gridData.exclude = exclude;
          m_imageLabel.getParent().getParent().layout(true, true);
          // getShell().layout(true);
        }
      }
    }
  }

  protected void setSeverityFromScout(int severity) {
    if (getScoutObject().getIconId() == null) {
      Image icon = null;
      switch (severity) {
        case IProcessingStatus.ERROR:
          icon = getShell().getDisplay().getSystemImage(SWT.ICON_ERROR);
          break;
        case IProcessingStatus.WARNING:
          icon = getShell().getDisplay().getSystemImage(SWT.ICON_WARNING);
          break;
        case IProcessingStatus.INFO:
          icon = getShell().getDisplay().getSystemImage(SWT.ICON_INFORMATION);
          break;
        default:
          break;
      }
      setImageInternal(icon);
    }
  }

  protected void setTitleTextFromScout(String titleText) {
    if (titleText == null) {
      titleText = "";
    }
    getShell().setText(titleText);
  }

  protected void setIntoTextFromScout(String introText) {
    boolean exclude = false;
    if (introText == null) {
      introText = "";
      exclude = true;
    }
    m_introLabel.setText(introText);
    if (m_introLabel.getLayoutData() instanceof GridData) {
      GridData gridData = ((GridData) m_introLabel.getLayoutData());
      if (gridData.exclude != exclude) {
        gridData.exclude = exclude;
        // getShell().layout(true);
        m_introLabel.getParent().getParent().layout(true, true);
      }
    }
  }

  protected void setActionTextFromScout(String actionText) {
    // When the header text is not set we would like to have the action text in
    // the header area
    if (getScoutObject().getIntroText() == null) {
      setIntoTextFromScout(actionText);
      actionText = null;
    }

    boolean exclude = false;
    if (actionText == null) {
      actionText = "";
      exclude = true;
    }
    m_actionText.setText(actionText);
    if (m_actionText.getLayoutData() instanceof GridData) {
      GridData gridData = ((GridData) m_actionText.getLayoutData());
      if (gridData.exclude != exclude) {
        gridData.exclude = exclude;
        getShell().layout(true, true);
        m_actionText.getParent().getParent().layout(true, true);
      }
    }

  }

  protected void dettachScout() {
    if (m_scoutMessageBoxListener != null) {
      getScoutObject().removeMessageBoxListener(m_scoutMessageBoxListener);
      m_scoutMessageBoxListener = null;
    }
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    Control header = createHeaderArea(container);
    m_actionText = getEnvironment().getFormToolkit().createStyledText(container, SWT.READ_ONLY | SWT.V_SCROLL);
    m_actionText.setWordWrap(true);

    // layout
    container.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

    GridLayout dialogAreaLayout = new GridLayout(1, true);
    dialogAreaLayout.marginHeight = 12;
    dialogAreaLayout.marginWidth = 12;
    container.setLayout(dialogAreaLayout);

    GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
    header.setLayoutData(gridData);
    gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
    gridData.horizontalIndent = 5;
    m_actionText.setLayoutData(gridData);

    // No control in the dialog area should be in the tab list
    container.setTabList(new Control[]{});
    return container;
  }

  private Control createHeaderArea(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    m_imageLabel = getEnvironment().getFormToolkit().createLabel(container, "");
    m_introLabel = getEnvironment().getFormToolkit().createLabel(container, "", SWT.WRAP | SWT.LEFT);

    // layout

    GridLayout headerLayout = new GridLayout(2, false);
    headerLayout.marginBottom = 7;
    headerLayout.marginWidth = 0;
    headerLayout.horizontalSpacing = 5;
    container.setLayout(headerLayout);
    GridData gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
    gridData.exclude = true;
    m_imageLabel.setLayoutData(gridData);
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
    gridData.horizontalIndent = 7;
    gridData.verticalIndent = 0;
    gridData.exclude = true;
    m_introLabel.setLayoutData(gridData);
    return container;
  }

  @Override
  protected Control createButtonBar(Composite parent) {
    Composite buttonArea = getEnvironment().getFormToolkit().createComposite(parent);

    int inset = 10;

    // Set the background color of the button bar
    buttonArea.setBackground(getEnvironment().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

    // yes button
    if (getScoutObject().getYesButtonText() != null) {
      Button yesButton = createButton(buttonArea, getScoutObject().getYesButtonText(), null, IMessageBox.YES_OPTION);
      ButtonBarLayoutData data = new ButtonBarLayoutData();
      data.insetTop = inset;
      data.insetBottom = inset;
      data.insetRight = inset;
      yesButton.setLayoutData(data);
    }
    if (getScoutObject().getNoButtonText() != null) {
      Button noButton = createButton(buttonArea, getScoutObject().getNoButtonText(), null, IMessageBox.NO_OPTION);
      ButtonBarLayoutData data = new ButtonBarLayoutData();
      data.insetTop = inset;
      data.insetBottom = inset;
      data.insetRight = inset;
      noButton.setLayoutData(data);
    }
    if (getScoutObject().getCancelButtonText() != null) {
      Button cancelButton = createButton(buttonArea, getScoutObject().getCancelButtonText(), null, IMessageBox.CANCEL_OPTION);
      ButtonBarLayoutData data = new ButtonBarLayoutData();
      data.insetTop = inset;
      data.insetBottom = inset;
      data.insetRight = inset;
      cancelButton.setLayoutData(data);
    }
    if (getScoutObject().getHiddenText() != null) {
      Button copyButton = createButton(buttonArea, SwtUtility.getNlsText(Display.getCurrent(), "Copy"), null, -1);
      copyButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          Clipboard clipboard = null;
          try {
            clipboard = new Clipboard(getEnvironment().getDisplay());
            String hiddenText = getScoutObject().getHiddenText();
            if (hiddenText != null) {
              clipboard.setContents(new Object[]{hiddenText}, new Transfer[]{TextTransfer.getInstance()});
            }
          }
          finally {
            if (clipboard != null) {
              clipboard.dispose();
              clipboard = null;
            }
          }

        }
      });
      ButtonBarLayoutData data = new ButtonBarLayoutData();
      data.insetTop = inset;
      data.insetBottom = inset;
      data.insetRight = inset;
      copyButton.setLayoutData(data);
    }
    // layout
    // i would expect the container to be spaned over the whole dialog area but
    // ... (SWT)
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    buttonArea.setLayoutData(gridData);

    ButtonBarLayout layout = new ButtonBarLayout(SWT.RIGHT);
    layout.horizontalGap = 0;
    buttonArea.setLayout(layout);
    return buttonArea;
  }

  protected Button createButton(Composite parent, String text, String iconId, int buttonId) {
    Button b = getEnvironment().getFormToolkit().createButton(parent, text, SWT.PUSH);
    if (iconId != null) {
      b.setImage(getEnvironment().getIcon(iconId));
    }
    if (buttonId >= 0) {
      b.addSelectionListener(new P_SwtButtonListener(buttonId));
    }
    return b;
  }

  @Override
  protected Point getInitialSize() {
    // get the preferred width of the button bar
    Point buttonBarSize = getButtonBar().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

    // get the preferred height of the control
    Point initialSize = getShell().computeSize(UiDecorationExtensionPoint.getLookAndFeel().getMessageBoxMinWidth(), SWT.DEFAULT, true);

    // The width and height of the messageBox is defined in the lookAndFeel extension point.
    // If we have a lot of big buttons we would like to scale the dialog in order to show every button. In this case the width of the button bar is used.
    initialSize.x = Math.max(buttonBarSize.x + 20, UiDecorationExtensionPoint.getLookAndFeel().getMessageBoxMinWidth());
    initialSize.y = Math.max(initialSize.y, UiDecorationExtensionPoint.getLookAndFeel().getMessageBoxMinHeight());
    return initialSize;
  }

  @Override
  public int open() {
    if (getShell() == null || getShell().isDisposed()) {
      create();
    }
    initializeBounds();
    return super.open();

  }

  @Override
  public boolean close() {
    handleSwtButtonSelection(IMessageBox.CANCEL_OPTION);
    return false;
  }

  public void closeNoFire() {
    super.close();
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public IMessageBox getScoutObject() {
    return m_scoutObject;
  }

  /*
   * event handlers
   */
  protected void handleScoutMessageBoxClosed(MessageBoxEvent e) {
    // dialog model detach
    dettachScout();
    closeNoFire();
  }

  private void handleSwtButtonSelection(final int buttonId) {
    Runnable j = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setResultFromUI(buttonId);
      }
    };
    getEnvironment().invokeScoutLater(j, 0);
  }

  private class P_SwtButtonListener extends SelectionAdapter {
    private final int m_buttonId;

    P_SwtButtonListener(int buttonId) {
      m_buttonId = buttonId;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      handleSwtButtonSelection(m_buttonId);
    }

  } // end class P_SwtButtonListener

  /*
   * other observers
   */
  private class P_ScoutMessageBoxListener implements MessageBoxListener {
    @Override
    public void messageBoxChanged(final MessageBoxEvent e) {
      switch (e.getType()) {
        case MessageBoxEvent.TYPE_CLOSED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              switch (e.getType()) {
                case MessageBoxEvent.TYPE_CLOSED: {
                  handleScoutMessageBoxClosed(e);
                  break;
                }
              }
            }
          };
          getEnvironment().invokeSwtLater(t);
          break;
        }
      }
    }
  }// end private class

  private class P_DisposeListener implements DisposeListener {

    private static final long serialVersionUID = 1L;

    @Override
    public void widgetDisposed(DisposeEvent event) {
      dettachScout();
    }

  }
}
