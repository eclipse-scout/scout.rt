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
package org.eclipse.scout.rt.platform.service;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;

/**
 * Marker interface to automatically declare beans as {@link ApplicationScoped} services.<br>
 * A service is like any normal {@link ApplicationScoped} bean. <br>
 * See also {@link Order} for defining bean orders and {@link Replace} to hide the super class bean.
 */
@ApplicationScoped
public interface IService {

}
