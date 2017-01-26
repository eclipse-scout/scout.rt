package org.eclipse.scout.rt.testing.shared;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.config.AbstractLongConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link TestingUtilityTest}</h3>
 *
 * @author oca
 */
public class TestingUtilityTest {

  @Test
  public void testMockConfigProperty() {
    Assert.assertEquals(1L, CONFIG.getPropertyValue(TestingUtilityLongConfigProperty.class).longValue());
    IBean<?> mockProperty = TestingUtility.mockConfigProperty(TestingUtilityLongConfigProperty.class, 2L);
    Assert.assertEquals(2L, CONFIG.getPropertyValue(TestingUtilityLongConfigProperty.class).longValue());
    TestingUtility.unregisterBean(mockProperty);
    Assert.assertEquals(1L, CONFIG.getPropertyValue(TestingUtilityLongConfigProperty.class).longValue());
  }

  public static class TestingUtilityLongConfigProperty extends AbstractLongConfigProperty {

    @Override
    public String getKey() {
      return getClass().getName();
    }

    @Override
    protected Long getDefaultValue() {
      return 1L;
    }

  }

}
