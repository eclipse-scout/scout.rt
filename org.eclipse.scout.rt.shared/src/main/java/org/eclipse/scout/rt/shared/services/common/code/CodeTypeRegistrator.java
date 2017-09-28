/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * registers all {@link ICodeType} beans using the {@link ICodeService} for instance caching between client and server
 */
public class CodeTypeRegistrator implements IPlatformListener {

  private static final Logger LOG = LoggerFactory.getLogger(CodeTypeRegistrator.class);

  @Override
  public void stateChanged(PlatformEvent e) {
    if (e.getState() == State.BeanManagerPrepared) {
      IBeanManager beanManager = e.getSource().getBeanManager();
      Set<Class<? extends ICodeType<?, ?>>> classes = BEANS.get(CodeTypeClassInventory.class).getClasses();
      for (Class<? extends ICodeType<?, ?>> c : classes) {
        LOG.debug("Register {}", c.getName());
        beanManager.registerBean(
            new BeanMetaData(c)
                .withProducer(new CodeTypeProducer()));
      }
      LOG.info("{} code type classes registered.", classes.size());
    }
  }

}
