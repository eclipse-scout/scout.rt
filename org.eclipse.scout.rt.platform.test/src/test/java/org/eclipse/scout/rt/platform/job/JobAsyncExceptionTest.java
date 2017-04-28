/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;
import org.eclipse.scout.rt.platform.context.AbstractRunContextChainInterceptor;
import org.eclipse.scout.rt.platform.context.IRunContextChainInterceptor;
import org.eclipse.scout.rt.platform.context.IRunContextChainInterceptorProducer;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContextChainIntercepterRegistry;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.internal.CallableChainHandledException;
import org.eclipse.scout.rt.platform.job.internal.JobFutureTask;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobAsyncExceptionTest {

  private List<IBean<?>> m_registeredBeans = new ArrayList<>();

  @Test
  public void testExceptionInChainInterceptor() throws Exception {
    registerTestBeans(new P_InterceptorThrowingExceptionProducer());
    P_JobManager jobManager = new P_JobManager();

    IFuture<Void> future = jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // NOP
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    future.awaitDone();
    Assert.assertTrue(jobManager.e1 instanceof PlatformException);
    Assert.assertTrue(jobManager.e2 instanceof PlatformException);
    Assert.assertNull(jobManager.e3);

  }

  @Test(expected = PlatformException.class)
  public void testExceptionInCallable() throws Exception {
    P_JobManager jobManager = new P_JobManager();

    IFuture<Void> future = jobManager.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        throw new PlatformException("Expected test exception");
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    future.awaitDone();
    Assert.assertTrue(jobManager.e1 instanceof PlatformException);
    Assert.assertTrue(jobManager.e2 instanceof CallableChainHandledException);
    Assert.assertTrue(jobManager.e3 instanceof PlatformException);

  }

  private void registerTestBeans(Object... beans) throws Exception {
    for (Object bean : beans) {
      m_registeredBeans.add(BEANS.getBeanManager().registerBean(
          new BeanMetaData(bean.getClass())
              .withApplicationScoped(true).withInitialInstance(bean)));
    }
    BEANS.get(RunContextChainIntercepterRegistry.class).reindex();
  }

  @After
  public void unregisterTestBeans() {
    IBeanManager manager = BEANS.getBeanManager();
    for (IBean<?> bean : m_registeredBeans) {
      manager.unregisterBean(bean);
    }
    BEANS.get(RunContextChainIntercepterRegistry.class).reindex();
  }

  private final class P_InterceptorThrowingExceptionProducer implements IRunContextChainInterceptorProducer<RunContext> {
    @Override
    public <RESULT> IRunContextChainInterceptor<RESULT> create() {
      return new AbstractRunContextChainInterceptor<RESULT>() {
        @Override
        public RESULT intercept(Chain<RESULT> chain) throws Exception {
          throw new PlatformException("Expected test exception");
        }

        @Override
        public boolean isEnabled() {
          return true;
        }
      };
    }
  }

  private final class P_JobManager extends JobManager {
    private Exception e1;
    private Exception e2;
    private Exception e3;

    @Override
    protected <RESULT> void interceptCallableChain(CallableChain<RESULT> callableChain, JobFutureTask<?> future, RunMonitor runMonitor, JobInput input) {

      super.interceptCallableChain(callableChain, future, runMonitor, input);
      // first
      callableChain.add(0, new ICallableInterceptor<RESULT>() {
        @Override
        public RESULT intercept(Chain<RESULT> chain) throws Exception {
          try {
            return chain.continueChain();
          }
          catch (Exception e) {
            e1 = e;
            throw e;
          }
        }

        @Override
        public boolean isEnabled() {
          return true;
        }
      });

      // after exception handler
      callableChain.add(2, new ICallableInterceptor<RESULT>() {
        @Override
        public RESULT intercept(Chain<RESULT> chain) throws Exception {
          try {
            return chain.continueChain();
          }
          catch (Exception e) {
            e2 = e;
            throw e;
          }
        }

        @Override
        public boolean isEnabled() {
          return true;
        }
      });

      // last
      callableChain.addLast(new ICallableInterceptor<RESULT>() {
        @Override
        public RESULT intercept(Chain<RESULT> chain) throws Exception {
          try {
            return chain.continueChain();
          }
          catch (Exception e) {
            e3 = e;
            throw e;
          }
        }

        @Override
        public boolean isEnabled() {
          return true;
        }
      });
    }

  }
}
