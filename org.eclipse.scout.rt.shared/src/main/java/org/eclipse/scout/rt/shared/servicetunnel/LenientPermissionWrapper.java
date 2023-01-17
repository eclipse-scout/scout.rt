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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.Serializable;
import java.security.Permission;

import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for {@link Permission} that is serialize safe. This is useful to transfer permission collections that may
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
    byte[] data = SerializationUtility.createObjectSerializer().serialize(m_permission);
    //
    PutField pfields = out.putFields();
    pfields.put("m_className", m_className);
    pfields.put("m_permission", data);
    out.writeFields();
  }

  @SuppressWarnings("squid:S1181")
  private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    //no call to defaultReadObject
    try {
      GetField gfields = in.readFields();
      m_className = (String) gfields.get("m_className", null);
      byte[] data = (byte[]) gfields.get("m_permission", null);
      m_permission = SerializationUtility.createObjectSerializer().deserialize(data, Permission.class);
    }
    catch (Throwable t) {
      LOG.warn("cannot deserialize permission", t);
    }
  }
}
