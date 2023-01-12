/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.namespace;

import java.util.Collection;

/**
 * Interface for items possessing a namespace version and dependencies.
 */
public interface INamespaceVersioned {

  /**
   * @return non-null namespace version.
   */
  NamespaceVersion getVersion();

  /**
   * @return A collection of dependencies (never <code>null</code>).
   */
  Collection<NamespaceVersion> getDependencies();
}
