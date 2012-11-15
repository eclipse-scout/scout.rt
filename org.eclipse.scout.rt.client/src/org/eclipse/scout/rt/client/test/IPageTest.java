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
package org.eclipse.scout.rt.client.test;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.service.IService;

/**
 * A test for one or several pages, that performs specific tests for these
 * pages. The service is used together with the {@link DrilldownOutlineUnitTest} and
 * {@link CurrentOutlineSearchFormUnitTest}. These tests then use this more
 * specific test if {@link IPageTest.canHandle} returns true.
 */
public interface IPageTest extends IService {

  /**
   * Can this IPageTest handle the page and perform a specific test for this page?
   * 
   * @param page
   *          The page to be tested.
   * @return
   */
  boolean canHandle(IPage page);

  /**
   * Perform a test on <code>page</code>. <code>canHandle(page) == true</code> can be assumed.
   * 
   * @param page
   *          The page to be tested.
   * @return Another page (typically a child page) for which further testing will be performed.
   * @throws Throwable
   */
  IPage testPage(IPage page) throws Throwable;

}
