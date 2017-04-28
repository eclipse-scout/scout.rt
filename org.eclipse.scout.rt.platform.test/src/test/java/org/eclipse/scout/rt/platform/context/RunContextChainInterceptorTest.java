package org.eclipse.scout.rt.platform.context;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h3>{@link RunContextChainInterceptorTest}</h3>
 *
 * @author aho
 */
@RunWith(PlatformTestRunner.class)
public class RunContextChainInterceptorTest {

  private static final ThreadLocal<String> TL_NOT_IN_RUNCONTEXT = new ThreadLocal<String>();
  private static final ThreadLocal<String> COLOR_TL = new ThreadLocal<String>();
  private List<IBean<?>> m_registeredBeans = new ArrayList<>();
  private final List<Integer> m_activityLog = new ArrayList<>();

  @Before
  public void registerInterceptor() {
    try {
      m_registeredBeans.add(BEANS.getBeanManager().registerBean(
          new BeanMetaData(ActivitiyLogInterceptorProducer.class)
              .withApplicationScoped(true).withInitialInstance(new ActivitiyLogInterceptorProducer())));
      m_registeredBeans.add(BEANS.getBeanManager().registerBean(
          new BeanMetaData(ThreadLocalColorInterceptorProducer.class)
              .withApplicationScoped(true).withInitialInstance(new ThreadLocalColorInterceptorProducer())));
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
  public void testInterception() {

    BEANS.get(RunContext.class).run(new IRunnable() {

      @Override
      public void run() throws Exception {
        m_activityLog.add(2);
      }
    });
    Assert.assertEquals("1,2,3", CollectionUtility.format(m_activityLog, ","));
  }

  @Test
  public void testAsyncCallWithThreadLocal() {
    String backup = COLOR_TL.get();
    String backupNotInRunctontext = TL_NOT_IN_RUNCONTEXT.get();
    try {
      TL_NOT_IN_RUNCONTEXT.set("red");
      COLOR_TL.set("blue");
      Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          Assert.assertNull(TL_NOT_IN_RUNCONTEXT.get());
          Assert.assertEquals("blue", COLOR_TL.get());
        }
      }, Jobs.newInput().withRunContext(RunContexts.copyCurrent())).awaitDone();
    }
    finally {
      COLOR_TL.set(backup);
      TL_NOT_IN_RUNCONTEXT.set(backupNotInRunctontext);
    }

    BEANS.get(RunContext.class).run(new IRunnable() {

      @Override
      public void run() throws Exception {

        m_activityLog.add(2);
      }
    });
  }

  @Test
  public void testRunContextEmpty() {
    String backup = COLOR_TL.get();
    try {
      COLOR_TL.set("blue");
      Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          Assert.assertEquals(null, COLOR_TL.get());
        }
      }, Jobs.newInput().withRunContext(RunContexts.empty())).awaitDone();
    }
    finally {
      COLOR_TL.set(backup);
    }

  }

  private class ActivitiyLogInterceptorProducer implements IRunContextChainInterceptorProducer<RunContext> {
    @Override
    public <R> IRunContextChainInterceptor<R> create() {

      return new AbstractRunContextChainInterceptor<R>() {

        @Override
        public R intercept(Chain<R> chain) throws Exception {
          try {
            m_activityLog.add(1);
            return chain.continueChain();
          }
          finally {
            m_activityLog.add(3);
          }
        }

        @Override
        public boolean isEnabled() {
          return true;
        }

      };
    }
  }

  private class ThreadLocalColorInterceptorProducer implements IRunContextChainInterceptorProducer<RunContext> {
    @Override
    public <R> IRunContextChainInterceptor<R> create() {
      return new P_ColorThreadLocalInterceptor<R>();
    }
  }

  private class P_ColorThreadLocalInterceptor<RESULT> extends AbstractRunContextChainInterceptor<RESULT> {

    private String m_color;

    @Override
    public void fillCurrent() {
      m_color = COLOR_TL.get();
    }

    @Override
    public RESULT intercept(Chain<RESULT> chain) throws Exception {
      String backup = COLOR_TL.get();
      try {
        COLOR_TL.set(m_color);
        return chain.continueChain();
      }
      finally {
        COLOR_TL.set(backup);
      }

    }

    @Override
    public boolean isEnabled() {
      return true;
    }
  }
}
