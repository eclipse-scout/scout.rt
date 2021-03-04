/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
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
