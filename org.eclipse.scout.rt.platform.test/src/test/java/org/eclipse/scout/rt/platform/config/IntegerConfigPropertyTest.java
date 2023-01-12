/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.config;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Tests for {@link AbstractLongConfigProperty}
 */
public class IntegerConfigPropertyTest {

  @Test
  public void testDefaultForEmptyValue() {
    Integer propertyValue = CONFIG.getPropertyValue(SampleTestProperty.class);
    MatcherAssert.assertThat(propertyValue, is(42));

    propertyValue = CONFIG.getPropertyValue(PositiveSampleTestProperty.class);
    MatcherAssert.assertThat(propertyValue, is(42));
  }

  @Test
  public void testParse() {
    MatcherAssert.assertThat(new SampleTestProperty().parse(null), is(nullValue()));
    MatcherAssert.assertThat(new SampleTestProperty().parse("0"), is(0));
    MatcherAssert.assertThat(new SampleTestProperty().parse("123"), is(123));
    MatcherAssert.assertThat(new SampleTestProperty().parse("-123"), is(-123));

    MatcherAssert.assertThat(new PositiveSampleTestProperty().parse(null), is(nullValue()));
    MatcherAssert.assertThat(new PositiveSampleTestProperty().parse("0"), is(0));
    MatcherAssert.assertThat(new PositiveSampleTestProperty().parse("123"), is(123));
  }

  @Test(expected = NumberFormatException.class)
  public void testInvalidInteger_tooLong() {
    new SampleTestProperty().parse("9223372036854775807");
  }

  @Test(expected = PlatformException.class)
  public void testInvalidInteger_negative() {
    new PositiveSampleTestProperty().parse("-123");
  }

  @Bean
  public static class SampleTestProperty extends AbstractIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 42;
    }

    @Override
    public String description() {
      return null;
    }

    @Override
    public String getKey() {
      return "testKey";
    }
  }

  @Bean
  public static class PositiveSampleTestProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 42;
    }

    @Override
    public String description() {
      return null;
    }

    @Override
    public String getKey() {
      return "testKey.positive";
    }
  }
}
