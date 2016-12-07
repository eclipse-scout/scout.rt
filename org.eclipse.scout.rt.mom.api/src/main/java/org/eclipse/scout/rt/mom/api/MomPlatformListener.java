package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.filter.IFilter;

/**
 * {@link IPlatformListener} to shutdown all MOM transports upon platform shutdown.
 */
@Order(IMom.DESTROY_ORDER)
public class MomPlatformListener implements IPlatformListener {

  /**
   * Filters {@link IMom}s that are not {@link IMomTransport} (namely the {@link IMomImplementor}s).
   */
  protected static final IFilter<IMom> MOM_TRANSPORT_FILTER = new IFilter<IMom>() {
    @Override
    public boolean accept(IMom element) {
      return (element instanceof IMomTransport);
    }
  };

  @Override
  public void stateChanged(final PlatformEvent event) {
    if (event.getState() == State.PlatformStopping) {
      for (IMom transport : BEANS.all(IMom.class, MOM_TRANSPORT_FILTER)) {
        transport.destroy();
      }
    }
  }
}
