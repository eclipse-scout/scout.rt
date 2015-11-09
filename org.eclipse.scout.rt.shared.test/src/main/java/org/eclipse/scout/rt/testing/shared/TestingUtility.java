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
package org.eclipse.scout.rt.testing.shared;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.junit.Assert;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TestingUtility {

  private static final Logger LOG = LoggerFactory.getLogger(TestingUtility.class);

  /**
   * Order to make sure testing beans are preferred over regular beans
   */
  public static final int TESTING_BEAN_ORDER = -10000;

  /**
   * Order for testing beans to be used when needed
   */
  public static final int TESTING_RESOURSE_ORDER = 10000;

  private TestingUtility() {
  }

  /**
   * Wait until the condition returns a non-null result or timeout is reached.
   * <p>
   * When timeout is reached an exception is thrown.
   */
  public static <T> T waitUntil(long timeout, WaitCondition<T> w) throws Throwable {
    long ts = System.currentTimeMillis() + timeout;
    T t = w.run();
    while ((t == null) && System.currentTimeMillis() < ts) {
      Thread.sleep(40);
      t = w.run();
    }
    if (t != null) {
      return t;
    }
    else {
      throw new InterruptedException("timeout reached");
    }
  }

  /**
   * Registers the given beans in the {@link IBeanManager} of {@link Platform#get()} with an {@link Order} value of
   * {@link #TESTING_BEAN_ORDER} (if none is already set) that overrides all other beans
   * <p>
   * If registering Mockito mocks, use {@link BeanMetaData#BeanData(Class, Object)}.
   *
   * @return the registrations
   */
  public static List<IBean<?>> registerBeans(BeanMetaData... beanDatas) {
    if (beanDatas == null) {
      return CollectionUtility.emptyArrayList();
    }
    List<IBean<?>> registeredBeans = new ArrayList<>();
    for (BeanMetaData beanData : beanDatas) {
      registeredBeans.add(registerBean(beanData));
    }
    return registeredBeans;
  }

  /**
   * Registers the given bean in the {@link IBeanManager} of {@link Platform#get()} with an {@link Order} value of
   * {@link #TESTING_BEAN_ORDER} (if none is already set) that overrides all other beans. If the underlying instance is
   * mocked, any {@link TunnelToServer} annotation is removed.
   * <p>
   * If registering Mockito mocks, use {@link BeanMetaData#BeanData(Class, Object)}.
   *
   * @return the registration
   */
  public static IBean<?> registerBean(BeanMetaData beanData) {
    if (beanData == null) {
      return null;
    }
    boolean isMock = Mockito.mockingDetails(beanData.getBeanClazz()).isMock();
    Assertions.assertFalse(isMock && beanData.getInitialInstance() == null, "Cannot register mocked bean. Use 'registerService' and provide the concrete type. [mock=%s]",
        beanData.getBeanClazz());
    if (beanData.getBeanAnnotation(Order.class) == null) {
      beanData.withOrder(TESTING_BEAN_ORDER);
    }
    if (Mockito.mockingDetails(beanData.getInitialInstance()).isMock() && beanData.getBeanAnnotation(TunnelToServer.class) != null) {
      LOG.info("removing TunnelToServer annotation on mocked bean: {}", beanData.getBeanClazz());
      beanData.withoutAnnotation(TunnelToServer.class);
    }
    return Platform.get().getBeanManager().registerBean(beanData);
  }

  /**
   * Register an existing bean with order {@link TESTING_BEAN_ORDER}
   */
  public static IBean<?> registerWithTestingOrder(Class<?> beanClass) {
    IBean<?> bean = BEANS.getBeanManager().getBean(beanClass);
    BeanMetaData newBean = new BeanMetaData(bean).withOrder(TESTING_BEAN_ORDER);
    return BEANS.getBeanManager().registerBean(newBean);
  }

  /**
   * Unregister a bean
   */
  public static void unregisterBean(IBean<?> bean) {
    LinkedList<IBean<?>> beans = new LinkedList<>();
    beans.add(bean);
    unregisterBeans(beans);
  }

  /**
   * Unregisters the given beans
   *
   * @param beans
   */
  public static void unregisterBeans(List<? extends IBean<?>> beans) {
    if (beans == null) {
      return;
    }
    for (IBean<?> bean : beans) {
      Platform.get().getBeanManager().unregisterBean(bean);
    }
  }

  /**
   * Clears Java's HTTP authentication cache.
   *
   * @return Returns <code>true</code> if the operation was successful, otherwise <code>false</code>.
   */
  public static boolean clearHttpAuthenticationCache() {
    boolean successful = true;
    try {
      Class<?> c = Class.forName("sun.net.www.protocol.http.AuthCacheValue");
      Field cacheField = c.getDeclaredField("cache");
      cacheField.setAccessible(true);
      Object cache = cacheField.get(null);
      Field hashtableField = cache.getClass().getDeclaredField("hashtable");
      hashtableField.setAccessible(true);
      Map<?, ?> map = (Map<?, ?>) hashtableField.get(cache);
      map.clear();
    }
    catch (Exception e) {
      successful = false;
    }
    return successful;
  }

  /**
   * convenience overload for {@link #createLocaleSpecificNumberString(minus, integerPart, fractionPart, percent)} with
   * <code>percent=0</code>
   *
   * @param minus
   * @param integerPart
   * @param fractionPart
   * @return
   */
  public static String createLocaleSpecificNumberString(Locale loc, boolean minus, String integerPart, String fractionPart) {
    return createLocaleSpecificNumberString(loc, minus, integerPart, fractionPart, NumberStringPercentSuffix.NONE);
  }

  /**
   * convenience overload for {@link #createLocaleSpecificNumberString(minus, integerPart, fractionPart, percent)} with
   * <code>fractionPart=null</code> and <code>percent=0</code>
   *
   * @param minus
   * @param integerPart
   * @return
   */
  public static String createLocaleSpecificNumberString(Locale loc, boolean minus, String integerPart) {
    return createLocaleSpecificNumberString(loc, minus, integerPart, null, NumberStringPercentSuffix.NONE);
  }

  public enum NumberStringPercentSuffix {
    /**
     * ""
     */
    NONE {
      @Override
      public String getSuffix(DecimalFormatSymbols symbols) {
        return "";
      }
    },
    /**
     * "%"
     */
    JUST_SYMBOL {
      @Override
      public String getSuffix(DecimalFormatSymbols symbols) {
        return String.valueOf(symbols.getPercent());
      }
    },
    /**
     * " %'
     */
    BLANK_AND_SYMBOL {
      @Override
      public String getSuffix(DecimalFormatSymbols symbols) {
        return " " + symbols.getPercent();
      }
    };

    public abstract String getSuffix(DecimalFormatSymbols symbols);
  }

  /**
   * Create a string representing a number using locale specific minus, decimalSeparator and percent symbols
   *
   * @param minus
   * @param integerPart
   * @param fractionPart
   * @param percentSuffix
   * @return
   */
  public static String createLocaleSpecificNumberString(Locale loc, boolean minus, String integerPart, String fractionPart, NumberStringPercentSuffix percentSuffix) {
    DecimalFormatSymbols symbols = (BEANS.get(NumberFormatProvider.class).getPercentInstance(loc)).getDecimalFormatSymbols();
    StringBuilder sb = new StringBuilder();
    if (minus) {
      sb.append(symbols.getMinusSign());
    }
    sb.append(integerPart);
    if (fractionPart != null) {
      sb.append(symbols.getDecimalSeparator()).append(fractionPart);
    }
    sb.append(percentSuffix.getSuffix(symbols));
    return sb.toString();
  }

  /**
   * Invokes the GC several times and verifies that the object referenced by the weak reference was garbage collected.
   */
  public static void assertGC(WeakReference<?> ref) {
    int maxRuns = 50;
    for (int i = 0; i < maxRuns; i++) {
      if (ref.get() == null) {
        return;
      }
      System.gc();
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) {
        // NOP
      }
    }
    Assert.fail("Potential memory leak, object " + ref.get() + "still exists after gc");
  }

}
