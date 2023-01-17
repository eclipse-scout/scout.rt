/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
