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

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.transformation.IDeviceTransformationService;
import org.eclipse.scout.rt.client.mobile.ui.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.rap.mobile.action.ActionButtonBar;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileFormHeader extends AbstractRwtScoutFormHeader {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutMobileFormHeader.class);

  private List<IMenu> m_leftBarActions;
  private List<IMenu> m_rightBarActions;

  @Override
  protected void initializeUi(Composite parent) {
    setRightActionBarOrientation(ActionButtonBar.ORIENTATION_RIGHT_TO_LEFT);

    if (getScoutObject().getRootGroupBox().getSystemProcessButtonCount() > 0) {
      m_leftBarActions = createLeftBarActions();
    }
    if (getScoutObject().getRootGroupBox().getSystemProcessButtonCount() > 0 || getScoutObject().getRootGroupBox().getCustomProcessButtonCount() > 0) {
      m_rightBarActions = createRightBarActions();
    }

    super.initializeUi(parent);
  }

  @Override
  protected void collectMenusForLeftButtonBar(final List<IMenu> menuList) {
    List<IMenu> collectedMenus = new LinkedList<IMenu>(menuList);
    if (m_leftBarActions != null && m_leftBarActions.size() > 0) {
      collectedMenus.addAll(m_leftBarActions);
    }

    final List<IMenu> menuListToAdapt = new LinkedList<IMenu>(menuList);
    menuListToAdapt.addAll(collectedMenus);
    final BooleanHolder filled = new BooleanHolder(false);
    ClientSyncJob job = new ClientSyncJob("Adapting form header left menus", getUiEnvironment().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        SERVICES.getService(IDeviceTransformationService.class).getDeviceTransformer().adaptFormHeaderLeftActions(getScoutObject(), menuListToAdapt);

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
    if (m_rightBarActions != null) {
      collectedMenus.addAll(m_rightBarActions);
    }

    final List<IMenu> menuListToAdapt = new LinkedList<IMenu>(menuList);
    menuListToAdapt.addAll(collectedMenus);
    final BooleanHolder filled = new BooleanHolder(false);
    ClientSyncJob job = new ClientSyncJob("Adapting form header left menus", getUiEnvironment().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        SERVICES.getService(IDeviceTransformationService.class).getDeviceTransformer().adaptFormHeaderRightActions(getScoutObject(), menuListToAdapt);

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

  protected List<IMenu> convertCustomProcessButtons() {
    IButton[] customProcessButtons = getScoutObject().getRootGroupBox().getCustomProcessButtons();
    if (customProcessButtons == null || customProcessButtons.length == 0) {
      return null;
    }

    return ActionButtonBarUtility.convertButtonsToActions(getScoutObject().getRootGroupBox().getCustomProcessButtons());
  }

  /**
   * If there are multiple buttons with a matching system types the order given in the list is used to sort the buttons.
   */
  protected List<IMenu> convertSystemProcessButtons(final List<Integer> relevantSystemTypes) {
    if (relevantSystemTypes == null || relevantSystemTypes.size() == 0) {
      return null;
    }

    IButton[] systemProcessButtons = getScoutObject().getRootGroupBox().getSystemProcessButtons();
    if (systemProcessButtons == null || systemProcessButtons.length == 0) {
      return null;
    }

    Comparator<IButton> comparator = new Comparator<IButton>() {
      @Override
      public int compare(IButton button1, IButton button2) {
        int index1 = relevantSystemTypes.indexOf(button1.getSystemType());
        int index2 = relevantSystemTypes.indexOf(button2.getSystemType());
        if (index1 >= index2) {
          return 1;
        }
        else {
          return -1;
        }
      }
    };

    Set<IButton> sortedButtons = new TreeSet<IButton>(comparator);
    sortedButtons.addAll(Arrays.asList(systemProcessButtons));
    List<IMenu> actions = new LinkedList<IMenu>();
    for (IButton button : sortedButtons) {
      if (relevantSystemTypes.contains(button.getSystemType())) {
        actions.add(ActionButtonBarUtility.convertButtonToAction(button));
      }
    }

    return actions;
  }

  protected List<IMenu> createLeftBarActions() {
    return convertSystemProcessButtons(getRelevantSystemTypesForLeftBar());
  }

  protected List<IMenu> createRightBarActions() {
    List<IMenu> actions = new LinkedList<IMenu>();

    List<IMenu> systemActions = convertSystemProcessButtons(getRelevantSystemTypesForRightBar());
    if (systemActions != null) {
      actions.addAll(systemActions);
    }

    List<IMenu> customActions = convertCustomProcessButtons();
    if (customActions != null) {
      actions.addAll(customActions);
    }

    return actions;
  }

  protected List<Integer> getRelevantSystemTypesForLeftBar() {
    List<Integer> systemTypesToConsider = new LinkedList<Integer>();
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_CANCEL);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_CLOSE);
    return systemTypesToConsider;
  }

  /**
   * Returns the system types which are relevant for the right button bar. The order of the list is taken into account
   * too.
   */
  protected List<Integer> getRelevantSystemTypesForRightBar() {
    List<Integer> systemTypesToConsider = new LinkedList<Integer>();
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_OK);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_SAVE);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_RESET);

    return systemTypesToConsider;
  }

}
