package org.eclipse.scout.rt.platform.cdi;

public interface Instance<T> extends Iterable<T> {

  /**
   * <p>
   * Determines if there is more than one bean that matches the required type and qualifiers and is eligible for
   * injection into the class into which the parent <tt>Instance</tt> was injected.
   * </p>
   *
   * @return <tt>true</tt> if there is more than one bean that matches the required type and qualifiers and is eligible
   *         for
   *         injection into the class into which the parent <tt>Instance</tt> was injected, or <tt>false</tt> otherwise.
   */
  public boolean isAmbiguous();

}
