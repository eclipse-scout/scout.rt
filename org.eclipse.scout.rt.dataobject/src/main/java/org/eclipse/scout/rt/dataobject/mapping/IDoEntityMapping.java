/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.mapping;

import org.eclipse.scout.rt.dataobject.IDoEntity;

/**
 * A DO entity mapping applies the values from a DO entity to the peer and vice versa.
 */
public interface IDoEntityMapping<DO_ENTITY extends IDoEntity, PEER> {

  /**
   * Applies the value from the source to the DO entity.
   * <p>
   * If the source has a way to signal the existence of a value, it's recommended that the value is only applied to the
   * DO entity if the value exists.
   */
  void toDo(PEER source, DO_ENTITY dataObject);

  /**
   * Applies the value from the DO entity to the target.
   * <p>
   * It's recommended that implementations that work on a per node base must only apply the node value to the target if
   * the node exists.
   */
  void fromDo(DO_ENTITY dataObject, PEER target);
}
