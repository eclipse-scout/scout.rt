package org.eclipse.scout.commons.html.internal;

import java.util.Comparator;

public class ReversedLengthComparator implements Comparator<String> {

  @Override
  public int compare(String o1, String o2) {
    return o2.length() - o1.length();
  }

}
