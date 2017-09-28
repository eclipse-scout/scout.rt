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
package org.eclipse.scout.rt.client.context;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContexts.ClientRunContextFactory;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.eclipse.scout.rt.platform.context.AbstractRunContextChainInterceptor;
import org.eclipse.scout.rt.platform.context.IRunContextChainInterceptor;
import org.eclipse.scout.rt.platform.context.IRunContextChainInterceptorProducer;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContextChainIntercepterRegistry;
import org.eclipse.scout.rt.platform.context.RunContexts.RunContextFactory;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h3>{@link ClientRunContextWithInterceptorProducerTest}</h3>
 */
@RunWith(PlatformTestRunner.class)
public class ClientRunContextWithInterceptorProducerTest {

  private static final ThreadLocal<String> TL_CLIENT_RUN_CONTEXT = new ThreadLocal<String>();
  private static final ThreadLocal<String> TL_RUN_CONTEXT = new ThreadLocal<String>();
  private List<IBean<?>> m_registeredBeans = new ArrayList<>();

  @Before
  public void registerInterceptor() {
    try {
      m_registeredBeans.add(BEANS.getBeanManager().registerBean(
          new BeanMetaData(ClientRunContextInterceptorProducer.class)
              .withApplicationScoped(true).withInitialInstance(new ClientRunContextInterceptorProducer())));
      m_registeredBeans.add(BEANS.getBeanManager().registerBean(
          new BeanMetaData(RunContextInterceptorProducer.class)
              .withApplicationScoped(true).withInitialInstance(new RunContextInterceptorProducer())));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    BEANS.get(RunContextChainIntercepterRegistry.class).reindex();
  }

  @After
  public void unregisterInterceptor() {
    IBeanManager manager = BEANS.getBeanManager();
    for (IBean<?> bean : m_registeredBeans) {
      manager.unregisterBean(bean);
    }
    BEANS.get(RunContextChainIntercepterRegistry.class).reindex();
  }

  @Test
  public void testRunContextInterception() {
    try {
      TL_CLIENT_RUN_CONTEXT.set("client.runcontext");
      TL_RUN_CONTEXT.set("runcontext");

      Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          Assert.assertNull(TL_CLIENT_RUN_CONTEXT.get());
          Assert.assertEquals("runcontext", TL_RUN_CONTEXT.get());
        }
      }, Jobs.newInput().withRunContext(new RunContextFactory().copyCurrent())).awaitDone();

    }
    finally {
      TL_CLIENT_RUN_CONTEXT.set(null);
      TL_RUN_CONTEXT.set(null);
    }
  }

  @Test
  public void testClientRunContextInterception() {
    try {
      TL_CLIENT_RUN_CONTEXT.set("client.runcontext");
      TL_RUN_CONTEXT.set("runcontext");

      Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          Assert.assertEquals("client.runcontext", TL_CLIENT_RUN_CONTEXT.get());
          Assert.assertEquals("runcontext", TL_RUN_CONTEXT.get());
        }
      }, Jobs.newInput().withRunContext(BEANS.get(ClientRunContextFactory.class).copyCurrent())).awaitDone();

    }
    finally {
      TL_CLIENT_RUN_CONTEXT.set(null);
      TL_RUN_CONTEXT.set(null);
    }
  }

  private class RunContextInterceptorProducer implements IRunContextChainInterceptorProducer<RunContext> {
    @Override
    public <RESULT> IRunContextChainInterceptor<RESULT> create() {
      return new AbstractRunContextChainInterceptor<RESULT>() {
        private String m_value;

        @Override
        public void fillCurrent() {
          m_value = TL_RUN_CONTEXT.get();
        }

        @Override
        public RESULT intercept(Chain<RESULT> chain) throws Exception {
          String backup = TL_RUN_CONTEXT.get();
          try {
            TL_RUN_CONTEXT.set(m_value);
            return chain.continueChain();
          }
          finally {
            TL_RUN_CONTEXT.set(backup);
          }

        }

        @Override
        public boolean isEnabled() {
          return true;
        }
      };
    }
  }

  private class ClientRunContextInterceptorProducer implements IRunContextChainInterceptorProducer<ClientRunContext> {
    @Override
    public <RESULT> IRunContextChainInterceptor<RESULT> create() {
      return new AbstractRunContextChainInterceptor<RESULT>() {
        private String m_value;

        @Override
        public void fillCurrent() {
          m_value = TL_CLIENT_RUN_CONTEXT.get();
        }

        @Override
        public RESULT intercept(Chain<RESULT> chain) throws Exception {
          String backup = TL_CLIENT_RUN_CONTEXT.get();
          try {
            TL_CLIENT_RUN_CONTEXT.set(m_value);
            return chain.continueChain();
          }
          finally {
            TL_CLIENT_RUN_CONTEXT.set(backup);
          }

        }

        @Override
        public boolean isEnabled() {
          return true;
        }
      };
    }
  }
}
