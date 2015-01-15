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
package org.eclipse.scout.rt.ui.swing.form.fields.textfield;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.services.common.spellchecker.ISpellingMonitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.menu.SwingScoutContextMenu;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextFieldEx;
import org.eclipse.scout.rt.ui.swing.ext.decoration.ContextMenuDecorationItem;
import org.eclipse.scout.rt.ui.swing.ext.decoration.JTextFieldWithDecorationIcons;
import org.eclipse.scout.rt.ui.swing.spellchecker.ISwingSpellCheckerService;
import org.eclipse.scout.rt.ui.swing.spellchecker.SwingFieldHolder;
import org.eclipse.scout.service.SERVICES;

public class SwingScoutTextField extends SwingScoutTextFieldComposite<IStringField> implements ISwingScoutTextField {

  private ISpellingMonitor m_spellingMonitor;

  private ContextMenuDecorationItem m_contextMenuMarker;
  private SwingScoutContextMenu m_contextMenu;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    JTextFieldWithDecorationIcons textField = new JTextFieldWithDecorationIcons();
    m_contextMenuMarker = new ContextMenuDecorationItem(getScoutObject().getContextMenu(), textField, getSwingEnvironment());
    m_contextMenuMarker.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        m_contextMenu.showSwingPopup(e.getX(), e.getY(), false);
      }
    });
    textField.setDecorationIcon(m_contextMenuMarker);
    container.add(textField);
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(textField);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  protected void installContextMenu() {
    m_contextMenuVisibilityListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
          m_contextMenuMarker.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
        }
      }
    };
    getScoutObject().getContextMenu().addPropertyChangeListener(m_contextMenuVisibilityListener);
    m_contextMenuMarker.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    m_contextMenu = SwingScoutContextMenu.installContextMenuWithSystemMenus(getSwingTextField(), getScoutObject().getContextMenu(), getSwingEnvironment());
  }

  protected void uninstallContextMenu() {
    if (m_contextMenuVisibilityListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(m_contextMenuVisibilityListener);
      m_contextMenuVisibilityListener = null;
    }
  }

  @Override
  public JTextFieldEx getSwingTextField() {
    return (JTextFieldEx) getSwingTextComponent();
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    int swingAlign = SwingUtility.createHorizontalAlignment(scoutAlign);
    getSwingTextField().setHorizontalAlignment(swingAlign);
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    // spell checking
    ISwingSpellCheckerService spellCheckerService = SERVICES.getService(ISwingSpellCheckerService.class);
    if (spellCheckerService != null && spellCheckerService.isInstalled()) {
      m_spellingMonitor = spellCheckerService.createSpellingMonitor(new SwingFieldHolder(this, this.getSwingTextField()));
    }
    installContextMenu();
    updateContextMenuFromScout();
  }

  @Override
  protected void detachScout() {
    // spell checking
    if (m_spellingMonitor != null) {
      m_spellingMonitor.dispose();
      m_spellingMonitor = null;
    }
    if (m_contextMenuMarker != null) {
      m_contextMenuMarker.destroy();
    }
    uninstallContextMenu();
    super.detachScout();
  }

}
