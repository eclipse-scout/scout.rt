package org.eclipse.scout.rt.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PreDestroy;

import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.platform.internal.PlatformImplementor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * <h3>{@link PreDestroyTest}</h3>
 */
public class PreDestroyTest {

  private static List<String> events = new ArrayList<>();

  @AfterClass
  public static void cleanup() {
    events = null;
  }

  @Before
  public void prepareTest() {
    events.clear(); // reset for next test
  }

  @Test
  public void testPreDestroyOnUnusedBean() {
    IPlatform platform = privatePlatformWith(
        new BeanMetaData(P_FixtureWithSingleValidPreDestroy.class)
            .withApplicationScoped(true));
    platform.stop();
    assertTrue(events.isEmpty());
  }

  @Test
  public void testPreDestroyOnNonApplicationScopedBean() {
    IPlatform platform = privatePlatformWith(
        new BeanMetaData(P_FixtureWithSingleValidPreDestroy.class));
    P_FixtureWithSingleValidPreDestroy instance = platform.getBeanManager().getBean(P_FixtureWithSingleValidPreDestroy.class).getInstance();
    assertNotNull(instance);
    platform.stop();
    assertTrue(events.isEmpty());
  }

  @Test
  public void testPreDestroyOnSimpleBean() {
    IPlatform platform = privatePlatformWith(
        new BeanMetaData(P_FixtureWithSingleValidPreDestroy.class)
            .withApplicationScoped(true));
    P_FixtureWithSingleValidPreDestroy instance = platform.getBeanManager().getBean(P_FixtureWithSingleValidPreDestroy.class).getInstance();
    assertNotNull(instance);
    platform.stop();
    assertEquals(Arrays.asList("cleanup"), events);
  }

  @Test
  public void testPreDestroyThrowingException() {
    IPlatform platform = privatePlatformWith(new BeanMetaData(P_FixtureWithPreDestroyThrowingException.class).withApplicationScoped(true));
    assertNotNull(platform.getBeanManager().getBean(P_FixtureWithPreDestroyThrowingException.class).getInstance());
    platform.stop();

    assertEquals(Arrays.asList("cleanupOne", "cleanupTwo"), events);
  }

  @Test
  public void testPreDestroyWithInheritedMethods() {
    IPlatform platform = privatePlatformWith(new BeanMetaData(P_FixtureWithInherited.class).withApplicationScoped(true));
    assertNotNull(platform.getBeanManager().getBean(P_FixtureWithInherited.class).getInstance());
    platform.stop();

    assertEquals(Arrays.asList("cleanupOne", "cleanupTwoInherited"), events);
  }

  @Test
  public void testPreDestroyWithInheritedPrivateMethods() {
    IPlatform platform = privatePlatformWith(new BeanMetaData(P_FixtureWithInheritedPrivate.class).withApplicationScoped(true));
    assertNotNull(platform.getBeanManager().getBean(P_FixtureWithInheritedPrivate.class).getInstance()); // create the instance
    platform.stop();

    assertEquals(Arrays.asList("cleanupChild", "cleanup"), events);
  }

  private static class P_FixtureWithSingleValidPreDestroy {

    @PreDestroy
    private final void cleanup() {
      events.add("cleanup");
    }

    @PreDestroy
    private static void cleanupStatic() {
      events.add("cleanupStatic");
    }

    @PreDestroy
    private void cleanupWithArgs(int param) {
      events.add("cleanupWithArgs");
    }
  }

  private static class P_FixtureWithPreDestroyThrowingException {
    @PreDestroy
    public void cleanupOne() throws Exception {
      events.add("cleanupOne");
      throw new Exception("test");
    }

    @PreDestroy
    public void cleanupTwo() {
      events.add("cleanupTwo");
    }
  }

  private static final class P_FixtureWithInherited extends P_FixtureWithPreDestroyThrowingException {
    @Override
    public void cleanupTwo() {
      events.add("cleanupTwoInherited");
    }
  }

  private static final class P_FixtureWithInheritedPrivate extends P_FixtureWithSingleValidPreDestroy {
    @PreDestroy
    private final void cleanup() {
      events.add("cleanupChild");
    }
  }

  private IPlatform privatePlatformWith(final BeanMetaData bean) {
    PlatformImplementor platform = new PlatformImplementor() {

      @Override
      protected void validateConfiguration() {
        // nop
      }

      @Override
      protected void initBeanDecorationFactory() {
        // nop
      }

      @Override
      protected void validateHeadless() {
        // nop
      }

      @Override
      protected BeanManagerImplementor createBeanManager() {
        BeanManagerImplementor beanManager = new BeanManagerImplementor();
        beanManager.registerBean(bean);
        return beanManager;
      }
    };
    platform.start();
    return platform;
  }
}
