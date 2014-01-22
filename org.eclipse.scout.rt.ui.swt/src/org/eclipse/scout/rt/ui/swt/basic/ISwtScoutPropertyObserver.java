/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.basic;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;

/**
 * Root interface for classes rendering elements from the Scout model.
 * 
 * @since 3.10.0-M5
 */
public interface ISwtScoutPropertyObserver<T extends IPropertyObserver> {

  T getScoutObject();

  ISwtEnvironment getEnvironment();

}
