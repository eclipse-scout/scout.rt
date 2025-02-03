/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.mapping;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Extension for {@link AbstractDoEntityMapper} to add additional mappings, e.g. provided by a form extension.
 */
@ApplicationScoped
public interface IDoEntityMapperExtension<DO_ENTITY extends IDoEntity, PEER> {

  /**
   * @return The mapper that is used to identify the mapper this extension is applied to.
   */
  Class<? extends AbstractDoEntityMapper<DO_ENTITY, PEER>> getMapperClass();

  /**
   * @param mappings
   *     Mappings to extend.
   */
  void extendMappings(DoEntityMappings<DO_ENTITY, PEER> mappings);
}
