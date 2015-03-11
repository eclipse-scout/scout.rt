/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.internal;

import org.eclipse.scout.jaxws.JaxWs216Module;
import org.eclipse.scout.jaxws.security.provider.ConfigIniAuthenticator;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanContributor;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;

/**
 * Bean Contributor for JAX-WS Scout RT.
 */
public class BeanContributor implements IBeanContributor {

  @Override
  public void contributeBeans(IClassInventory classInventory, IBeanContext context) {
    context.registerClass(JaxWs216Module.class);
    context.registerClass(ConfigIniAuthenticator.class);
  }
}
