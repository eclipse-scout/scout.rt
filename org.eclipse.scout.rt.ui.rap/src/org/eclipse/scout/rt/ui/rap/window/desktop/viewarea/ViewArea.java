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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.extension.IUiDecoration;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutViewStack;
import org.eclipse.scout.rt.ui.rap.window.desktop.IViewArea;
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
public class ViewArea extends Composite implements IViewArea {

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
  private HashMap<String, Point> m_formPositions;

  RwtScoutViewStack[][] m_viewStacks;
  HashMap<SashKey, Sash> m_sashes;

  public ViewArea(Composite parent) {
    super(parent, SWT.NONE);
    m_sashes = new HashMap<ViewArea.SashKey, Sash>();
    m_sashPositions = new HashMap<ViewArea.SashKey, Integer>();
    if (isSashCreationEnabled()) {
      m_sashListener = new P_SashSelectionListener();
    }

    initFormPositions();
    createContent(this);
    readPreferences();
    setLayout(new ViewAreaLayout(getSashWidth()));
  }

  protected int getSashWidth() {
    return 3;
  }

  private void initFormPositions() {
    m_formPositions = new HashMap<String, Point>();
    m_formPositions.put(IForm.VIEW_ID_NW, new Point(0, 0));
    m_formPositions.put(IForm.VIEW_ID_N, new Point(1, 0));
    m_formPositions.put(IForm.VIEW_ID_NE, new Point(2, 0));
    m_formPositions.put(IForm.VIEW_ID_W, new Point(0, 1));
    m_formPositions.put(IForm.VIEW_ID_CENTER, new Point(1, 1));
    m_formPositions.put(IForm.VIEW_ID_E, new Point(2, 1));
    m_formPositions.put(IForm.VIEW_ID_SW, new Point(0, 2));
    m_formPositions.put(IForm.VIEW_ID_S, new Point(1, 2));
    m_formPositions.put(IForm.VIEW_ID_SE, new Point(2, 2));
  }

  private void readPreferences() {
    IUiDecoration deco = UiDecorationExtensionPoint.getLookAndFeel();
    m_sashPositions.put(SashKey.VERTICAL_RIGHT, deco.getLogicalGridLayoutDefaultColumnWidth());
  }

  private String getViewIdForCoord(int x, int y) {
    for (String viewId : m_formPositions.keySet()) {
      Point point = m_formPositions.get(viewId);
      if (point != null && point.x == x && point.y == y) {
        return viewId;
      }
    }

    return null;
  }

