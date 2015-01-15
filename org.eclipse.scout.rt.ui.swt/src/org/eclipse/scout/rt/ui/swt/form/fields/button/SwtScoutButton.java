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
package org.eclipse.scout.rt.ui.swt.form.fields.button;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonEvent;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonListener;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.action.menu.MenuPositionCorrectionListener;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtScoutContextMenu;
import org.eclipse.scout.rt.ui.swt.ext.MultilineButton;
import org.eclipse.scout.rt.ui.swt.ext.MultilineRadioButton;
import org.eclipse.scout.rt.ui.swt.extension.IUiDecoration;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * <h3>SwtScoutButton</h3> ...
 *
 * @since 1.0.0 07.04.2008
 */
public class SwtScoutButton<T extends IButton> extends SwtScoutFieldComposite<T> implements ISwtScoutButton<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutButton.class);

  private ButtonListener m_scoutButtonListener;
  private OptimisticLock m_selectionLock;
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  private SwtScoutContextMenu m_contextMenu;

  private SwtContextMenuMarkerComposite m_menuMarkerComposite;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  public SwtScoutButton() {
    m_selectionLock = new OptimisticLock();
  }

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    m_menuMarkerComposite = new SwtContextMenuMarkerComposite(container, getEnvironment(), SWT.NONE);
    getEnvironment().getFormToolkit().adapt(m_menuMarkerComposite);
    m_menuMarkerComposite.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        getSwtField().setFocus();
        m_contextMenu.getSwtMenu().setVisible(true);
      }
    });
    Control swtFieldAsButton = null;
    Hyperlink swtFieldAsLink = null;
    switch (getScoutObject().getDisplayStyle()) {
      case IButton.DISPLAY_STYLE_RADIO: {
        swtFieldAsButton = createSwtRadioButton(m_menuMarkerComposite, SWT.NONE);
        m_menuMarkerComposite.setMarkerLabelTopMargin(4);
        break;
      }
      case IButton.DISPLAY_STYLE_TOGGLE: {
        Button swtButton = createSwtToggleButton(m_menuMarkerComposite, SWT.CENTER | SWT.TOGGLE);
        m_menuMarkerComposite.setMarkerLabelTopMargin(0);
        swtFieldAsButton = swtButton;
        break;
      }
      case IButton.DISPLAY_STYLE_LINK: {
        Hyperlink swtLink = createSwtHyperlink(m_menuMarkerComposite, "", SWT.CENTER);
        m_menuMarkerComposite.setMarkerLabelTopMargin(3);
        swtFieldAsLink = swtLink;
        break;
      }
      default: {
        swtFieldAsButton = createSwtPushButton(m_menuMarkerComposite, SWT.CENTER | SWT.PUSH);
        m_menuMarkerComposite.setMarkerLabelTopMargin(0);
      }
    }
    //
    setSwtContainer(container);
    setSwtLabel(null);
    LogicalGridData contextMenuMarkerData = LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData());
    if (swtFieldAsButton != null) {

      // attach swt listeners
      swtFieldAsButton.addListener(SWT.Selection, new P_SwtSelectionListener());
      setSwtField(swtFieldAsButton);
      //auto process button height
      adaptButtonLayoutData(contextMenuMarkerData);

    }
    else if (swtFieldAsLink != null) {
      swtFieldAsLink.addHyperlinkListener(new HyperlinkAdapter() {
        @Override
        public void linkActivated(HyperlinkEvent e) {
          handleSwtAction();
        }

      });
      setSwtField(swtFieldAsLink);
    }
    getSwtContainer().setTabList(new Control[]{m_menuMarkerComposite});
    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(0, 0));
    m_menuMarkerComposite.setLayoutData(contextMenuMarkerData);
  }

  /**
   * @since 4.0.0-M7
   */
  protected void adaptButtonLayoutData(LogicalGridData gd) {

    //set default button height
    if (getScoutObject().isProcessButton() && !gd.useUiHeight) {
      gd.useUiHeight = true;
      IUiDecoration deco = UiDecorationExtensionPoint.getLookAndFeel();
      gd.heightHint = deco.getProcessButtonHeight();
    }
  }

  /**
   * @since 4.0.0-M7
   */
  protected Hyperlink createSwtHyperlink(Composite container, String text, int style) {
    Hyperlink link = getEnvironment().getFormToolkit().createHyperlink(container, text, style);
    link.setUnderlined(true);
    return link;
  }

  /**
   * @since 4.0.0-M7
   */
  protected Control createSwtRadioButton(Composite container, int style) {
    return getEnvironment().getFormToolkit().createMultilineRadioButton(container);
  }

  /**
   * @since 4.0.0-M7
   */
  protected Button createSwtToggleButton(Composite container, int style) {
    return getEnvironment().getFormToolkit().createButton(container, style);
  }

  /**
   * @since 4.0.0-M7
   */
  protected Button createSwtPushButton(Composite container, int style) {
    Button swtButton = getEnvironment().getFormToolkit().createButton(container, style);
    return swtButton;
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
    getSwtField().setMenu(m_contextMenu.getSwtMenu());
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
  protected void attachScout() {
    super.attachScout();
    if (m_scoutButtonListener == null) {
      m_scoutButtonListener = new P_ScoutButtonListener();
      getScoutObject().addButtonListener(m_scoutButtonListener);
    }
    installContextMenu();
  }

  @Override
  protected void detachScout() {
    if (m_scoutButtonListener != null) {
      getScoutObject().removeButtonListener(m_scoutButtonListener);
      m_scoutButtonListener = null;
    }
    uninstallContextMenu();
    super.detachScout();
  }

  @Override
  public Control getSwtField() {
    return super.getSwtField();
  }

  public SwtScoutContextMenu getContextMenu() {
    return m_contextMenu;
  }

  @Override
  protected void applyScoutProperties() {
    super.applyScoutProperties();
    IButton b = getScoutObject();
    setIconIdFromScout(b.getIconId());
    setImageFromScout(b.getImage());
    setSelectionFromScout(b.isSelected());
    setLabelFromScout(b.getLabel());
  }

  protected void setIconIdFromScout(String s) {
    if (s != null) {
      Image icon = getEnvironment().getIcon(s);
      Control comp = getSwtField();
      if (comp instanceof Button) {
        Button b = (Button) comp;
        b.setImage(icon);
      }
      else if (comp instanceof MultilineButton) {
        MultilineButton b = (MultilineButton) comp;
        b.setImage(icon);
      }
      else if (comp instanceof Hyperlink) {
      }
    }
  }

  @Override
  protected void setLabelFromScout(String s) {
    Control comp = getSwtField();
    if (comp instanceof Button) {
      Button b = (Button) comp;
      String label = s;
      b.setText(label == null ? "" : label);
    }
    else if (comp instanceof Hyperlink) {
      Hyperlink t = (Hyperlink) comp;
      String label = StringUtility.removeMnemonic(s);
      t.setText(label == null ? "" : label);
    }
    else if (comp instanceof MultilineRadioButton) {
      MultilineRadioButton b = (MultilineRadioButton) comp;
      b.setText(s == null ? "" : s);
    }
  }

  protected void setSelectionFromScout(boolean b) {
    try {
      if (m_selectionLock.acquire()) {
        switch (getScoutObject().getDisplayStyle()) {
          case IButton.DISPLAY_STYLE_RADIO:
          case IButton.DISPLAY_STYLE_TOGGLE:
            Control comp = getSwtField();
            if (comp instanceof Button) {
              Button but = (Button) comp;
              if (b != but.getSelection()) {
                but.setSelection(b);
              }
            }
            else if (comp instanceof MultilineRadioButton) {
              MultilineRadioButton but = (MultilineRadioButton) comp;
              if (b != but.getSelection()) {
                but.setSelection(b);
              }
            }
            break;
        }
      }

    }
    finally {
      m_selectionLock.release();
    }
  }

  protected void setSelectionFromSwt(final boolean b) {
    try {
      if (m_selectionLock.acquire()) {
        //notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            if (getScoutObject().isSelected() != b) {
              getScoutObject().getUIFacade().setSelectedFromUI(b);
            }
            getScoutObject().getUIFacade().fireButtonClickedFromUI();
          }
        };
        getEnvironment().invokeScoutLater(t, 0);
        //end notify
      }
    }
    finally {
      m_selectionLock.release();
    }
  }

  protected void handleSwtAction() {
    if (SwtUtility.runSwtInputVerifier()) {
      if (!m_handleActionPending) {
        m_handleActionPending = true;
        //notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              getScoutObject().getUIFacade().fireButtonClickedFromUI();
            }
            finally {
              m_handleActionPending = false;
            }
          }
        };
        getEnvironment().invokeScoutLater(t, 0);
        //end notify
      }
    }
  }

  protected void setImageFromScout(Object img) {
    if (img instanceof Image) {
      Control comp = getSwtField();
      if (comp instanceof Button) {
        Button b = (Button) comp;
        b.setImage((Image) img);
      }
      else if (comp instanceof MultilineButton) {
        MultilineButton b = (MultilineButton) comp;
        b.setImage((Image) img);
      }
      else if (comp instanceof Hyperlink) {
      }
    }
  }

  protected void disarmButtonFromScout() {
  }

  protected void requestPopupFromScout() {
    if (getContextMenu() != null) {
      getContextMenu().getSwtMenu().setVisible(true);
    }
  }

  /**
   * in swt thread
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IButton.PROP_ICON_ID)) {
      setIconIdFromScout((String) newValue);
    }
    else if (name.equals(IButton.PROP_IMAGE)) {
      setImageFromScout(newValue);
    }
    else if (name.equals(IButton.PROP_SELECTED)) {
      setSelectionFromScout(((Boolean) newValue).booleanValue());
    }
  }

  private class P_SwtSelectionListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      if (event.type == SWT.Selection) {
        switch (getScoutObject().getDisplayStyle()) {
          case IButton.DISPLAY_STYLE_RADIO:
          case IButton.DISPLAY_STYLE_TOGGLE: {
            if (getSwtField() instanceof Button) {
              setSelectionFromSwt(((Button) getSwtField()).getSelection());
            }
            else if (getSwtField() instanceof MultilineRadioButton) {
              setSelectionFromSwt(((MultilineRadioButton) getSwtField()).getSelection());
            }
            break;
          }
          default: {
            handleSwtAction();
            break;
          }
        }
      }
    }
  }// end private class

  private class P_ScoutButtonListener implements ButtonListener, WeakEventListener {
    @Override
    public void buttonChanged(ButtonEvent e) {
      switch (e.getType()) {
        case ButtonEvent.TYPE_DISARM: {
          getEnvironment().invokeSwtLater(
              new Runnable() {
                @Override
                public void run() {
                  disarmButtonFromScout();
                }
              });
          break;
        }
        case ButtonEvent.TYPE_REQUEST_POPUP: {
          getEnvironment().invokeSwtLater(
              new Runnable() {
                @Override
                public void run() {
                  requestPopupFromScout();
                }
              });
          break;
        }
      }
    }
  } // end class P_ScoutButtonListener
}
