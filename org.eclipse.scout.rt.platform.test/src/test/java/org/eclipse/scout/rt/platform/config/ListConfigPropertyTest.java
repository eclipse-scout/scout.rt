package org.eclipse.scout.rt.platform.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link ListConfigPropertyTest}</h3>
 */

public class ListConfigPropertyTest {

  @Test
  public void testListProperty() {
    Assert.assertEquals(Arrays.asList(1234, -44, 40, 4589, 5, 4, 574, 5, 4, 98, 2, 23), new TestIntegerListProperty().getValue("my-namespace"));
    Assert.assertEquals(Arrays.asList(22, 33), new TestIntegerListProperty().getValue("not-existing"));
    Assert.assertEquals(Arrays.asList(11111, 22222), new TestIntegerListProperty().getValue());
  }

  public static final class TestIntegerListProperty extends AbstractConfigProperty<List<Integer>, List<String>> {

    @Override
    public List<Integer> getValue(String namespace) {
      List<String> value = ConfigUtility.getPropertyList(getKey(), null, namespace);
      if (value == null) {
        return getDefaultValue();
      }
      return parse(value);
    }

    @Override
    protected List<Integer> parse(List<String> value) {
      List<Integer> result = new ArrayList<Integer>(value.size());
      for (String s : value) {
        result.add(Integer.valueOf(s));
      }
      return result;
    }

    @Override
    protected List<Integer> getDefaultValue() {
      return Arrays.asList(22, 33);
    }

    @Override
    public String getKey() {
      return "myListKey";
    }
  }
}
