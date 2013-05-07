/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.holders.LongArrayHolder;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link TypeCastUtility}
 */
public class TypeCastUtilityTest {

  @Test
  public void testGetGenericsParameterClass() {
    /*
    Logger.getLogger("").setLevel(Level.FINEST);
    for (Handler handler : Logger.getLogger("").getHandlers()) {
      handler.setLevel(Level.FINEST);
    }
    */
    Class<?> genericT;
    //
    genericT = TypeCastUtility.getGenericsParameterClass(LongArrayHolder.class, IHolder.class, 0);
    Assert.assertEquals(Long[].class, genericT);
    //
    try {
      TypeCastUtility.getGenericsParameterClass(X0.class, X0.class, 0);
      Assert.fail("no specific parametrized type defined, must fail");
    }
    catch (IllegalArgumentException t) {
      //nop
    }
    try {
      TypeCastUtility.getGenericsParameterClass(X1.class, X0.class, 0);
      Assert.fail("no specific parametrized type defined, must fail");
    }
    catch (IllegalArgumentException t) {
      //nop
    }
    genericT = TypeCastUtility.getGenericsParameterClass(X2.class, X0.class, 0);
    Assert.assertEquals(Data2.class, genericT);
    //
    genericT = TypeCastUtility.getGenericsParameterClass(Y3.class, Y0.class, 0);
    Assert.assertEquals(Data3.class, genericT);
    //
    genericT = TypeCastUtility.getGenericsParameterClass(Y3.class, Y0.class, 1);
    Assert.assertEquals(Flavor3.class, genericT);
  }

  @Test
  public void testCastBigDecimalToBoolean() throws Exception {
    //Bug 406875
    Assert.assertFalse(TypeCastUtility.castValue(BigDecimal.ZERO, boolean.class));
    Assert.assertTrue(TypeCastUtility.castValue(BigDecimal.ONE, boolean.class));

    Assert.assertFalse(TypeCastUtility.castValue(createCustomBigDecimal(0), boolean.class));
    Assert.assertTrue(TypeCastUtility.castValue(createCustomBigDecimal(1), boolean.class));
  }

  protected BigDecimal createCustomBigDecimal(long l) {
    int scale = 10;
    int precision = 30;
    RoundingMode roundingMode = RoundingMode.HALF_UP;
    MathContext mathContext = new MathContext(precision, roundingMode);
    BigDecimal customZero = new BigDecimal(l, mathContext).setScale(scale, roundingMode);
    return customZero;
  }

  static class Data0 {

  }

  static class Data1 extends Data0 {

  }

  static class Data2 extends Data1 {

  }

  static class Data3 extends Data2 {

  }

  static class Flavor0 {

  }

  static class Flavor1 extends Flavor0 {

  }

  static class Flavor2 extends Flavor1 {

  }

  static class Flavor3 extends Flavor2 {

  }

  static class X0<D0 extends Data0> {

  }

  static class X1<D1 extends Data1> extends X0<D1> {

  }

  static class X2 extends X0<Data2> {

  }

  static class Y0<D0 extends Data0, F0> {

  }

  static class Y1<F1 extends Flavor1, D1 extends Data1> extends Y0<D1, F1> {

  }

  static class Y2<F2 extends Flavor2, D2 extends Data2> extends Y1<F2, D2> {

  }

  static class Y3 extends Y2<Flavor3, Data3> {

  }
}
