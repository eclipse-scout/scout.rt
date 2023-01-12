/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import java.security.Permission;
import java.security.Permissions;
import java.util.Date;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.serialization.IObjectReplacer;
import org.eclipse.scout.rt.platform.util.date.UTCDate;

/**
 * Object replacer that supports the following cases
 * <ul>
 * <li><b>Dates</b> all objects instance of {@link Date} - except {@link UTCDate} - are replaced by a {@link StaticDate}
 * , which is timezone independent.</li>
 * <li><b>Permissions</b> {@link Permissions} objects are replaced by a {@link LenientPermissionsWrapper}, which does
 * not break deserialization if a {@link Permission} class is not available on the other side of a service tunnel.</li>
 * </ul>
 *
 * @since 3.8.2
 */
public class ServiceTunnelObjectReplacer implements IObjectReplacer {

  @Override
  public Object replaceObject(Object obj) {
    if (obj instanceof Date && !(obj instanceof UTCDate)) {
      return new StaticDate((Date) obj);
    }
    if (obj != null && obj.getClass() == Permissions.class) {
      return new LenientPermissionsWrapper((Permissions) obj);
    }
    if (obj instanceof IDoEntity) {
      return new DoEntityWrapper((IDoEntity) obj);
    }
    return obj;
  }

  @Override
  public Object resolveObject(Object obj) {
    if (obj instanceof StaticDate) {
      return ((StaticDate) obj).getDate();
    }
    if (obj instanceof LenientPermissionsWrapper) {
      return ((LenientPermissionsWrapper) obj).getPermissions();
    }
    if (obj instanceof DoEntityWrapper) {
      return ((DoEntityWrapper) obj).getDoEntity();
    }
    return obj;
  }
}
