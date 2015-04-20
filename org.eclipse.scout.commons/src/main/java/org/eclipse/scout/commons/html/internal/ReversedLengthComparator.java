package org.eclipse.scout.commons.html.internal;

import java.io.Serializable;
import java.util.Comparator;

public class ReversedLengthComparator implements Comparator<String>, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public int compare(String o1, String o2) {
    return o2.length() - o1.length();
  }

}
