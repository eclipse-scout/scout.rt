/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit test for {@link LenientPermissionWrapper}
 */
public class LenientPermissionWrapperTest {
  private static final Logger LOG = LoggerFactory.getLogger(LenientPermissionWrapperTest.class);

  private static String data =
      "rO0ABXNyAENvcmcuZWNsaXBzZS5zY291dC5ydC5zaGFyZWQuc2VydmljZXR1bm5lbC5MZW5pZW50UGVybWlzc2lvbnNXcmFwcGVyAAAAAAAAAAEDAAFMAA1tX3Blcm1pc3Npb25zdAAbTGphdmEvc2VjdXJpdHkvUGVybWlzc2lvbnM7eHBzcgATamF2YS51dGlsLkFycmF5TGlzdHiB0h2Zx2GdAwABSQAEc2l6ZXhwAAAAA3cEAAAACnNyAEJvcmcuZWNsaXBzZS5zY291dC5ydC5zaGFyZWQuc2VydmljZXR1bm5lbC5MZW5pZW50UGVybWlzc2lvbldyYXBwZXIAAAAAAAAAAQMAAkwAC21fY2xhc3NOYW1ldAASTGphdmEvbGFuZy9TdHJpbmc7TAAMbV9wZXJtaXNzaW9udAAaTGphdmEvc2VjdXJpdHkvUGVybWlzc2lvbjt4cHQASG9yZy5lY2xpcHNlLnNjb3V0LnJ0LnNoYXJlZC5zZXJ2aWNldHVubmVsLkxlbmllbnRQZXJtaXNzaW9uV3JhcHBlclRlc3QkQXVyAAJbQqzzF/gGCFTgAgAAeHAAAADQrO0ABXNyAEhvcmcuZWNsaXBzZS5zY291dC5ydC5zaGFyZWQuc2VydmljZXR1bm5lbC5MZW5pZW50UGVybWlzc2lvbldyYXBwZXJUZXN0JEEAAAAAAAAAAQIAAHhyAB1qYXZhLnNlY3VyaXR5LkJhc2ljUGVybWlzc2lvblclC9zPTqZ6AgAAeHIAGGphdmEuc2VjdXJpdHkuUGVybWlzc2lvbrHG4T8oV1F+AgABTAAEbmFtZXQAEkxqYXZhL2xhbmcvU3RyaW5nO3hwdAABQXhzcQB+AAV0AEhvcmcuZWNsaXBzZS5zY291dC5ydC5zaGFyZWQuc2VydmljZXR1bm5lbC5MZW5pZW50UGVybWlzc2lvbldyYXBwZXJUZXN0JEN1cQB+AAoAAADQrO0ABXNyAEhvcmcuZWNsaXBzZS5zY291dC5ydC5zaGFyZWQuc2VydmljZXR1bm5lbC5MZW5pZW50UGVybWlzc2lvbldyYXBwZXJUZXN0JEMAAAAAAAAAAQIAAHhyAB1qYXZhLnNlY3VyaXR5LkJhc2ljUGVybWlzc2lvblclC9zPTqZ6AgAAeHIAGGphdmEuc2VjdXJpdHkuUGVybWlzc2lvbrHG4T8oV1F+AgABTAAEbmFtZXQAEkxqYXZhL2xhbmcvU3RyaW5nO3hwdAABQ3hzcQB+AAV0AEhvcmcuZWNsaXBzZS5zY291dC5ydC5zaGFyZWQuc2VydmljZXR1bm5lbC5MZW5pZW50UGVybWlzc2lvbldyYXBwZXJUZXN0JEJ1cQB+AAoAAADQrO0ABXNyAEhvcmcuZWNsaXBzZS5zY291dC5ydC5zaGFyZWQuc2VydmljZXR1bm5lbC5MZW5pZW50UGVybWlzc2lvbldyYXBwZXJUZXN0JEIAAAAAAAAAAQIAAHhyAB1qYXZhLnNlY3VyaXR5LkJhc2ljUGVybWlzc2lvblclC9zPTqZ6AgAAeHIAGGphdmEuc2VjdXJpdHkuUGVybWlzc2lvbrHG4T8oV1F+AgABTAAEbmFtZXQAEkxqYXZhL2xhbmcvU3RyaW5nO3hwdAABQnh4eA==";

  /**
   * main function to create (sysout) the {@link #data} java Code:
   */
  public static void main(String[] args) throws Exception {
    new LenientPermissionWrapperTest().write();
  }

  private void write() throws Exception {
    Permissions p = new Permissions();
    p.add(new A());
    p.add(new B_XXX());//rename to B to re-create the test input data string
    p.add(new C());
    byte[] b = getObjectSerializer().serialize(p);
    LOG.debug("private static String data=\"" + Base64Utility.encode(b) + "\";");
  }

  private IObjectSerializer getObjectSerializer() {
    return SerializationUtility.createObjectSerializer(new ServiceTunnelObjectReplacer());
  }

  @Test
  public void read() throws Exception {
    // FIXME abr: switch logging off and on again
//    Logger logger2 = Logger.getLogger(LenientPermissionWrapper.class.getName());
//    Level oldLevel2 = logger2.getLevel();
//    try {
//      logger2.setLevel(Level.OFF);
    //
    Permissions actual = getObjectSerializer().deserialize(new ByteArrayInputStream(Base64Utility.decode(data)), Permissions.class);
    Permissions expected = new Permissions();
    expected.add(new A());
    expected.add(new C());
    assertPermissionsEqual(expected, actual);
//    }
//    finally {
//      logger2.setLevel(oldLevel2);
//    }
  }

  public static void assertPermissionsEqual(Permissions expected, Permissions actual) {
    ArrayList<Permission> e = new ArrayList<Permission>();
    for (Enumeration<Permission> en = expected.elements(); en.hasMoreElements();) {
      e.add(en.nextElement());
    }
    ArrayList<Permission> a = new ArrayList<Permission>();
    for (Enumeration<Permission> en = actual.elements(); en.hasMoreElements();) {
      a.add(en.nextElement());
    }
    assertEquals(e, a);
  }

  public static class A extends BasicPermission {
    private static final long serialVersionUID = 1L;

    public A() {
      super("A");
    }
  }

  public static class B_XXX extends BasicPermission {
    private static final long serialVersionUID = 1L;

    public B_XXX() {
      super("B");
    }
  }

  public static class C extends BasicPermission {
    private static final long serialVersionUID = 1L;

    public C() {
      super("C");
    }
  }
}
