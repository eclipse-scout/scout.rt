/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.datachange;

import java.util.EventListener;

/**
 * An event listener to be used with the {@link IDataChangeManager}.
 *
 * @since 8.0
 */
@FunctionalInterface
public interface IDataChangeListener extends EventListener {

  void dataChanged(DataChangeEvent event);
}
