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

package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Provider for {@link ITableTileGridMediator}.
 *
 * @since 10.0
 */
@FunctionalInterface
@ApplicationScoped
public interface ITableTileGridMediatorProvider {

  ITableTileGridMediator createTableTileGridMediator(ITable table);

}
