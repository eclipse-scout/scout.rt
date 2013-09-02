package org.eclipse.scout.rt.ui.swing;

import org.eclipse.scout.rt.ui.swing.extension.ISwingApplicationExtension;

public class RankedExtension implements Comparable<RankedExtension> {

  public int ranking;

  public ISwingApplicationExtension extension;

  public RankedExtension(int ranking, ISwingApplicationExtension extension) {
    this.ranking = ranking;
    this.extension = extension;
  }

  @Override
  public int compareTo(RankedExtension o) {
    return o.ranking - ranking;
  }

}
