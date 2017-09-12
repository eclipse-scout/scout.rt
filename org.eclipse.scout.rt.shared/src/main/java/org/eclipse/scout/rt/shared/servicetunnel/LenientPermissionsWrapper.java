/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.Serializable;
import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Wrapper for {@link Permissions} that is serialize safe. This is useful to transfer permission collections that may
 * contain permissions not known by the consumer. This wrapper simply ignores those.
 */
public class LenientPermissionsWrapper implements Serializable {
  private static final long serialVersionUID = 1L;

  /*
   * do NOT change this field name, it is used as text in readObject and writeObject
   */
  private Permissions m_permissions;

  public LenientPermissionsWrapper(Permissions p) {
    m_permissions = p;
  }

  public Permissions getPermissions() {
    return m_permissions;
  }

  /**
   * @serialData Default fields.
   */
  private void writeObject(ObjectOutputStream out) throws IOException {
    //no call to defaultWriteObject
    PutField pfields = out.putFields();
    List<LenientPermissionWrapper> list = null;
    if (m_permissions != null) {
      list = new ArrayList<>();
      for (Enumeration<Permission> en = m_permissions.elements(); en.hasMoreElements();) {
        Permission perm = en.nextElement();
        if (perm != null) {
          list.add(new LenientPermissionWrapper(perm));
        }
      }
    }
    pfields.put("m_permissions", list);
    out.writeFields();
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    //no call to defaultReadObject
    m_permissions = new Permissions();
    GetField gfields = in.readFields();
    Iterable<LenientPermissionWrapper> list = (ArrayList<LenientPermissionWrapper>) gfields.get("m_permissions", null);
    if (list != null) {
      for (LenientPermissionWrapper w : list) {
        if (w.getPermission() != null) {
          m_permissions.add(w.getPermission());
        }
      }
    }
  }

}
