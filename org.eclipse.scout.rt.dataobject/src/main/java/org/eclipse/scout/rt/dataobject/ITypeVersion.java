/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.namespace.INamespaceVersioned;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

/**
 * Type versions should be final classes.
 * <p>
 * Every ITypeVersion belongs to a single namespace.
 */
@ApplicationScoped
public interface ITypeVersion extends INamespaceVersioned {

  @Override
  default Collection<NamespaceVersion> getDependencies() {
    return Collections.emptyList();
  }
}
