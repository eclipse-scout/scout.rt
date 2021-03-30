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
package org.eclipse.scout.rt.dataobject.migration;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Interface for migration context data classes that are created and pushed as local context data to
 * {@link DoStructureMigrationContext} when a corresponding data object is traversed (see
 * {@link DoStructureMigrationContextDataTarget} annotation for data object matching).
 */
@Bean
public interface IDoStructureMigrationTargetContextData extends IDoStructureMigrationLocalContextData {

  /**
   * A context for a data object is initialized after the corresponding data object was migrated to the specific
   * version.
   *
   * @return <code>true</true> if it's a valid context and should be used, <code>false</false> to discard context.
   **/
  boolean initialize(DoStructureMigrationContext ctx, IDoEntity doEntity);
}
