/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.Bean;

@Bean
public interface IHybridAction<DO_ENTITY extends IDoEntity> {

  String DELIMITER = ":";

  Class<DO_ENTITY> getDoEntityClass();

  void init(String id);

  void execute(DO_ENTITY data);

  default void execute(String id, IDoEntity data) {
    init(id);
    if (data != null && !getDoEntityClass().isInstance(data)) {
      throw new IllegalArgumentException("data must be of type '" + getDoEntityClass() + "' but is '" + data.getClass() + "'.");
    }
    execute(getDoEntityClass().cast(data));
  }
}
