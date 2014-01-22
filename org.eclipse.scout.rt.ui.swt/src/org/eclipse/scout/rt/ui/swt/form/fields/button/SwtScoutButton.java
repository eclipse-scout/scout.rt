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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonEvent;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonListener;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.scout.rt.ui.swt.ext.ButtonEx;
import org.eclipse.scout.rt.ui.swt.ext.MultilineButton;
import org.eclipse.scout.rt.ui.swt.ext.MultilineRadioButton;
import org.eclipse.scout.rt.ui.swt.extension.IUiDecoration;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * <h3>SwtScoutButton</h3> ...
 * 
 * @since 1.0.0 07.04.2008
 */
public class SwtScoutButton extends SwtScoutFieldComposite<IButton> implements ISwtScoutButton {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutButton.class);

  private ButtonListener m_scoutButtonListener;
  private OptimisticLock m_selectionLock;
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  private Menu m_contextMenu;

  public SwtScoutButton() {
    m_selectionLock = new OptimisticLock();
  }

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    setSwtContainer(container);
    Control swtFieldAsButton = null;
    Hyperlink swtFieldAsLink = null;
    switch (getScoutObject().getDisplayStyle()) {
      case IButton.DISPLAY_STYLE_RADIO: {
        MultilineRadioButton btn = getEnvironment().getFormToolkit().createMultilineRadioButton(container);
        swtFieldAsButton = btn;
        break;
      }
      case IButton.DISPLAY_STYLE_TOGGLE: {
        ButtonEx swtButton = getEnvironment().getFormToolkit().createButtonEx(container, SWT.CENTER | SWT.TOGGLE);
        swtFieldAsButton = swtButton;
        break;
      }
      case IButton.DISPLAY_STYLE_LINK: {
        int style = SWT.CENTER;
        Hyperlink swtLink = getEnvironment().getFormToolkit().createHyperlink(container, "", style);
        swtLink.setUnderlined(true);
        swtFieldAsLink = swtLink;
        break;
      }
      default: {
        int style = SWT.CENTER | SWT.PUSH;
        if (getScoutObject().hasMenus()) {
          style |= SWT.DROP_DOWN;
        }
        ButtonEx swtButton = getEnvironment().getFormToolkit().createButtonEx(container, style);
        swtButton.setDropDownEnabled(true);
        swtFieldAsButton = swtButton;
      }
    }
    //
    setSwtLabel(null);
    if (swtFieldAsButton != null) {
      // context menu
      m_contextMenu = new Menu(swtFieldAsButton.getShell(), SWT.POP_UP);
      m_contextMenu.addMenuListener(new P_ContextMenuListener());
      swtFieldAsButton.setMenu(m_contextMenu);
      // attach swt listeners
      swtFieldAsButton.addListener(ButtonEx.SELECTION_ACTION, new P_SwtSelectionListener());
      setSwtField(swtFieldAsButton);
      //auto process button height
      LogicalGridData gd = (LogicalGridData) swtFieldAsButton.getLayoutData();
      //set default button height
      if (getScoutObject().isProcessButton() && !gd.useUiHeight) {
        gd.useUiHeight = true;
        IUiDecoration deco = UiDecorationExtensionPoint.getLookAndFeel();
        gd.heightHint = deco.getProcessButtonHeight();
      }
    }
    else if (swtFieldAsLink != null) {
      swtFieldAsLink.addHyperlinkListener(new HyperlinkAdapter() {
        @Override
        public void linkActivated(HyperlinkEvent e) {
          handleSwtAction();
        }

      });
      setSwtField(swtFieldAsLink);
      getSwtContainer().setTabList(new Control[]{swtFieldAsLink});
    }
    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(0, 0));
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_scoutButtonListener == null) {
      m_scoutButtonListener = new P_ScoutButtonListener();
      getScoutObject().addButtonListener(m_scoutButtonListener);
    }

  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (m_scoutButtonListener != null) {
      getScoutObject().removeButtonListener(m_scoutButtonListener);
      m_scoutButtonListener = null;
    }
  }

  @Override
  public Control getSwtField() {
    return super.getSwtField();
  }

  @Override
  protected void applyScoutProperties() {
    super.applyScoutProperties();
    IButton b = getScoutObject();
    if (b.hasMenus()) {
      // XXX button menus
    }
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
    if (m_contextMenu != null) {
      m_contextMenu.setVisible(true);
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
      switch (event.type) {
        case ButtonEx.SELECTION_ACTION: {
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
          IMenu[] scoutMenus = getScoutObject().getUIFacade().fireButtonPopupFromUI();
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
