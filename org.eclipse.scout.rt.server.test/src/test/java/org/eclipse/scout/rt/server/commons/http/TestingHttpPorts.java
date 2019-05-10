/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
  int PORT_33006 = 33006;
  //add new ports for every new test class
}
