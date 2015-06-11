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

import javax.swing.JScrollPane;

import org.eclipse.scout.rt.client.services.common.spellchecker.ISpellingMonitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.menu.SwingScoutContextMenu;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextAreaEx;
import org.eclipse.scout.rt.ui.swing.ext.decoration.ContextMenuDecorationItem;
import org.eclipse.scout.rt.ui.swing.ext.decoration.JTextAreaWithDecorationIcons;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.spellchecker.ISwingSpellCheckerService;
import org.eclipse.scout.rt.ui.swing.spellchecker.SwingFieldHolder;
import org.eclipse.scout.service.SERVICES;

public class SwingScoutTextArea extends SwingScoutTextFieldComposite<IStringField> implements ISwingScoutTextArea {

  private JScrollPane m_swingScrollPane;
  private ISpellingMonitor m_spellingMonitor;

  private ContextMenuDecorationItem m_contextMenuMarker;
  private SwingScoutContextMenu m_contextMenu;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    JPanelEx container = new JPanelEx();
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    JTextAreaWithDecorationIcons textField = new JTextAreaWithDecorationIcons();
    textField.setOpaque(true);
    m_contextMenuMarker = new ContextMenuDecorationItem(getScoutObject().getContextMenu(), textField, getSwingEnvironment());
    m_contextMenuMarker.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        m_contextMenu.showSwingPopup(e.getX(), e.getY(), false);
      }
    });
    textField.setDecorationIcon(m_contextMenuMarker);
    m_swingScrollPane = new JScrollPaneEx(textField);
    m_swingScrollPane.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData()));
    container.add(m_swingScrollPane);
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(textField);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  @Override
  public JScrollPane getSwingScrollPane() {
    return m_swingScrollPane;
  }

  @Override
  public JTextAreaEx getSwingTextArea() {
    return (JTextAreaEx) getSwingTextComponent();
  }

  /*
   * scout property handlers
   */
  @Override
  protected void attachScout() {
    super.attachScout();
    setTextWrapFromScout(getScoutObject().isWrapText());
    // spell checking
    ISwingSpellCheckerService spellCheckerService = SERVICES.getService(ISwingSpellCheckerService.class);
    if (spellCheckerService != null && spellCheckerService.isInstalled()) {
      m_spellingMonitor = spellCheckerService.createSpellingMonitor(new SwingFieldHolder(this, this.getSwingTextArea()));
    }
    installContextMenu();
  }

  @Override
  protected void detachScout() {
    // spell checking
    if (m_spellingMonitor != null) {
      m_spellingMonitor.dispose();
      m_spellingMonitor = null;
    }
    uninstallContextMenu();
    super.detachScout();
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
    m_contextMenu = SwingScoutContextMenu.installContextMenuWithSystemMenus(getSwingField(), getScoutObject().getContextMenu(), getSwingEnvironment());
  }

  protected void uninstallContextMenu() {
    if (m_contextMenuVisibilityListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(m_contextMenuVisibilityListener);
      m_contextMenuVisibilityListener = null;
    }
  }

  @Override
  protected void setKeyStrokesFromScout() {
    super.setKeyStrokesFromScout();
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    float swingAlignX = SwingUtility.createAlignmentX(scoutAlign);
    getSwingTextArea().setAlignmentX(swingAlignX);
  }

  @Override
  protected void setVerticalAlignmentFromScout(int scoutAlign) {
    float swingAlignY = SwingUtility.createAlignmentY(scoutAlign);
    getSwingTextArea().setAlignmentY(swingAlignY);
  }

  protected void setTextWrapFromScout(boolean b) {
    getSwingTextArea().setLineWrap(b);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IStringField.PROP_WRAP_TEXT)) {
      setTextWrapFromScout(((Boolean) newValue).booleanValue());
    }
  }
}
