/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
