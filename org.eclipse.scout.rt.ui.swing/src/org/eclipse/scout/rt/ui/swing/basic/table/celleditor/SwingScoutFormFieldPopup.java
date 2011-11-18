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
package org.eclipse.scout.rt.ui.swing.basic.table.celleditor;

import java.awt.AWTKeyStroke;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;
import org.eclipse.scout.rt.ui.swing.window.popup.SwingScoutDropDownPopup;

/**
 * Wraps a {@link IFormField} to be displayed as popup cell editor
 */
public class SwingScoutFormFieldPopup extends SwingScoutComposite<IFormField> {

  private SwingScoutDropDownPopup m_swingScoutPopup;
  private ISwingScoutFormField<IFormField> m_innerSwingScoutFormField;

  private JComponent m_owner;
  private int m_minWidth;
  private int m_prefWidth;
  private int m_minHeight;
  private int m_prefHeight;
  private SwingScoutViewListener m_popupEventListener;

  private List<IFormFieldPopupEventListener> m_eventListeners = new ArrayList<IFormFieldPopupEventListener>();
  private Object m_eventListenerLock = new Object();

  public SwingScoutFormFieldPopup(JComponent owner) {
    m_owner = owner;
    m_popupEventListener = new P_PopupEventListener();
  }

  @Override
  protected void initializeSwing() {
    super.initializeSwing();

    m_owner.addHierarchyListener(new HierarchyListener() {

      @SuppressWarnings("unchecked")
      @Override
      public void hierarchyChanged(HierarchyEvent e) {
        if (!e.getComponent().isShowing()) {
          return;
        }
        // remove listener to only be called once
        m_owner.removeHierarchyListener(this);

        // create popup in reference to cell editor (owner)
        m_swingScoutPopup = new SwingScoutDropDownPopup(getSwingEnvironment(), m_owner, null);
        m_swingScoutPopup.setResizable(true);
        m_swingScoutPopup.setPopupOnField(true);

        if (m_prefWidth > 0 || m_prefHeight > 0) {
          m_swingScoutPopup.getSwingWindow().setPreferredSize(toValidDimension(new Dimension(m_prefWidth, m_prefHeight)));
        }
        if (m_minWidth > 0 || m_minHeight > 0) {
          m_swingScoutPopup.getSwingWindow().setMinimumSize(toValidDimension(new Dimension(m_minWidth, m_minHeight)));
        }
        m_innerSwingScoutFormField = getSwingEnvironment().createFormField(m_swingScoutPopup.getSwingContentPane(), getScoutObject());

        // put field into scrollpane
        JPanelEx rootPanel = new JPanelEx(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
        rootPanel.setOpaque(false);
        m_swingScoutPopup.getSwingContentPane().add(rootPanel);

        JScrollPane scrollPane = new JScrollPaneEx(m_innerSwingScoutFormField.getSwingField());
        scrollPane.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createField(getSwingEnvironment(), ((IFormField) getScoutObject()).getGridData()));
        scrollPane.setBorder(null);
        rootPanel.add(scrollPane);

        // install popup listener
        m_swingScoutPopup.addSwingScoutViewListener(m_popupEventListener);
        // install keystrokes on inner form field
        installTraverseKeyStrokes(getInnerSwingField());

        // open popup
        m_swingScoutPopup.openView();

        // request focus
        getSwingEnvironment().invokeScoutLater(new Runnable() {

          @Override
          public void run() {
            getScoutObject().requestFocus();
          }
        }, 0);
      }
    });

    m_owner.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e) {
        if (m_swingScoutPopup != null) {
          m_swingScoutPopup.autoAdjustBounds();
        }
      }
    });

    setSwingField(m_owner);
  }

  private void installTraverseKeyStrokes(JComponent component) {
    component.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, new HashSet<AWTKeyStroke>());
    component.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, new HashSet<AWTKeyStroke>());

    // escape
    component.getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("pressed ESCAPE"), "escape");
    component.getActionMap().put("escape", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        closePopup(FormFieldPopupEvent.TYPE_CANCEL);
      }
    });

    // TAB back
    component.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, new HashSet<AWTKeyStroke>());
    component.getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("shift TAB"), "reverse-tab");
    component.getActionMap().put("reverse-tab", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        closePopup(FormFieldPopupEvent.TYPE_OK | FormFieldPopupEvent.TYPE_FOCUS_BACK);
      }
    });

    // TAB / CTRL-TAB
    // forward-focus-traversal key (TAB) cannot be installed by the component's action map because
    // TAB / CTRL-TAB cannot be intercepted separately that way. This is required to support tabs within the field.
    // Also, if using KeyListener to intercept TAB events, there is a slightly hack necessary, because the CTRL
    // modifier is lost.
    component.addKeyListener(new KeyAdapter() {

      private boolean m_controlDown;

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
          m_controlDown = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_TAB && !m_controlDown && !e.isShiftDown()) {
          closePopup(FormFieldPopupEvent.TYPE_OK | FormFieldPopupEvent.TYPE_FOCUS_NEXT);
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
          m_controlDown = false;
        }
      }
    });
  }

  public void addEventListener(IFormFieldPopupEventListener eventListener) {
    synchronized (m_eventListenerLock) {
      m_eventListeners.add(eventListener);
    }
  }

  public void removeEventListener(IFormFieldPopupEventListener eventListener) {
    synchronized (m_eventListenerLock) {
      m_eventListeners.remove(eventListener);
    }
  }

  protected void notifyEventListeners(FormFieldPopupEvent event) {
    IFormFieldPopupEventListener[] eventListeners;
    synchronized (m_eventListenerLock) {
      eventListeners = m_eventListeners.toArray(new IFormFieldPopupEventListener[m_eventListeners.size()]);
    }
    for (IFormFieldPopupEventListener eventListener : eventListeners) {
      eventListener.handleEvent(event);
    }
  }

  public void closePopup(int type) {
    // update model with changed value
    getInnerSwingField().getInputVerifier().verify(getInnerSwingField());

    // close popup
    m_swingScoutPopup.removeSwingScoutViewListener(m_popupEventListener);
    m_swingScoutPopup.closeView();

    // notify listeners
    notifyEventListeners(new FormFieldPopupEvent(getScoutObject(), type));
  }

  public int getMinWidth() {
    return m_minWidth;
  }

  public void setMinWidth(int minWidth) {
    m_minWidth = minWidth;
  }

  public int getPrefWidth() {
    return m_prefWidth;
  }

  public void setPrefWidth(int prefWidth) {
    m_prefWidth = prefWidth;
  }

  public int getMinHeight() {
    return m_minHeight;
  }

  public void setMinHeight(int minHeight) {
    m_minHeight = minHeight;
  }

  public int getPrefHeight() {
    return m_prefHeight;
  }

  public void setPrefHeight(int prefHeight) {
    m_prefHeight = prefHeight;
  }

  public SwingScoutDropDownPopup getPopup() {
    return m_swingScoutPopup;
  }

  public JComponent getInnerSwingField() {
    return m_innerSwingScoutFormField.getSwingField();
  }

  private Dimension toValidDimension(Dimension dimension) {
    if (dimension.width == 0) {
      dimension.width = getSwingEnvironment().getFormColumnWidth() / 2;
    }
    if (dimension.height == 0) {
      dimension.height = getSwingEnvironment().getFormColumnWidth() / 2;
    }
    return dimension;
  }

  private class P_PopupEventListener implements SwingScoutViewListener {

    @Override
    public void viewChanged(SwingScoutViewEvent e) {
      if (e.getType() == SwingScoutViewEvent.TYPE_CLOSED) {
        closePopup(FormFieldPopupEvent.TYPE_OK);
      }
    }
  }
}
