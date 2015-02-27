package org.eclipse.scout.rt.server;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.AbstractDynamicHashMap;

class VirtualSessionCache extends AbstractDynamicHashMap<String, IServerSession> {
  private static final long serialVersionUID = 1L;

  private long m_sessionTimeoutMillis = TimeUnit.MINUTES.toMillis(5);
  private long m_nextMaintenance = TimeUnit.MINUTES.toMillis(0);

  public long getSessionTimeoutMillis() {
    return m_sessionTimeoutMillis;
  }

  public void setSessionTimeoutMillis(long sessionTimeoutMillis) {
    m_sessionTimeoutMillis = sessionTimeoutMillis;
  }

  @Override
  protected void beforeAccessToInternalMap() {
    if (System.currentTimeMillis() > m_nextMaintenance) {
      validateInternalMap();
      m_nextMaintenance = System.currentTimeMillis() + m_sessionTimeoutMillis / 2;
    }
  }

  @Override
  protected DynamicEntry<IServerSession> createDynamicEntry(IServerSession value) {
    return new VirtualSessionEntry(value, getSessionTimeoutMillis());
  }

  @Override
  protected boolean isEntryValid(DynamicEntry e) {
    VirtualSessionEntry v = (VirtualSessionEntry) e;
    return v.getTimestamp() + v.getInactivityTimeout() >= System.currentTimeMillis();
  }

  static class VirtualSessionEntry extends DynamicEntry<IServerSession> {
    private long m_time;
    private long m_timeout;

    public VirtualSessionEntry(IServerSession value, long timeout) {
      super(value);
      m_timeout = timeout;
      m_time = System.currentTimeMillis();
    }

    public long getTimestamp() {
      return m_time;
    }

    public long getInactivityTimeout() {
      return m_timeout;
    }

    @Override
    public void touch() {
      m_time = System.currentTimeMillis();
    }

  }
}
