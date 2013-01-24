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
package org.eclipse.scout.rt.shared.services.common.test;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.service.IService;

/**
 *
 * @deprecated Use Scout JUnit Testing Support: {@link org.eclipse.scout.testing.client.runner.ScoutClientTestRunner} or
 *             {@link org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner} to run Unit tests.
 *
 *
 * <h1>Integration test service</h1> A test gets a test context {@link #setTestContext(ITestContext)} and is
 * launched by calling {@link #run()}
 * <p>
 * Normally a name of a test contains the three parts: product, title, subTitle <br>
 * Examples:
 * <p>
 * <code>BSI CRM . LookupServices . TicketPhaseLookupService.getByTest</code> <br>
 * <code>Marco Polo . ProcessServices . PersonProcess.load</code>
 */
@Priority(-3)
@Deprecated
@SuppressWarnings("deprecation")
public interface ITest extends IService {

  String getProduct();

  String getTitle();

  String getSubTitle();

  /**
   * called before the test is run and before {@link #setUp()}
   */
  void setTestContext(ITestContext ctx);

  /**
   * called before the test is run
   */
  void setUp() throws Throwable;

  /**
   * implement in subclass
   */
  void run() throws Throwable;

  /**
   * called after the test was run
   */
  void tearDown() throws Throwable;

}
