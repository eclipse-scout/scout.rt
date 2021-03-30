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
