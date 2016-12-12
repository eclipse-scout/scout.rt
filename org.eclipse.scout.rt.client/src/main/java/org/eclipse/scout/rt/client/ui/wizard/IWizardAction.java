/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.wizard;

import java.security.Permission;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.shared.dimension.IEnabledDimension;
import org.eclipse.scout.rt.shared.dimension.IVisibleDimension;

/**
 * An interface for "wizard actions", i.e. buttons or menus that are used for wizard navigation like "next", "previous"
 * etc. To be able to use either buttons or menus, this neutral interface has to be used, because {@link IButton} and
 * {@link IMenu} don't share a common interface.
 * <p>
 * <h2>Implementing the interface</h2> The methods on the interface match the usual Scout conventions, therefore it can
 * be added to any {@link IButton} without any adjustments. When applied to an {@link IMenu}, the methods
 * {@link #setLabel(String)} and {@link #getLabel()} have to be implemented. The calls may just be delegated to
 * {@link IMenu#setText(String)} and {@link IMenu#getText()}, respectively.
 */
public interface IWizardAction extends IVisibleDimension, IEnabledDimension {

  void setView(boolean visible, boolean enabled);

  void setVisiblePermission(Permission permission);

  boolean isVisibleGranted();

  void setVisibleGranted(boolean visibleGranted);

  void setVisible(boolean visible);

  boolean isVisible();

  void setEnabledPermission(Permission permission);

  boolean isEnabledGranted();

  void setEnabledGranted(boolean enabledGranted);

  void setEnabled(boolean enabled);

  boolean isEnabled();

  void setLabel(String label);

  String getLabel();

  void setTooltipText(String tooltipText);

  String getTooltipText();
}
