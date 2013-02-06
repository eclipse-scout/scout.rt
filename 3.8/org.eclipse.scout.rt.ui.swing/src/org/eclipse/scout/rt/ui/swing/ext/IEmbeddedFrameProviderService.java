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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Frame;

import javax.swing.RootPaneContainer;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.service.IService;

/**
 * Support for in-browser application using a window handle to create an
 * sun.awt.EmbeddedFrame
 */
@Priority(-3)
public interface IEmbeddedFrameProviderService extends IService {

  /**
   * @return a {@link Frame} that must also implement {@link RootPaneContainer}
   */
  Frame createEmbeddedFrame(long hWnd);

}
