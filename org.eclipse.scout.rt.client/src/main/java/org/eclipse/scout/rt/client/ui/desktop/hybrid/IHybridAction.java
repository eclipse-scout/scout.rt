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

import java.util.Map;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.Bean;

/**
 * An action that can be called remotely by JavaScript code based on the configured {@link HybridActionType}.
 *
 * @see HybridManager
 */
@Bean
public interface IHybridAction<DO_ENTITY extends IDoEntity> {

  String DELIMITER = ":";

  Class<DO_ENTITY> getDoEntityClass();

  void init(String id, Map<String, HybridActionContextElement> contextElements);

  void execute(DO_ENTITY data);

  default void execute(String id, IDoEntity data, Map<String, HybridActionContextElement> contextElements) {
    if (data != null && !getDoEntityClass().isInstance(data)) {
      throw new IllegalArgumentException("data must be of type '" + getDoEntityClass() + "' but is '" + data.getClass() + "'.");
    }
    init(id, contextElements);
    execute(getDoEntityClass().cast(data));
  }
}
