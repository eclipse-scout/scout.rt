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
package org.eclipse.scout.rt.client;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;

/**
 * Handling of various framework aspects regarding memory allocation, caching and lazy-ness
 * <p>
 * The use of which profile to be used in {@link IClientSession} can be set in the config.ini as
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
   * Whenever a new page is selected this methode is called to give the possibility to release unused pages.
   */
  void afterOutlineSelectionChanged(IDesktop desktop);

  void beforeTablePageLoadData(IPageWithTable<?> page);

  void afterTablePageLoadData(IPageWithTable<?> page);

}
