package org.eclipse.scout.rt.platform.config;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.scout.rt.platform.Bean;
import org.junit.Test;

/**
 * Tests for {@link AbsteactLongConfigProperty}
 */
public class LongConfigPropertyTest {

  @Test
  public void testDefaultForEmptyValue() {
    Long propertyValue = CONFIG.getPropertyValue(SampleTestProperty.class);
    assertThat(propertyValue, is(42L));
  }

  @Test
  public void testParse() {
    assertThat(new SampleTestProperty().parse(null), is(nullValue()));
    assertThat(new SampleTestProperty().parse("0"), is(0L));
  }

  @Bean
  public static class SampleTestProperty extends AbstractLongConfigProperty {

    @Override
    protected Long getDefaultValue() {
      return 42L;
    }

    @Override
    public String getKey() {
      return "testKey";
    }
  }

}
