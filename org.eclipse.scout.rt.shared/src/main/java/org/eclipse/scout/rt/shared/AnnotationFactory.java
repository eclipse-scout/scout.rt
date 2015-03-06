package org.eclipse.scout.rt.shared;

public final class AnnotationFactory {
  private AnnotationFactory() {
  }

  public static TunnelToServer createTunnelToServer() {
    return AnnotationFactory.Dummy.class.getAnnotation(TunnelToServer.class);
  }

  @TunnelToServer
  private static class Dummy {
  }
}
