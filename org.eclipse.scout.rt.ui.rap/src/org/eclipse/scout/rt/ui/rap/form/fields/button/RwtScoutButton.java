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
package org.eclipse.scout.rt.ui.rap.form.fields.button;

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
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtScoutContextMenu;
import org.eclipse.scout.rt.ui.rap.ext.ButtonEx;
import org.eclipse.scout.rt.ui.rap.extension.IUiDecoration;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * @since 3.8.0
 */
public class RwtScoutButton<T extends IButton> extends RwtScoutFieldComposite<T> implements IRwtScoutButton<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutButton.class);

  private ButtonListener m_scoutButtonListener;
  private OptimisticLock m_selectionLock;
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  private RwtContextMenuMarkerComposite m_menuMarkerComposite;

  private RwtScoutContextMenu m_contextMenu;

  public RwtScoutButton() {
    m_selectionLock = new OptimisticLock();
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    m_menuMarkerComposite = new RwtContextMenuMarkerComposite(container, getUiEnvironment(), SWT.NO_FOCUS);
    getUiEnvironment().getFormToolkit().adapt(m_menuMarkerComposite);
    ButtonEx uiFieldAsButton = null;
    Hyperlink uiFieldAsLink = null;
    switch (getScoutObject().getDisplayStyle()) {
      case IButton.DISPLAY_STYLE_RADIO: {
        uiFieldAsButton = createSwtRadioButton(m_menuMarkerComposite, SWT.LEFT | SWT.RADIO | SWT.WRAP);
        break;
      }
      case IButton.DISPLAY_STYLE_TOGGLE: {
        uiFieldAsButton = createSwtToggleButton(m_menuMarkerComposite, SWT.CENTER | SWT.TOGGLE);
        break;
      }
      case IButton.DISPLAY_STYLE_LINK: {
        uiFieldAsLink = createSwtHyperlink(m_menuMarkerComposite, "", SWT.CENTER);
        break;
      }
      default: {
        uiFieldAsButton = createSwtPushButton(m_menuMarkerComposite, SWT.CENTER | SWT.PUSH);
      }
    }
    //
    setUiContainer(container);
    setUiLabel(null);
    if (uiFieldAsButton != null) {
      setUiField(uiFieldAsButton);

      // attach rwt listeners
      uiFieldAsButton.addListener(ButtonEx.SELECTION_ACTION, new P_RwtSelectionListener());

      LogicalGridData gd = (LogicalGridData) getUiField().getLayoutData();
      adaptButtonLayoutData(gd);
    }
    else if (uiFieldAsLink != null) {
      uiFieldAsLink.addHyperlinkListener(new HyperlinkAdapter() {
        @Override
        public void linkActivated(HyperlinkEvent e) {
          handleUiAction();
        }
      });
      setUiField(uiFieldAsLink);
      getUiContainer().setTabList(new Control[]{m_menuMarkerComposite});
    }
    // layout
    getUiContainer().setLayout(new LogicalGridLayout(0, 0));
    m_menuMarkerComposite.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
  }

  @Override
  protected void installContextMenu() {
    m_menuMarkerComposite.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    getScoutObject().getContextMenu().addPropertyChangeListener(new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {

        if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
          final boolean markerVisible = getScoutObject().getContextMenu().isVisible();
          getUiEnvironment().invokeUiLater(new Runnable() {
            @Override
            public void run() {
              m_menuMarkerComposite.setMarkerVisible(markerVisible);
            }
          });
        }
      }
    });
    m_contextMenu = new RwtScoutContextMenu(getUiField().getShell(), getScoutObject().getContextMenu(), m_menuMarkerComposite, getUiEnvironment());
    getUiField().setMenu(getContextMenu().getUiMenu());
  }

  /**
   * @since 4.0.0-M7
   */
  protected Hyperlink createSwtHyperlink(Composite container, String text, int style) {
    HyperlinkGroup linkGroup = getUiEnvironment().getFormToolkit().getHyperlinkGroup();
    linkGroup.setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);
    Hyperlink uiLink = getUiEnvironment().getFormToolkit().createHyperlink(container, "", style);
    return uiLink;
  }

  /**
   * @since 4.0.0-M7
   */
  protected ButtonEx createSwtRadioButton(Composite container, int style) {
    return getUiEnvironment().getFormToolkit().createButtonEx(container, style);
  }

  /**
   * @since 4.0.0-M7
   */
  protected ButtonEx createSwtToggleButton(Composite container, int style) {
    return getUiEnvironment().getFormToolkit().createButtonEx(container, style);
  }

  /**
   * @since 4.0.0-M7
   */
  protected ButtonEx createSwtPushButton(Composite container, int style) {
    ButtonEx swtButton = getUiEnvironment().getFormToolkit().createButtonEx(container, style);
    swtButton.setDropDownEnabled(true);
    return swtButton;
  }

  protected void adaptButtonLayoutData(LogicalGridData gd) {
    //set default button height
    if (!getScoutObject().isProcessButton() || gd.useUiHeight) {
      return;
    }

    gd.useUiHeight = true;
    IUiDecoration deco = UiDecorationExtensionPoint.getLookAndFeel();
    gd.heightHint = deco.getProcessButtonHeight();
  }

  @Override
  protected void setBackgroundFromScout(String scoutColor) {
    // XXX hstaudacher We need to override this method because when not it overrides RWT theme
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IButton b = getScoutObject();
    setIconIdFromScout(b.getIconId());
    setImageFromScout(b.getImage());
    setSelectionFromScout(b.isSelected());
    setLabelFromScout(b.getLabel());
    if (m_scoutButtonListener == null) {
      m_scoutButtonListener = new P_ScoutButtonListener();
      getScoutObject().addButtonListener(m_scoutButtonListener);
    }
  }

  @Override
  protected void detachScout() {
    if (m_scoutButtonListener != null) {
      getScoutObject().removeButtonListener(m_scoutButtonListener);
      m_scoutButtonListener = null;
    }
    super.detachScout();
  }

  protected void setIconIdFromScout(String s) {
    if (s != null) {
      Image icon = getUiEnvironment().getIcon(s);
      Control comp = getUiField();
      if (comp instanceof Button) {
        Button b = (Button) comp;
        b.setImage(icon);
      }
      else if (comp instanceof Hyperlink) {
      }
    }
  }

  @Override
  protected void setLabelFromScout(String s) {
    Control comp = getUiField();
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
  }

  protected void setSelectionFromScout(boolean b) {
    try {
      if (m_selectionLock.acquire()) {
        switch (getScoutObject().getDisplayStyle()) {
          case IButton.DISPLAY_STYLE_RADIO:
          case IButton.DISPLAY_STYLE_TOGGLE:
            Control comp = getUiField();
            if (comp instanceof Button) {
              Button but = (Button) comp;
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

  protected void setSelectionFromUi(final boolean b) {
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
        getUiEnvironment().invokeScoutLater(t, 0);
        //end notify
      }
    }
    finally {
      m_selectionLock.release();
    }
  }

  protected void handleUiAction() {
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
      getUiEnvironment().invokeScoutLater(t, 0);
      //end notify
    }
  }

  protected void setImageFromScout(Object img) {
    if (img instanceof Image) {
      Control comp = getUiField();
      if (comp instanceof Button) {
        Button b = (Button) comp;
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
      getContextMenu().getUiMenu().setVisible(true);
    }
  }

  /**
   * in rwt thread
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

  public RwtScoutContextMenu getContextMenu() {
    return m_contextMenu;
  }

  private class P_RwtSelectionListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case ButtonEx.SELECTION_ACTION: {
          switch (getScoutObject().getDisplayStyle()) {
            case IButton.DISPLAY_STYLE_RADIO:
            case IButton.DISPLAY_STYLE_TOGGLE: {
              setSelectionFromUi(((Button) getUiField()).getSelection());
              break;
            }
            default: {
              handleUiAction();
              break;
            }
          }
          break;
        }
      }
    }
  }// end private class

  private class P_ScoutButtonListener implements ButtonListener, WeakEventListener {
    @Override
    public void buttonChanged(ButtonEvent e) {
      switch (e.getType()) {
        case ButtonEvent.TYPE_DISARM: {
          getUiEnvironment().invokeUiLater(
              new Runnable() {
                @Override
                public void run() {
                  disarmButtonFromScout();
                }
              });
          break;
        }
        case ButtonEvent.TYPE_REQUEST_POPUP: {
          getUiEnvironment().invokeUiLater(
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
