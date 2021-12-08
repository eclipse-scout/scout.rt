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
   *          Mappings to extend.
   */
  void extendMappings(DoEntityMappings<DO_ENTITY, PEER> mappings);
}
