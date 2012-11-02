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
import java.io.OutputStream;
import java.security.Permissions;
import java.util.Date;

import org.eclipse.scout.commons.UTCDate;
import org.eclipse.scout.commons.osgi.BundleObjectOutputStream;

/**
 * Serialization override<br>
 * used partly to make Date's timezone independent using StaticDate class
 * <p>
 * {@link UTCDate}s are not converted and pass unchanged.
 * <p>
 * converts {@link Permissions} to {@link LenientPermissionsWrapper}
 */
public class ServiceTunnelOutputStream extends BundleObjectOutputStream {

  public ServiceTunnelOutputStream(OutputStream out) throws IOException {
    super(out);
  }

  @Override
  protected Object replaceObject(Object obj) throws IOException {
    if (obj instanceof Date && !(obj instanceof UTCDate)) {
      return new StaticDate((Date) obj);
    }
    if (obj != null && obj.getClass() == Permissions.class) {
      return new LenientPermissionsWrapper((Permissions) obj);
    }
    return super.replaceObject(obj);
  }
}
