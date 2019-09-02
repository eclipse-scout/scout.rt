/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
