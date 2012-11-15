/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.scout.commons.osgi.BundleObjectInputStream;
import org.osgi.framework.Bundle;

/**
 * Serialization override<br>
 * used to make Date's timezone independent using StaticDate class
 * and lazy (convenient) permissions loading using {@link LenientPermissionsWrapper}
 */
public class ServiceTunnelInputStream extends BundleObjectInputStream {

  public ServiceTunnelInputStream(InputStream in, Bundle[] bundleList) throws IOException {
    super(in, bundleList);
  }

  @Override
  protected Object resolveObject(Object obj) throws IOException {
    if (obj instanceof StaticDate) {
      return ((StaticDate) obj).getDate();
    }
    if (obj instanceof LenientPermissionsWrapper) {
      return ((LenientPermissionsWrapper) obj).getPermissions();
    }
    return super.resolveObject(obj);
  }
}
