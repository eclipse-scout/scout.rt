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
package org.eclipse.scout.rt.client;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;

/**
 * Handling of various framework aspects regarding memory allocation, caching and lazy-ness
 * <p>
 * The use of which profile to be used in {@link IClientSession} can be set in the config.properties as
 * <code>org.eclipse.scout.memory=small | medium | large</code>
 * <ul>
 * <li>small: low profile hardware, citrix and alike, memory consumption below 256m, no caching where not explicitly
 * needed. {@link SmallMemoryPolicy}</li>
 * <li>medium: medium profile hardware, old PC's and alike, memory consumption in the area 256m-512m, but caching is ok
 * to gain speed. {@link MediumMemoryPolicy}</li>
 * <li>large: client pc's and customer workstations, memory consumption can be 512m or more, caching whenever needed for
 * optimal speed. {@link LargeMemoryPolicy}</li>
 * </ul>
 */
public interface IMemoryPolicy {

  /**
   * After policy was set to a {@link IClientSession}.
   */
  void addNotify();

  /**
   * Before policy is removed from a {@link IClientSession} and replaced by another one.
   */
  void removeNotify();

  /**
   * This method is called just after a new page was created using {@link IPage#initPage()}.
   * <p>
   * Do not access the {@link IPageWithTable#getSearchFormInternal()} or {@link IPage#getTable()} since it is lazy
   * created.
   * <p>
   * For search form caching use {@link #pageSearchFormStarted(IPageWithTable)} instead.<br>
   * For table caching use {@link #pageTableCreated(IPage)} instead.
   */
  void pageCreated(IPage<?> page);

  /**
   * This method is called just after a search form inside a page was started using {@link ISearchForm#startSearch()}
   */
  void pageSearchFormStarted(IPageWithTable<?> page);

  /**
   * Whenever a new page is selected this method is called to give the possibility to release unused pages.
   */
  void afterOutlineSelectionChanged(IDesktop desktop);

  /**
   * Before data is fetched and loaded this method is called to give the possibility to previously manipulate the table.
   */
  void beforeTablePageLoadData(IPageWithTable<?> page);

  /**
   * After data is fetched and loaded this method is called to give the possibility to manipulate the table.
   */
  void afterTablePageLoadData(IPageWithTable<?> page);

  /**
   * This method is called after the {@link ITable} of the {@link IPage} has been created.
   * 
   * @param p
   */
  void pageTableCreated(IPage<?> p);

}
