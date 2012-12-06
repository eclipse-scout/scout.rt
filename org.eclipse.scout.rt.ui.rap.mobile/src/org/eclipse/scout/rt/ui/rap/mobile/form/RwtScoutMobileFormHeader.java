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
package org.eclipse.scout.rt.ui.rap.mobile.form;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.transformation.IDeviceTransformationService;
import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.rap.mobile.action.ActionButtonBar;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileFormHeader extends AbstractRwtScoutFormHeader {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutMobileFormHeader.class);

  private List<IMenu> m_actions;

  @Override
  protected void initializeUi(Composite parent) {
    setRightActionBarOrientation(ActionButtonBar.ORIENTATION_RIGHT_TO_LEFT);

    m_actions = fetchActions();

    super.initializeUi(parent);
  }

  @Override
  protected void collectMenusForLeftButtonBar(final List<IMenu> menuList) {
    List<IMenu> collectedMenus = new LinkedList<IMenu>(menuList);
    if (m_actions != null) {
      for (IMenu action : m_actions) {
        if (AbstractMobileAction.getHorizontalAlignment(action) < 0) {
          collectedMenus.add(action);
        }
      }
    }

    final List<IMenu> menuListToAdapt = new LinkedList<IMenu>(menuList);
    menuListToAdapt.addAll(collectedMenus);
    final BooleanHolder filled = new BooleanHolder(false);
    ClientSyncJob job = new ClientSyncJob("Adapting form header left menus", getUiEnvironment().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        IDeviceTransformationService service = SERVICES.getService(IDeviceTransformationService.class);
        if (service != null && service.getDeviceTransformer() != null) {
          service.getDeviceTransformer().adaptFormHeaderLeftActions(getScoutObject(), menuListToAdapt);
        }

        synchronized (RwtScoutMobileFormHeader.this) {
          if (!filled.getValue()) {
            menuList.addAll(menuListToAdapt);
            filled.setValue(true);
          }
        }
      }
    };
    job.schedule();
    try {
      job.join(5000);
    }
    catch (InterruptedException e) {
      LOG.warn("Failed to adapt form header left menus.", e);
    }

    synchronized (RwtScoutMobileFormHeader.this) {
      if (!filled.getValue()) {
        LOG.warn("Failed to adapt form header left menus, timeout reached.");
        menuList.addAll(collectedMenus);
        filled.setValue(true);
      }
    }

  }

  @Override
  protected void collectMenusForRightButtonBar(final List<IMenu> menuList) {
    List<IMenu> collectedMenus = new LinkedList<IMenu>(menuList);
    if (m_actions != null) {
      for (IMenu action : m_actions) {
        if (AbstractMobileAction.getHorizontalAlignment(action) > 0) {
          collectedMenus.add(action);
        }
      }

    }

    final List<IMenu> menuListToAdapt = new LinkedList<IMenu>(menuList);
    menuListToAdapt.addAll(collectedMenus);
    final BooleanHolder filled = new BooleanHolder(false);
    ClientSyncJob job = new ClientSyncJob("Adapting form header left menus", getUiEnvironment().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        IDeviceTransformationService service = SERVICES.getService(IDeviceTransformationService.class);
        if (service != null && service.getDeviceTransformer() != null) {
          service.getDeviceTransformer().adaptFormHeaderRightActions(getScoutObject(), menuListToAdapt);
        }

        synchronized (RwtScoutMobileFormHeader.this) {
          if (!filled.getValue()) {
            menuList.addAll(menuListToAdapt);
            filled.setValue(true);
          }
        }
      }
    };
    job.schedule();
    try {
      job.join(5000);
    }
    catch (InterruptedException e) {
      LOG.warn("Failed to adapt form header right menus.", e);
    }

    synchronized (RwtScoutMobileFormHeader.this) {
      if (!filled.getValue()) {
        LOG.warn("Failed to adapt form header right menus, timeout reached.");
        menuList.addAll(collectedMenus);
        filled.setValue(true);
      }
    }
  }
}
