/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;

@TypeName("TestEnum")
public class TestEnumDo extends DoEntity {

  public DoValue<FixtureEnum1> fixtureEnum1() {
    return doValue("fixtureEnum1");
  }

  public DoValue<FixtureEnum2> fixtureEnum2() {
    return doValue("fixtureEnum2");
  }

  public DoValue<FixtureEnum3> fixtureEnum3() {
    return doValue("fixtureEnum3");
  }

  public DoValue<Enum> anyEnum() {
    return doValue("anyEnum");
  }

  public DoValue<IEnum> anyIEnum() {
    return doValue("anyIEnum");
  }

  public enum FixtureEnum1 {
    ONE,
    TWO
  }

  public enum FixtureEnum2 {
    ONE {
      @Override
      public String toString() {
        return "one-to-string";
      }
    },
    TWO {
      @Override
      public String toString() {
        return "two-to-string";
      }
    }
  }

  public enum FixtureEnum3 implements IEnum {
    ONE {
      @Override
      public String stringValue() {
        return "oneStringValue";
      }

      @Override
      public String toString() {
        return "one-to-string";
      }
    },
    TWO {
      @Override
      public String stringValue() {
        return "twoStringValue";
      }

      @Override
      public String toString() {
        return "two-to-string";
      }
    }
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEnumDo withFixtureEnum1(FixtureEnum1 fixtureEnum1) {
    fixtureEnum1().set(fixtureEnum1);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureEnum1 getFixtureEnum1() {
    return fixtureEnum1().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEnumDo withFixtureEnum2(FixtureEnum2 fixtureEnum2) {
    fixtureEnum2().set(fixtureEnum2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureEnum2 getFixtureEnum2() {
    return fixtureEnum2().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEnumDo withFixtureEnum3(FixtureEnum3 fixtureEnum3) {
    fixtureEnum3().set(fixtureEnum3);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureEnum3 getFixtureEnum3() {
    return fixtureEnum3().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEnumDo withAnyEnum(Enum anyEnum) {
    anyEnum().set(anyEnum);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Enum getAnyEnum() {
    return anyEnum().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEnumDo withAnyIEnum(IEnum anyIEnum) {
    anyIEnum().set(anyIEnum);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IEnum getAnyIEnum() {
    return anyIEnum().get();
  }
}
