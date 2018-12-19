/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO sme [9.0] mark as bean related methods as deprecated (use BeanTestingHelper instead) and remove in 10.0.
// Methods clearHttpAuthenticationCache, registerWithReplace, registerWithTestingOrder will be removed completely.
public final class TestingUtility {

  private static final Logger LOG = LoggerFactory.getLogger(TestingUtility.class);

  /**
   * @see TestingHelper#TESTING_BEAN_ORDER.
   */
  public static final int TESTING_BEAN_ORDER = BeanTestingHelper.TESTING_BEAN_ORDER;

  /**
   * @see TestingHelper#TESTING_RESOURCE_ORDER.
   */
  public static final int TESTING_RESOURCE_ORDER = BeanTestingHelper.TESTING_RESOURCE_ORDER;

  private TestingUtility() {
  }

  /**
   * @see TestingHelper#registerBeans(BeanMetaData...).
   */
  public static List<IBean<?>> registerBeans(BeanMetaData... beanDatas) {
    return BeanTestingHelper.get().registerBeans(beanDatas);
  }

  /**
   * @see BeanTestingHelper#registerBean(BeanMetaData)
   */
  public static IBean<?> registerBean(BeanMetaData beanData) {
    return BeanTestingHelper.get().registerBean(beanData);
  }

  /**
   * Register a new bean to replace an existing bean.
   */
  public static IBean<?> registerWithReplace(Class<?> beanClass) {
    IBean<?> bean = BEANS.getBeanManager().getBean(beanClass);
    BeanMetaData newBean = new BeanMetaData(bean).withReplace(true);
    return BEANS.getBeanManager().registerBean(newBean);
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
   * @see BeanTestingHelper#unregisterBean(IBean)
   */
  public static void unregisterBean(IBean<?> bean) {
    BeanTestingHelper.get().unregisterBean(bean);
  }

  /**
   * @see BeanTestingHelper#unregisterBeans(List)
   */
  public static void unregisterBeans(List<? extends IBean<?>> beans) {
    BeanTestingHelper.get().unregisterBeans(beans);
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
      LOG.warn("Could not clear HTTP authentication cache", e);
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
    String suffix = percentSuffix.getSuffix(symbols);

    // special case for some magic Arabic locales in which the percent sign is an invisible character that cannot be displayed
    byte[] suffixBytes = suffix.getBytes(java.nio.charset.StandardCharsets.UTF_16BE);
    if (suffixBytes.length == 2 && suffixBytes[0] == 32 && suffixBytes[1] == 14) {
      return sb.append('%').toString();
    }

    return sb.append(suffix).toString();
  }

  /**
   * Invokes the GC several times and verifies that the object referenced by the weak reference was garbage collected.
   */
  @SuppressWarnings("squid:S1215")
  public static void assertGC(WeakReference<?> ref) {
    int maxRuns = 50;
    for (int i = 0; i < maxRuns; i++) {
      if (ref.get() == null) {
        return;
      }
      System.gc();

      SleepUtil.sleepSafe(50, TimeUnit.MILLISECONDS);
    }
    Assert.fail("Potential memory leak, object " + ref.get() + "still exists after gc");
  }

  public static <T> IBean<?> mockConfigProperty(Class<? extends IConfigProperty<T>> propertyClass, T value) {
    return BeanTestingHelper.get().mockConfigProperty(propertyClass, value);
  }
}
