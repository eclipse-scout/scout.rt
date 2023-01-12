/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

/**
 * @since 3.9.0
 */
@Order(5300)
public class TabletDeviceTransformer extends AbstractDeviceTransformer {
  protected boolean m_navigationWasVisible = false;
  protected boolean m_navigationVisibleChanging = false;

  @Override
  protected void initTransformationConfig() {
    enableTransformation(TabletDeviceTransformation.AUTO_HIDE_NAVIGATION);
    enableTransformation(TabletDeviceTransformation.USE_BREAD_CRUMB_NAVIGATION);
  }

  @Override
  public boolean isActive() {
    return UserAgentUtility.isTabletDevice();
  }

  @Override
  public void transformDesktop() {
    getDesktop().setCacheSplitterPosition(false);
  }

  @Override
  public void transformOutline(IOutline outline) {
    if (!isTransformationEnabled(TabletDeviceTransformation.USE_BREAD_CRUMB_NAVIGATION)) {
      return;
    }
    outline.setDisplayStyle(ITree.DISPLAY_STYLE_BREADCRUMB);
  }

  @Override
  public void notifyFormAboutToShow(IForm form) {
    super.notifyFormAboutToShow(form);
    hideNavigationIfNecessary(form);
  }

  @Override
  public void notifyFormDisposed(IForm form) {
    super.notifyFormDisposed(form);
    showNavigationIfNecessary(form);
  }

  protected void hideNavigationIfNecessary(IForm form) {
    if (!isTransformationEnabled(TabletDeviceTransformation.AUTO_HIDE_NAVIGATION)) {
      return;
    }
    // When the first view opens, close the navigation
    if (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW && getDesktop().getViews().size() == 0) {
      boolean navigationVisible = getDesktop().isNavigationVisible();
      m_navigationWasVisible = navigationVisible;
      m_navigationVisibleChanging = true;
      try {
        getDesktop().setNavigationVisible(false);
      }
      finally {
        m_navigationVisibleChanging = false;
      }
    }
  }

  protected void showNavigationIfNecessary(IForm form) {
    if (!isTransformationEnabled(TabletDeviceTransformation.AUTO_HIDE_NAVIGATION)) {
      return;
    }
    // When last view closes make the navigation visible again (if it was visible when the first view was opened)
    if (m_navigationWasVisible && getDesktop().getViews().size() == 1 && getDesktop().getViews().get(0) == form) {
      m_navigationVisibleChanging = true;
      try {
        getDesktop().setNavigationVisible(true);
      }
      finally {
        m_navigationVisibleChanging = false;
      }
    }
  }
}
