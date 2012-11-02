package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.security.Permission;
import java.security.Permissions;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleObjectInputStream;

/**
 * Wrapper for {@link Permissions} that is serialize safe.
 * This is useful to transfer permission collections that may contain permissions not known by the consumer.
 * This wrapper simply ignores those.
 */
public class LenientPermissionWrapper implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LenientPermissionWrapper.class);

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

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    //no call to defaultReadObject
    try {
      ObjectInputStream.GetField gfields = in.readFields();
      m_className = (String) gfields.get("m_className", (String) null);
      byte[] data = (byte[]) gfields.get("m_permission", (byte[]) null);
      //
      final BundleObjectInputStream bundleIn = (in instanceof BundleObjectInputStream ? (BundleObjectInputStream) in : null);
      ObjectInputStream localIn = new ObjectInputStream(new ByteArrayInputStream(data)) {
        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
          return bundleIn != null ? bundleIn.resolveClass(desc) : Class.forName(desc.getName());
        }
      };
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
