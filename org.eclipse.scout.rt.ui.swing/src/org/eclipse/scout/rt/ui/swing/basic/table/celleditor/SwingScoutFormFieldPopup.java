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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
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
import javax.swing.SwingUtilities;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.focus.SwingScoutFocusTraversalPolicy;
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
  private FocusTraversalPolicy m_focusTraversalPolicy;

  private List<IFormFieldPopupEventListener> m_eventListeners = new ArrayList<IFormFieldPopupEventListener>();
  private Object m_eventListenerLock = new Object();

  public SwingScoutFormFieldPopup(JComponent owner) {
    m_owner = owner;
    m_popupEventListener = new P_PopupEventListener();
    m_focusTraversalPolicy = new SwingScoutFocusTraversalPolicy();
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

        JScrollPane scrollPane = new JScrollPaneEx(getInnerSwingField());
        scrollPane.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createField(getSwingEnvironment(), ((IFormField) getScoutObject()).getGridData()));
        scrollPane.setBorder(null);
        rootPanel.add(scrollPane);

        // install popup listener
        m_swingScoutPopup.addSwingScoutViewListener(m_popupEventListener);

        // open popup
        m_swingScoutPopup.openView();

        // install keystrokes
        installTraverseKeyStrokes(scrollPane);
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
    Component firstComponent = m_focusTraversalPolicy.getFirstComponent(component);
    Component lastComponent = m_focusTraversalPolicy.getFirstComponent(component);
    // escape
    component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("pressed ESCAPE"), "escape");
    component.getActionMap().put("escape", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        closePopup(FormFieldPopupEvent.TYPE_CANCEL);
      }
    });

    // shift-tab (backward-focus-traversal)
    if (firstComponent instanceof JComponent) {
      JComponent c = (JComponent) firstComponent;
      c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, new HashSet<AWTKeyStroke>());
      c.getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("shift TAB"), "reverse-tab");
      c.getActionMap().put("reverse-tab", new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          closePopup(FormFieldPopupEvent.TYPE_OK | FormFieldPopupEvent.TYPE_FOCUS_BACK);
        }
      });
    }

    // tab (forward-focus-traversal) and ctrl-tab (tab within field)
    // Forward-focus-traversal key (TAB) cannot be installed by the component's action map because
    // tab / ctrl-tab cannot be intercepted separately that way. This is required to support tabs within the field.
    // Also, if using KeyListener to intercept tab events, there is a slightly hack necessary, because the ctrl
    // modifier is lost.
    if (lastComponent instanceof JComponent) {
      JComponent c = (JComponent) lastComponent;
      c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, new HashSet<AWTKeyStroke>());
      c.addKeyListener(new KeyAdapter() {

        private boolean m_controlDown;

        @Override
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            m_controlDown = true;
          }
          if (e.getKeyCode() == KeyEvent.VK_TAB && !m_controlDown && !e.isShiftDown()) {
            // forward-focus-traversal
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
    if (isClosed()) {
      return;
    }
    // force field to verify its input to be written back to model
    if (getInnerSwingField() != null && getInnerSwingField().getInputVerifier() != null) {
      getInnerSwingField().getInputVerifier().verify(getInnerSwingField());
    }

    // close popup
    try {
      m_swingScoutPopup.removeSwingScoutViewListener(m_popupEventListener);
      m_swingScoutPopup.closeView();
      m_swingScoutPopup = null;
    }
    finally {
      // notify listeners
      notifyEventListeners(new FormFieldPopupEvent(getScoutObject(), type));
    }
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

  public boolean isClosed() {
    return m_swingScoutPopup == null;
  }

  public JComponent getInnerSwingField() {
    // prefer field over container to omit border panel
    JComponent component = m_innerSwingScoutFormField.getSwingField();
    if (component == null) {
      component = m_innerSwingScoutFormField.getSwingContainer();
    }
    return component;
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
      if (e.getType() == SwingScoutViewEvent.TYPE_OPENED) {
        // request focus
        if (m_swingScoutPopup == null) {
          return;
        }
        JComponent contentPane = m_swingScoutPopup.getSwingContentPane();
        if (contentPane == null) {
          return;
        }
        final Component firstComponent = m_focusTraversalPolicy.getFirstComponent(contentPane);
        if (firstComponent == null) {
          return;
        }
        // must be called after all pending AWT events have been processed to ensure the focus to be kept
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            firstComponent.requestFocus();
          }
        });
      }
      else if (e.getType() == SwingScoutViewEvent.TYPE_CLOSED) {
        closePopup(FormFieldPopupEvent.TYPE_OK);
      }
    }
  }
}
