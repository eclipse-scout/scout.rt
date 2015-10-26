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
package org.eclipse.scout.rt.server.jaxws;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsImplementorProperty;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;

/**
 * Convenience listener to activate configured {@link JaxWsImplementorSpecifics} by adding a {@link Replace} annotation
 * to it.
 *
 * @since 5.1
 */
public class JaxWsPlatformListener implements IPlatformListener {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JaxWsPlatformListener.class);

  @Override
  public void stateChanged(final PlatformEvent event) throws PlatformException {
    IPlatform platform = event.getSource();

    switch (event.getState()) {
      case BeanManagerPrepared:
        installImplementorSpecifics(platform.getBeanManager());
        break;
      case BeanManagerValid:
        logImplementorInfo(platform.getBeanManager());
        break;
    }
  }

  protected void installImplementorSpecifics(final IBeanManager beanManager) {
    final String jaxwsImplementor = CONFIG.getPropertyValue(JaxWsImplementorProperty.class);
    if (jaxwsImplementor == null) {
      return;
    }

    try {
      final Class<?> jaxwsImplementorClazz = Class.forName(jaxwsImplementor);
      Assertions.assertTrue(JaxWsImplementorSpecifics.class.isAssignableFrom(jaxwsImplementorClazz), "Implementor class must be of type '%s'.", JaxWsImplementorSpecifics.class.getName());

      beanManager.unregisterClass(jaxwsImplementorClazz); // Unregister the Bean first, so it can be registered with @Replace annotation anew.
      beanManager.registerBean(new BeanMetaData(jaxwsImplementorClazz).withReplace(true));

      LOG.info("JAX-WS implementor specific class installed: {}", jaxwsImplementorClazz.getName());
    }
    catch (final ClassNotFoundException e) {
      throw new PlatformException("Configured JAX-WS implementor specific class", e);
    }
  }

  protected void logImplementorInfo(final IBeanManager beanManager) {
    LOG.info("JAX-WS implementor: " + BEANS.get(JaxWsImplementorSpecifics.class).getVersionInfo());
  }
}
