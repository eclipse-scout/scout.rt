/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.http;

/**
 * Surefire maven testing uses fork mode to speed up tests. Therefore it is crucial that every test uses distinct tcp
 * ports for the http server. The only thing that is guaranteed is that the test methods of one JUnit test class are run
 * in sequential order.
 * <p>
 * This class contains the used ports per test class. Every new test class adds a new port number for it to use.
 * <p>
 *
 * @since 9.x
 */
public interface TestingHttpPorts {
  int PORT_33000 = 33000;
  int PORT_33001 = 33001;
  int PORT_33002 = 33002;
  int PORT_33003 = 33003;
  int PORT_33004 = 33004;
  int PORT_33005 = 33005;
  int PORT_33006 = 33006;
  int PORT_33007 = 33007;
  //add new ports for every new test class
}
