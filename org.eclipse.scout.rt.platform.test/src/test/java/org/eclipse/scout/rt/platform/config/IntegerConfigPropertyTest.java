/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.junit.Test;

/**
 * Tests for {@link AbsteactLongConfigProperty}
 */
public class IntegerConfigPropertyTest {

  @Test
  public void testDefaultForEmptyValue() {
    Integer propertyValue = CONFIG.getPropertyValue(SampleTestProperty.class);
    assertThat(propertyValue, is(42));

    propertyValue = CONFIG.getPropertyValue(PositiveSampleTestProperty.class);
    assertThat(propertyValue, is(42));
  }

  @Test
  public void testParse() {
    assertThat(new SampleTestProperty().parse(null), is(nullValue()));
    assertThat(new SampleTestProperty().parse("0"), is(0));
    assertThat(new SampleTestProperty().parse("123"), is(123));
    assertThat(new SampleTestProperty().parse("-123"), is(-123));

    assertThat(new PositiveSampleTestProperty().parse(null), is(nullValue()));
    assertThat(new PositiveSampleTestProperty().parse("0"), is(0));
    assertThat(new PositiveSampleTestProperty().parse("123"), is(123));
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
    protected Integer getDefaultValue() {
      return 42;
    }

    @Override
    public String getKey() {
      return "testKey";
    }
  }

  @Bean
  public static class PositiveSampleTestProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    protected Integer getDefaultValue() {
      return 42;
    }

    @Override
    public String getKey() {
      return "testKey.positive";
    }
  }
}
