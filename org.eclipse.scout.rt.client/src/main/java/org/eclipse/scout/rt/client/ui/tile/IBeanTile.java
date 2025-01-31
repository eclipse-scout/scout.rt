/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("c0a7d785-3e06-4c08-8203-1a12f35528ed")
public interface IBeanTile<BEAN> extends ITile, IAppLinkCapable {

  String PROP_BEAN = "bean";

  IBeanTileUIFacade getUIFacade();

  BEAN getBean();

  void setBean(BEAN bean);
}
