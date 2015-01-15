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
package org.eclipse.scout.rt.ui.swt.form.fields.stringfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swt.action.menu.MenuPositionCorrectionListener;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtScoutContextMenu;
import org.eclipse.scout.rt.ui.swt.action.menu.text.StyledTextAccess;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * Typical string field
 */
public class SwtScoutStringField extends SwtScoutStringFieldComposite implements ISwtScoutStringField {
  private MouseListener m_linkTrigger;

  private boolean m_linkDecoration;

  private SwtContextMenuMarkerComposite m_menuMarkerComposite;

  private SwtScoutContextMenu m_contextMenu;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = createContainer(parent);
    StatusLabelEx label = createLabel(container);

    m_menuMarkerComposite = new SwtContextMenuMarkerComposite(container, getEnvironment());
    getEnvironment().getFormToolkit().adapt(m_menuMarkerComposite);
    m_menuMarkerComposite.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        getSwtField().setFocus();
        m_contextMenu.getSwtMenu().setVisible(true);
      }
    });

    int style = getSwtStyle(getScoutObject());
    StyledText textField = getEnvironment().getFormToolkit().createStyledText(m_menuMarkerComposite, style);
    textField.setAlignment(SwtUtility.getHorizontalAlignment(getScoutObject().getGridData().horizontalAlignment));
    textField.setMargins(2, 2, 2, 2);
    textField.setWrapIndent(textField.getIndent());

    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(textField);

    addDefaultUiListeners(textField);

    // layout
    getSwtContainer().setLayout(getContainerLayout());
    m_menuMarkerComposite.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
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

    SwtScoutContextMenu fieldMenu = new SwtScoutContextMenu(getSwtField().getShell(), getScoutObject().getContextMenu(), getEnvironment(),
        getScoutObject().isAutoAddDefaultMenus() ? new StyledTextAccess(getSwtField()) : null, getScoutObject().isAutoAddDefaultMenus() ? getSwtField() : null);
    getSwtField().setMenu(fieldMenu.getSwtMenu());

    // correction of menu position
    getSwtField().addListener(SWT.MenuDetect, new MenuPositionCorrectionListener(getSwtField()));
  }

  protected void uninstallContextMenu() {
    if (m_contextMenuVisibilityListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(m_contextMenuVisibilityListener);
      m_contextMenuVisibilityListener = null;
    }
  }

  protected void addDefaultUiListeners(StyledText textField) {
    addModifyListenerForBasicField(textField);
    textField.addSelectionListener(new P_SwtTextSelectionListener());
    textField.addVerifyListener(new P_TextVerifyListener());
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IStringField f = getScoutObject();
    setFormatFromScout(f.getFormat());
    setMaxLengthFromScout(f.getMaxLength());
    setSelectionFromScout(f.getSelectionStart(), f.getSelectionEnd());

    setDecorationLinkFromScout(f.isDecorationLink());
    setTextWrapFromScout(f.isWrapText());

    // dnd support
    new P_DndSupport(getScoutObject(), getScoutObject(), getSwtField(), getEnvironment());

    installContextMenu();
  }

  @Override
  protected void detachScout() {
    uninstallContextMenu();
    super.detachScout();
  }

  @Override
  public StyledTextEx getSwtField() {
    return (StyledTextEx) super.getSwtField();
  }

  protected void setDecorationLinkFromScout(boolean b) {
    if (m_linkDecoration != b) {
      m_linkDecoration = b;
      if (m_linkDecoration) {
        m_linkTrigger = new P_SwtLinkTrigger();
        getSwtField().addMouseListener(m_linkTrigger);
        getSwtField().setCursor(getSwtField().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        setForegroundFromScout(getScoutObject().getForegroundColor());
      }
      else {
        getSwtField().removeMouseListener(m_linkTrigger);
        m_linkTrigger = null;
        getSwtField().setCursor(null);
        setForegroundFromScout(getScoutObject().getForegroundColor());
      }
    }
  }

  @Override
  protected void setForegroundFromScout(String scoutColor) {
    if (scoutColor == null && m_linkDecoration) {
      scoutColor = "0000FF";
    }
    super.setForegroundFromScout(scoutColor);
  }

  @Override
  protected void setMaxLengthFromScout(int n) {
    getSwtField().setTextLimit(n);
  }

  @Override
  protected void setText(String text) {
    getSwtField().setText(text);
  }

  @Override
  protected int getCaretOffset() {
    return getSwtField().getCaretOffset();
  }

  @Override
  protected void setCaretOffset(int caretPosition) {
    getSwtField().setCaretOffset(caretPosition);
  }

  /**
   * select the swt field, if it has the focus
   */
  @Override
  protected void setSelection(int startIndex, int endIndex) {
    getSwtField().setSelection(startIndex, endIndex);
  }

  protected void setTextWrapFromScout(boolean booleanValue) {
    if (getScoutObject().isMultilineText()) {
      getSwtField().setWordWrap(booleanValue);
    }
  }

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IStringField.PROP_DECORATION_LINK)) {
      setDecorationLinkFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IStringField.PROP_WRAP_TEXT)) {
      setTextWrapFromScout(((Boolean) newValue).booleanValue());
    }
  }

  protected void handleSwtLinkTrigger() {
    final String text = getSwtField().getText();
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireLinkActionFromUI(text);
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  @Override
  protected String getText() {
    return getSwtField().getText();
  }

  private class P_SwtLinkTrigger extends MouseAdapter {
    @Override
    public void mouseDoubleClick(MouseEvent e) {
      handleSwtLinkTrigger();
    }
  } // end class P_SwtLinkTrigger

  @Override
  protected TextFieldEditableSupport createEditableSupport() {
    return new TextFieldEditableSupport(getSwtField());
  }

  @Override
  protected Point getSelection() {
    return getSwtField().getSelection();
  }

}
