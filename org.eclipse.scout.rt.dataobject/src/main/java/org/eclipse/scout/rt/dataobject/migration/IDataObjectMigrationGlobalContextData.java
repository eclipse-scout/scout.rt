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

/**
 * A global structure migration context data.
 * <p>
 * If a concrete subclass contains an @Bean annotation, it is auto-created when first accessed via
 * {@link DataObjectMigrationContext#getGlobal(Class)} and not existing yet.
 * <p>
 * Because a global context data may be accessed concurrently by different threads each implementation must be
 * thread-safe.
 */
public interface IDataObjectMigrationGlobalContextData {

  /**
   * @return Class used as identifier for context data (map key).
   */
  default Class<? extends IDataObjectMigrationGlobalContextData> getIdentifierClass() {
    return this.getClass();
  }
}
