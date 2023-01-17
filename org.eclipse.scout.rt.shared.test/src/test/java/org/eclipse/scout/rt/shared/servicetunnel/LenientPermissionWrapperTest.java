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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.logger.ILoggerSupport;
import org.eclipse.scout.rt.platform.logger.ILoggerSupport.LogLevel;
import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.junit.Test;

/**
 * JUnit test for {@link LenientPermissionWrapper}
 */
public class LenientPermissionWrapperTest {

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
    p.add(new B_YYY());//rename to B to re-create the test input data string
    p.add(new C());
    byte[] b = getObjectSerializer().serialize(p);
    System.out.println("private static String data=\"" + Base64Utility.encode(b) + "\";"); // sysout is desired here
  }

  private IObjectSerializer getObjectSerializer() {
    return SerializationUtility.createObjectSerializer(new ServiceTunnelObjectReplacer());
  }

  @Test
  public void read() throws Exception {
    ILoggerSupport loggerSupport = BEANS.get(ILoggerSupport.class);
    LogLevel oldLevel = loggerSupport.getLogLevel(LenientPermissionWrapper.class);
    try {
      loggerSupport.setLogLevel(LenientPermissionWrapper.class, LogLevel.OFF);

      Permissions actual = getObjectSerializer().deserialize(new ByteArrayInputStream(Base64Utility.decode(data)), Permissions.class);
      Permissions expected = new Permissions();
      expected.add(new A());
      expected.add(new C());
      assertPermissionsEqual(expected, actual);
    }
    finally {
      loggerSupport.setLogLevel(LenientPermissionWrapper.class, oldLevel);
    }
  }

  public static void assertPermissionsEqual(Permissions expected, Permissions actual) {
    ArrayList<Permission> e = new ArrayList<>();
    for (Enumeration<Permission> en = expected.elements(); en.hasMoreElements();) {
      e.add(en.nextElement());
    }
    ArrayList<Permission> a = new ArrayList<>();
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

  public static class B_YYY extends BasicPermission {
    private static final long serialVersionUID = 1L;

    public B_YYY() {
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
