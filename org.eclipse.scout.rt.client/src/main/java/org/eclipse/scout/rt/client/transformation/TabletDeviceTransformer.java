/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.transformation;

import java.beans.PropertyChangeEvent;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

/**
 * @since 3.9.0
 */
@Order(5300)
public class TabletDeviceTransformer extends AbstractDeviceTransformer {
  private boolean m_navigationHiddenByUser = false;
  private boolean m_navigationVisibleChanging = false;

  @Override
  protected void initTransformationConfig() {
    List<IDeviceTransformation> transformations = new LinkedList<>();

    transformations.add(TabletDeviceTransformation.AUTO_HIDE_NAVIGATION);
    transformations.add(TabletDeviceTransformation.USE_BREAD_CRUMB_NAVIGATION);

    for (IDeviceTransformation transformation : transformations) {
      getDeviceTransformationConfig().enableTransformation(transformation);
    }
  }

  @Override
  public boolean isActive() {
    return UserAgentUtility.isTabletDevice();
  }

  @Override
  public void transformDesktop() {
    getDesktop().setCacheSplitterPosition(false);
    if (getDeviceTransformationConfig().isTransformationEnabled(TabletDeviceTransformation.AUTO_HIDE_NAVIGATION)) {
      getDesktop().addPropertyChangeListener(IDesktop.PROP_NAVIGATION_VISIBLE, this::handleNavigationVisibleChange);
    }
  }

  protected void handleNavigationVisibleChange(PropertyChangeEvent event) {
    if (m_navigationVisibleChanging) {
      return;
    }
    m_navigationHiddenByUser = !getDesktop().isNavigationVisible();
  }

  @Override
  public void transformOutline(IOutline outline) {
    if (!getDeviceTransformationConfig().isTransformationEnabled(TabletDeviceTransformation.USE_BREAD_CRUMB_NAVIGATION)) {
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
    shoNavigationIfNecessary(form);
  }

  protected void hideNavigationIfNecessary(IForm form) {
    if (!getDeviceTransformationConfig().isTransformationEnabled(TabletDeviceTransformation.AUTO_HIDE_NAVIGATION)) {
      return;
    }
    if (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW) {
      m_navigationVisibleChanging = true;
      getDesktop().setNavigationVisible(false);
      m_navigationVisibleChanging = false;
    }
  }

  protected void shoNavigationIfNecessary(IForm form) {
    if (!getDeviceTransformationConfig().isTransformationEnabled(TabletDeviceTransformation.AUTO_HIDE_NAVIGATION)) {
      return;
    }
    if (!m_navigationHiddenByUser && getDesktop().getViews().size() == 1 && getDesktop().getViews().get(0) == form) {
      m_navigationVisibleChanging = true;
      getDesktop().setNavigationVisible(true);
      m_navigationVisibleChanging = false;
    }
  }

}
