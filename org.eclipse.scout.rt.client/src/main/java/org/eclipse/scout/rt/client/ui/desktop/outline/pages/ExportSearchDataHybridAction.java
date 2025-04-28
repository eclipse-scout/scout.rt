/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.ui.desktop.hybrid.AbstractHybridAction;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionType;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HybridActionType(ExportSearchDataHybridAction.TYPE)
public class ExportSearchDataHybridAction extends AbstractHybridAction<IDoEntity> {
  private static final Logger LOG = LoggerFactory.getLogger(ExportSearchDataHybridAction.class);

  protected static final String TYPE = "scout.ExportSearchData";

  @Override
  public void execute(IDoEntity data) {
    LOG.warn("Not implemented yet");
    fireHybridActionEndEvent();
  }
}
