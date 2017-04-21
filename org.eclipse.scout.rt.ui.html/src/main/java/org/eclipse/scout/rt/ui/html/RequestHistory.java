package org.eclipse.scout.rt.ui.html;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of processed <i>request sequence numbers</i>. Normally, only the last processed sequence number must be
 * remembered. However, there are cases where requests with a higher sequence number are processed first (e.g. a user
 * request may "overtake" a pending poll request). Temporarily missing sequence numbers are remembered in a separate
 * list that is cleaned up again when the request is finally processed.
 * <p>
 * This class is thread-safe.
 */
@Bean
public class RequestHistory {

  private static final Logger LOG = LoggerFactory.getLogger(RequestHistory.class);
  private static final int MAX_REQUEST_HISTORY_SIZE = 100;

  private final Object m_mutex = new Object();

  private Long m_lastProcessedRequestSequenceNo = -1L;
  private final SortedSet<Long> m_missingRequestSequenceNos = new TreeSet<>();

  private UiSession m_uiSession;

  public UiSession getUiSession() {
    return m_uiSession;
  }

  public RequestHistory withUiSession(UiSession uiSession) {
    m_uiSession = uiSession;
    return this;
  }

  protected String getUiSessionId() {
    return (m_uiSession == null ? null : m_uiSession.getUiSessionId());
  }

  /**
   * Marks the given <i>request sequence number</i> as "processed", i.e. {@link #isRequestProcessed(Long)} will return
   * <code>true</code>.
   */
  public void setRequestProcessed(Long requestSequenceNo) {
    Assertions.assertNotNull(requestSequenceNo);
    synchronized (m_mutex) {
      // If the processed request's sequence number is smaller than the "last processed request
      // sequence number", it was previously missing -> remove it from the set
      if (requestSequenceNo < m_lastProcessedRequestSequenceNo) {
        LOG.debug("Cleanup previously missing request sequence number #{}", requestSequenceNo);
        m_missingRequestSequenceNos.remove(requestSequenceNo);
      }
      else {
        // If the processed request's sequence number is larger than the next expected sequence
        // number, remember the missing sequence numbers in between
        for (Long missingSequenceNo = m_lastProcessedRequestSequenceNo + 1; missingSequenceNo < requestSequenceNo; missingSequenceNo++) {
          LOG.debug("Remember missing request sequence number #{}", missingSequenceNo);
          m_missingRequestSequenceNos.add(missingSequenceNo);
        }
        // Don't wait for missing sequence numbers forever
        while (m_missingRequestSequenceNos.size() > MAX_REQUEST_HISTORY_SIZE) {
          LOG.warn("Max. request history size exceeded for UI session {}, dropping oldest request #{}. Current state of {}", getUiSessionId(), m_missingRequestSequenceNos.first(), this.toString());
          m_missingRequestSequenceNos.remove(m_missingRequestSequenceNos.first());
        }
        m_lastProcessedRequestSequenceNo = requestSequenceNo;
      }
    }
  }

  /**
   * @return <code>true</code> if the given <i>request sequence number</i> was already marked as "processed",
   *         <code>false</code> otherwise.
   */
  public boolean isRequestProcessed(Long requestSequenceNo) {
    Assertions.assertNotNull(requestSequenceNo);
    synchronized (m_mutex) {
      if (m_missingRequestSequenceNos.contains(requestSequenceNo)) {
        return false;
      }
      return requestSequenceNo <= m_lastProcessedRequestSequenceNo;
    }
  }

  /**
   * @return the "last processed sequence number", i.e. the highest <i>request sequence number</i> that was marked as
   *         "processed".
   */
  public Long getLastProcessedSequenceNo() {
    return m_lastProcessedRequestSequenceNo;
  }

  /**
   * @return a copy of the set of missing <i>request sequence numbers</i>, i.e. sequence numbers that are lower than
   *         {@link #getLastProcessedSequenceNo()} but have not been marked as "processed".
   */
  public SortedSet<Long> getMissingRequestSequenceNos() {
    return new TreeSet<>(m_missingRequestSequenceNos);
  }

  @Override
  public String toString() {
    return "RequestHistory: lastProcessedRequestSequenceNo=" + m_lastProcessedRequestSequenceNo + ", missingRequestSequenceNos=[" + CollectionUtility.format(m_missingRequestSequenceNos) + "]";
  }
}
