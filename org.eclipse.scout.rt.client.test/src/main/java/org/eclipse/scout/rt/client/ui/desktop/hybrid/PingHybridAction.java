/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import org.eclipse.scout.rt.dataobject.IDoEntity;

@HybridActionType(PingHybridAction.TYPE)
public class PingHybridAction extends AbstractHybridAction<IDoEntity> {

  protected static final String TYPE = "Ping";

  @Override
  public void execute(IDoEntity data) {
    fireHybridActionEndEvent();
  }
}
