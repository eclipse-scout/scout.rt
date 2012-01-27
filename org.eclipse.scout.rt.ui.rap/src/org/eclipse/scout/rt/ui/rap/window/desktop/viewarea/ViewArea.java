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
package org.eclipse.scout.rt.ui.rap.window.desktop.viewarea;

import java.util.HashMap;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.extension.IUiDecoration;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutViewStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;

/**
 * <h3>ViewArea</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.7.0 June 2011
 */
public class ViewArea extends Composite {

  private static final long serialVersionUID = 1L;

  public enum SashKey {
    VERTICAL_LEFT,
    VERTICAL_RIGHT,
    HORIZONTAL_LEFT_TOP,
    HORIZONTAL_LEFT_BOTTOM,
    HORIZONTAL_CENTER_TOP,
    HORIZONTAL_CENTER_BOTTOM,
    HORIZONTAL_RIGHT_TOP,
    HORIZONTAL_RIGHT_BOTTOM
  }

  private P_SashSelectionListener m_sashListener;
  private HashMap<SashKey, Integer> m_sashPositions;

  RwtScoutViewStack[][] m_viewStacks;
  HashMap<SashKey, Sash> m_sashes;

  public ViewArea(Composite parent) {
    super(parent, SWT.NONE);
    m_sashes = new HashMap<ViewArea.SashKey, Sash>();
    m_sashPositions = new HashMap<ViewArea.SashKey, Integer>();
    m_sashListener = new P_SashSelectionListener();
    createContent(this);
    readPreferences();
    setLayout(new ViewAreaLayout());
  }

  private void readPreferences() {
    IUiDecoration deco = UiDecorationExtensionPoint.getLookAndFeel();
    m_sashPositions.put(SashKey.VERTICAL_RIGHT, deco.getLogicalGridLayoutDefaultColumnWidth());
  }

  protected void createContent(Composite parent) {
    m_viewStacks = new RwtScoutViewStack[3][3];
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        m_viewStacks[i][j] = new RwtScoutViewStack(parent, getUiEnvironment());
      }
    }
    // sashes
    for (SashKey k : SashKey.values()) {
      int style = SWT.HORIZONTAL;
      switch (k) {
        case VERTICAL_LEFT:
        case VERTICAL_RIGHT:
          style = SWT.VERTICAL;
          break;
      }
      Sash sash = new Sash(parent, style);
      sash.setData("SASH_KEY", k);
      sash.addListener(SWT.Selection, m_sashListener);
      m_sashes.put(k, sash);
    }
  }

  int getSashPosition(SashKey key) {
    Integer pos = m_sashPositions.get(key);
    if (pos != null && m_sashes.get(key).getVisible()) {
      return pos.intValue();
    }
    else {
      return -1;
    }
  }

  public Sash getSash(SashKey key) {
    return m_sashes.get(key);
  }

  private IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) getDisplay().getData(IRwtEnvironment.class.getName());
  }

  public RwtScoutViewStack getStackForForm(IForm form) {
    String scoutId = form.getDisplayViewId();
    if (scoutId == null) {
      return null;
    }
    // mapping
    if (IForm.VIEW_ID_OUTLINE_SELECTOR.equalsIgnoreCase(scoutId)) {
      scoutId = IForm.VIEW_ID_SW;
    }
    else if (IForm.VIEW_ID_OUTLINE.equalsIgnoreCase(scoutId)) {
      scoutId = IForm.VIEW_ID_NW;
    }
    else if (IForm.VIEW_ID_PAGE_DETAIL.equalsIgnoreCase(scoutId)) {
      scoutId = IForm.VIEW_ID_N;
    }
    else if (IForm.VIEW_ID_PAGE_TABLE.equalsIgnoreCase(scoutId)) {
      scoutId = IForm.VIEW_ID_CENTER;
    }
    //Editors are placed at center position
    //If multiple editors are defined the EDITOR_ID is used as prefix to distinguish them
    else if (scoutId.startsWith(IForm.EDITOR_ID) || scoutId.startsWith(IWizard.EDITOR_ID)) {
      scoutId = IForm.VIEW_ID_CENTER;
    }
    else if (IForm.VIEW_ID_PAGE_SEARCH.equalsIgnoreCase(scoutId)) {
      scoutId = IForm.VIEW_ID_S;
    }
    if (IForm.VIEW_ID_NW.equals(scoutId)) {
      return m_viewStacks[0][0];
    }
    if (IForm.VIEW_ID_N.equals(scoutId)) {
      return m_viewStacks[1][0];
    }
    if (IForm.VIEW_ID_NE.equals(scoutId)) {
      return m_viewStacks[2][0];
    }
    if (IForm.VIEW_ID_W.equals(scoutId)) {
      return m_viewStacks[0][1];
    }
    if (IForm.VIEW_ID_CENTER.equals(scoutId)) {
      return m_viewStacks[1][1];
    }
    if (IForm.VIEW_ID_E.equals(scoutId)) {
      return m_viewStacks[2][1];
    }
    if (IForm.VIEW_ID_SW.equals(scoutId)) {
      return m_viewStacks[0][2];
    }
    if (IForm.VIEW_ID_S.equals(scoutId)) {
      return m_viewStacks[1][2];
    }
    if (IForm.VIEW_ID_SE.equals(scoutId)) {
      return m_viewStacks[2][2];
    }
    else {
      return null;
    }
  }

  @Override
  public ViewAreaLayout getLayout() {
    return (ViewAreaLayout) super.getLayout();
  }

  private class P_SashSelectionListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      if (event.detail != SWT.DRAG) {
        int position = -1;
        Sash sash = (Sash) event.widget;
        Point size = sash.getParent().getSize();
        SashKey sashKey = (SashKey) sash.getData("SASH_KEY");
        switch (sashKey) {
          case VERTICAL_LEFT:
            position = event.x;
            break;
          case HORIZONTAL_LEFT_TOP:
          case HORIZONTAL_CENTER_TOP:
          case HORIZONTAL_RIGHT_TOP:
            position = event.y;
            break;
          case VERTICAL_RIGHT:
            position = size.x - event.x;
            break;
          case HORIZONTAL_LEFT_BOTTOM:
          case HORIZONTAL_CENTER_BOTTOM:
          case HORIZONTAL_RIGHT_BOTTOM:
            position = size.y - event.y;
            break;
        }
        if (position > 0) {
          m_sashPositions.put(sashKey, new Integer(position));
          layout();
        }
      }
    }
  }
}
