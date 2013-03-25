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
package org.eclipse.scout.rt.client.ui.basic.activitymap;

import java.util.EventListener;

/**
 * In Scout 3.9 ActivityMapListener will be typed and this class will be removed.
 */
public interface ActivityMapListenerTyped extends EventListener {

  void activityMapChanged(ActivityMapEventTyped<?> e);

}
