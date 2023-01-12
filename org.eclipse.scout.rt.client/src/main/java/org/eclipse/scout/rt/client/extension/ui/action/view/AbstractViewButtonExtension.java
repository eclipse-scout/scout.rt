/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.action.view;

import org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension;
import org.eclipse.scout.rt.client.ui.action.view.AbstractViewButton;

public abstract class AbstractViewButtonExtension<OWNER extends AbstractViewButton> extends AbstractActionExtension<OWNER> implements IViewButtonExtension<OWNER> {

  public AbstractViewButtonExtension(OWNER owner) {
    super(owner);
  }
}
