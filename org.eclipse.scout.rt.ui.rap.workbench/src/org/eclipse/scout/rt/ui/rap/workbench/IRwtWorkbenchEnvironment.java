/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.workbench;

import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;

/**
 * <h3>IRwtWorkbenchEnvironment</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 01.04.2011
 * @deprecated will be removed with the M-release.
 */
@Deprecated
public interface IRwtWorkbenchEnvironment extends IRwtEnvironment {

  boolean acquireActivateViewLock();

  void releaseActivateViewLock();

}
