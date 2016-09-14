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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Permission;
import java.security.Permissions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for {@link Permissions} that is serialize safe. This is useful to transfer permission collections that may
 * contain permissions not known by the consumer. This wrapper simply ignores those.
 */
public class LenientPermissionWrapper implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(LenientPermissionWrapper.class);

  /*
   * do NOT change this field name, it is used as text in readObject and writeObject
   */
  private Permission m_permission;
  /*
   * do NOT change this field name, it is used as text in readObject and writeObject
   */
  private String m_className;

  public LenientPermissionWrapper(Permission p) {
    m_permission = p;
    m_className = p.getClass().getName();
  }

  public Permission getPermission() {
    return m_permission;
  }

  /**
   * @serialData Default fields.
   */
  private void writeObject(ObjectOutputStream out) throws IOException {
    //no call to defaultWriteObject
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectOutputStream localOut = new ObjectOutputStream(bout);
    try {
      localOut.writeObject(m_permission);
    }
    finally {
      localOut.close();
    }
    byte[] data = bout.toByteArray();
    //
    ObjectOutputStream.PutField pfields = out.putFields();
    pfields.put("m_className", m_className);
    pfields.put("m_permission", data);
    out.writeFields();
  }

  @SuppressWarnings("squid:S1181")
  private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    //no call to defaultReadObject
    try {
      ObjectInputStream.GetField gfields = in.readFields();
      m_className = (String) gfields.get("m_className", (String) null);
      byte[] data = (byte[]) gfields.get("m_permission", (byte[]) null);

      ObjectInputStream localIn = new ObjectInputStream(new ByteArrayInputStream(data));
      try {
        m_permission = (Permission) localIn.readObject();
      }
      finally {
        localIn.close();
      }
    }
    catch (Throwable t) {
      LOG.warn("cannot deserialize permission", t);
    }
  }
}
