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
package org.eclipse.scout.rt.client.extension.ui.tile;

import org.eclipse.scout.rt.client.extension.ui.tile.BeanTileChains.BeanTileAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.tile.AbstractBeanTile;

public interface IBeanTileExtension<BEAN, OWNER extends AbstractBeanTile<BEAN>> extends ITileExtension<OWNER> {

  void execAppLinkAction(BeanTileAppLinkActionChain<BEAN> chain, String ref);

}