  protected void createContent(Composite parent) {
    m_viewStacks = new RwtScoutViewStack[3][3];
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        if (acceptViewId(getViewIdForCoord(x, y))) {
          m_viewStacks[x][y] = createRwtScoutViewStack(parent);
        }
        else {
          m_viewStacks[x][y] = null;
        }
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
      Sash sash = null;
      if (isSashCreationEnabled()) {
        sash = createSash(parent, style);
        if (sash != null) {
          sash.setData("SASH_KEY", k);
          sash.addListener(SWT.Selection, m_sashListener);
        }
      }
      m_sashes.put(k, sash);
    }
  }

  protected Sash createSash(Composite parent, int style) {
    return new Sash(parent, style);
  }

  /**
   * Controls whether a {@link RwtScoutViewStack} should be created for the given viewId.
   * <p>
   * The default accepts every view id. May be overridden to reduce the amount of created view stacks.
   */
  protected boolean acceptViewId(String viewId) {
    return true;
  }

  /**
   * Controls whether shashes should be created at all. May be overridden.
   * <p>
   * Default is true.
   */
  protected boolean isSashCreationEnabled() {
    return true;
  }

  protected RwtScoutViewStack createRwtScoutViewStack(Composite parent) {
    return new RwtScoutViewStack(parent, getUiEnvironment());
  }

  int getSashPosition(SashKey key) {
    Integer pos = m_sashPositions.get(key);
    if (pos != null) {
      return pos.intValue();
    }
    else {
      return -1;
    }
  }

  /**
   * Sets the sash position according to {@link RwtScoutViewStack#getHeightHint()} resp.
   * {@link RwtScoutViewStack#getWidthHint()} of the given {@link RwtScoutViewStack}.
   * <p>
   * For the vertical sashes the maximum width hint of the view stacks in the column containing the given view stack is
   * used.<br/>
   * For the horizontal sashes the height hint of the given view stack is used.<br/>
   * The view stacks in the center are not considered because they always get the remaining space.
   * <p>
   * The sash positions can be retrieved by the use of {@link #getSashPosition(SashKey)}.
   */
  @Override
  public void updateSashPositionForViewStack(IRwtScoutViewStack viewStack) {
    updateVerticalSashPositionForViewStack(viewStack, SashKey.VERTICAL_LEFT, 0);
    updateVerticalSashPositionForViewStack(viewStack, SashKey.VERTICAL_RIGHT, 2);

    List<RwtScoutViewStack> horizontalViewStacks = getHorizontalViewStacks(0);
    if (horizontalViewStacks.get(0) == viewStack) {
      updateSashPositionWithViewStackHeight(viewStack, SashKey.HORIZONTAL_LEFT_TOP);
    }
    else if (horizontalViewStacks.get(1) == viewStack) {
      updateSashPositionWithViewStackHeight(viewStack, SashKey.HORIZONTAL_CENTER_TOP);
    }
    else if (horizontalViewStacks.get(2) == viewStack) {
      updateSashPositionWithViewStackHeight(viewStack, SashKey.HORIZONTAL_RIGHT_TOP);
    }

    horizontalViewStacks = getHorizontalViewStacks(2);
    if (horizontalViewStacks.get(0) == viewStack) {
      updateSashPositionWithViewStackHeight(viewStack, SashKey.HORIZONTAL_LEFT_BOTTOM);
    }
    else if (horizontalViewStacks.get(1) == viewStack) {
      updateSashPositionWithViewStackHeight(viewStack, SashKey.HORIZONTAL_CENTER_BOTTOM);
    }
    else if (horizontalViewStacks.get(2) == viewStack) {
      updateSashPositionWithViewStackHeight(viewStack, SashKey.HORIZONTAL_RIGHT_BOTTOM);
    }

  }

  private void updateVerticalSashPositionForViewStack(IRwtScoutViewStack viewStack, SashKey sashKey, int col) {
    List<RwtScoutViewStack> verticalViewStacks = getVerticalViewStacks(col);
    if (!verticalViewStacks.contains(viewStack)) {
      return;
    }

    int maxWidth = -1;
    for (RwtScoutViewStack verticalViewStack : verticalViewStacks) {
      if (verticalViewStack != null && verticalViewStack.getWidthHint() > maxWidth) {
        maxWidth = viewStack.getWidthHint();
      }
    }

    if (maxWidth < 0) {
      return;
    }

    m_sashPositions.put(sashKey, maxWidth);
  }

  private void updateSashPositionWithViewStackHeight(IRwtScoutViewStack viewStack, SashKey sashKey) {
    int heightHint = viewStack.getHeightHint();
    if (heightHint < 0) {
      return;
    }

    m_sashPositions.put(sashKey, heightHint);
  }

  private List<RwtScoutViewStack> getVerticalViewStacks(int col) {
    List<RwtScoutViewStack> viewStacks = new LinkedList<RwtScoutViewStack>();
    for (int j = 0; j < 3; j++) {
      viewStacks.add(m_viewStacks[col][j]);
    }

    return viewStacks;
  }

  private List<RwtScoutViewStack> getHorizontalViewStacks(int row) {
    List<RwtScoutViewStack> viewStacks = new LinkedList<RwtScoutViewStack>();
    for (int i = 0; i < 3; i++) {
      viewStacks.add(m_viewStacks[i][row]);
    }

    return viewStacks;
  }

  /**
   * @return the Sash for the given key or null if {@link #isSashCreationEnabled()} returns false.
   */
  public Sash getSash(SashKey key) {
    return m_sashes.get(key);
  }

  protected IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) getDisplay().getData(IRwtEnvironment.class.getName());
  }

  @Override
  public RwtScoutViewStack getStackForForm(IForm form) {
    return getStackForForm(form.getDisplayViewId());
  }

  public RwtScoutViewStack getStackForForm(String scoutId) {
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
    Point coord = m_formPositions.get(scoutId);
    if (coord != null) {
      return m_viewStacks[coord.x][coord.y];
    }

    return null;
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
