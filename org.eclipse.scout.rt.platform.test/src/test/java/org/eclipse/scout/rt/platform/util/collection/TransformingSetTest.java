package org.eclipse.scout.rt.platform.util.collection;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link TransformingSetTest}</h3>
 */
public class TransformingSetTest {

  @Test
  public void testTransformingList() {
    Set<String> stringList = new LinkedHashSet<>(Arrays.asList("1234", "5678", "90", "11"));
    Set<Integer> intList = new TransformingSet<>(stringList, new ITransformer<Integer, String>() {

      @Override
      public String transform(Integer x) {
        return x.toString();
      }

      @Override
      public Integer revert(String y) {
        return Integer.parseInt(y);
      }
    });

    intList.add(1);
    intList.addAll(Arrays.asList(99, 66));

    Assert.assertTrue(intList.contains(11));
    Assert.assertFalse(intList.contains(55555));
    Assert.assertFalse(intList.isEmpty());

    intList.remove(90);

    Iterator<Integer> listIterator = intList.iterator();
    listIterator.next();
    listIterator.remove();
    listIterator.next();

    Assert.assertEquals(5, intList.size());
    Assert.assertArrayEquals(new Object[]{5678, 11, 1, 99, 66}, intList.toArray());
    Assert.assertArrayEquals(new String[]{"5678", "11", "1", "99", "66"}, stringList.toArray(new String[stringList.size()]));

    intList.clear();
    Assert.assertEquals(0, stringList.size());
  }
}
