package org.eclipse.scout.rt.ui.html;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects JSON responses and their corresponding <i>request sequence number</i> and <i>response sequence number</i>
 * until they are acknowledged by the client. A maximum of 10 responses is stored in the history.
 * <p>
 * This class is thread-safe.
 */
@Bean
public class ResponseHistory {

  private static final Logger LOG = LoggerFactory.getLogger(ResponseHistory.class);
  private static final int MAX_RESPONSE_HISTORY_SIZE = 10;

  private final Object m_mutex = new Object();

  private final Map<Long, Long> m_requestToResponseMap = new HashMap<>(); // RequestSequenceNo -> ResponseSequenceNo
  private final Map<Long, Long> m_responseToRequestMap = new HashMap<>(); // ResponseSequenceNo -> RequestSequenceNo
  private final SortedMap<Long, JSONObject> m_responses = new TreeMap<>(); // ResponseSequenceNo -> Response

  private UiSession m_uiSession;

  public UiSession getUiSession() {
    return m_uiSession;
  }

  public ResponseHistory withUiSession(UiSession uiSession) {
    m_uiSession = uiSession;
    return this;
  }

  protected String getUiSessionId() {
    return (m_uiSession == null ? null : m_uiSession.getUiSessionId());
  }

  /**
   * Stores the given <u>response</u> in the history, along with the <u>response</u> sequence number. If a
   * <u>request</u> sequence number is provided, a mapping between those sequence numbers is stored as well. This allows
   * to retrieve the response by request sequence number.
   * <p>
   * If the history is already full, the oldest entry is discarded.
   *
   * @param responseSequenceNo
   *          The sequence number of the <u>response</u> to remember <i>(mandatory)</i>
   * @param response
   *          The <u>response</u> to remember <i>(mandatory)</i>
   * @param requestSequenceNo
   *          The sequence number of the <u>request</u> that caused the response <i>(optional)</i>
   * @throws AssertionException
   *           if mandatory arguments are <code>null</code>
   */
  public void registerResponse(Long responseSequenceNo, JSONObject response, Long requestSequenceNo) {
    Assertions.assertNotNull(responseSequenceNo);
    Assertions.assertNotNull(response);

    synchronized (m_mutex) {
      Assertions.assertFalse(m_responses.containsKey(responseSequenceNo), "ResponseSequenceNo #{} already registered", responseSequenceNo);
      if (requestSequenceNo != null) { // optional
        Assertions.assertFalse(m_requestToResponseMap.containsKey(requestSequenceNo), "RequestSequenceNo #{} already registered", requestSequenceNo);
        Assertions.assertFalse(m_responseToRequestMap.containsKey(responseSequenceNo), "ResponseSequenceNo #{} already registered", responseSequenceNo);
      }

      m_responses.put(responseSequenceNo, response);
      if (requestSequenceNo != null) { // optional
        m_requestToResponseMap.put(requestSequenceNo, responseSequenceNo);
        m_responseToRequestMap.put(responseSequenceNo, requestSequenceNo);
      }

      if (m_responses.size() > MAX_RESPONSE_HISTORY_SIZE) {
        // Remove oldest entry to free up memory (protection against malicious clients that send no or wrong #ACKs)
        Long oldestSeqNo = m_responses.firstKey();
        LOG.warn("Max. response history size exceeded for UI session {}, dropping oldest response #{}", getUiSessionId(), oldestSeqNo);
        m_responses.remove(oldestSeqNo);
      }
      LOG.debug("Added response #{} to history {} for UI session {}", responseSequenceNo, m_responses.keySet(), getUiSessionId());
    }
  }

  /**
   * Confirms that the response with the given <i>response sequence number</i> has been successfully processed by the
   * client. The response is removed from the history. All responses that are older (i.e. have a lower response sequence
   * number) and the "response - request" mappings are automatically removed as well.
   *
   * @throws AssertionException
   *           if the argument is <code>null</code>
   */
  public void confirmResponseProcessed(Long confirmedResponseSequenceNo) {
    Assertions.assertNotNull(confirmedResponseSequenceNo);

    synchronized (m_mutex) {
      int removeCount = 0;
      for (Iterator<Long> it = m_responses.keySet().iterator(); it.hasNext();) {
        Long responseSequenceNo = it.next();
        if (responseSequenceNo <= confirmedResponseSequenceNo) {
          Long requestSequenceNo = m_responseToRequestMap.get(responseSequenceNo);
          m_requestToResponseMap.remove(requestSequenceNo);
          m_responseToRequestMap.remove(responseSequenceNo);
          it.remove();
          removeCount++;
        }
      }
      LOG.debug("Cleaned up response history (-{}). New content: {} [#ACK={}, uiSessionId={}]", removeCount, m_responses.keySet(), confirmedResponseSequenceNo, getUiSessionId());
    }
  }

  /**
   * Combines all responses currently contained in the history into one new JSON object.
   */
  public JSONObject toSyncResponse() {
    synchronized (m_mutex) {
      LOG.debug("Synchronize response queue {} for UI session {}", m_responses.keySet(), getUiSessionId());
      if (m_responses.isEmpty()) {
        return null;
      }

      Long lastSentSequenceNo = m_responses.lastKey();
      JSONObject combinedAdapterData = new JSONObject();
      JSONArray combinedEvents = new JSONArray();
      for (JSONObject response : m_responses.values()) {
        // combine adapterData
        JSONObject adapterData = response.optJSONObject(JsonResponse.PROP_ADAPTER_DATA);
        if (adapterData != null) {
          for (String key : adapterData.keySet()) {
            combinedAdapterData.put(key, adapterData.get(key));
          }
        }

        // combine events
        JSONArray events = response.optJSONArray(JsonResponse.PROP_EVENTS);
        if (events != null) {
          for (int i = 0; i < events.length(); i++) {
            combinedEvents.put(events.get(i));
          }
        }
      }

      JSONObject combinedResponse = new JSONObject();
      combinedResponse.put(JsonResponse.PROP_SEQUENCE_NO, lastSentSequenceNo);
      combinedResponse.put(JsonResponse.PROP_COMBINED, true);
      combinedResponse.put(JsonResponse.PROP_ADAPTER_DATA, (combinedAdapterData.length() == 0 ? null : combinedAdapterData));
      combinedResponse.put(JsonResponse.PROP_EVENTS, (combinedEvents.length() == 0 ? null : combinedEvents));
      return combinedResponse;
    }
  }

  /**
   * @return the <i>response sequence number</i> that corresponds to the given <i>request sequence number</i> (or
   *         <code>null</code> if no such mapping exists in the history)
   */
  public Long getResponseSequenceNo(Long requestSequenceNo) {
    if (requestSequenceNo == null) {
      return null;
    }
    synchronized (m_mutex) {
      return m_requestToResponseMap.get(requestSequenceNo);
    }
  }

  /**
   * @return the <i>request sequence number</i> that corresponds to the given <i>response sequence number</i> (or
   *         <code>null</code> if no such mapping exists in the history)
   */
  public Long getRequestSequenceNo(Long responseSequenceNo) {
    if (responseSequenceNo == null) {
      return null;
    }
    synchronized (m_mutex) {
      return m_responseToRequestMap.get(responseSequenceNo);
    }
  }

  /**
   * @return the response with the given <i>response sequence number</i> (or <code>null</code> if no response with this
   *         sequence number exists in the history)
   */
  public JSONObject getResponse(Long responseSequenceNo) {
    if (responseSequenceNo == null) {
      return null;
    }
    synchronized (m_mutex) {
      return m_responses.get(responseSequenceNo);
    }
  }

  /**
   * @return the response that was sent as answer for the request with the given <i>request sequence number</i> (or
   *         <code>null</code> if no response for this request exists in the history)
   */
  public JSONObject getResponseForRequest(Long requestSequenceNo) {
    synchronized (m_mutex) {
      return getResponse(getResponseSequenceNo(requestSequenceNo));
    }
  }

  /**
   * @return the number of responses in the history
   */
  public int size() {
    synchronized (m_mutex) {
      return m_responses.size();
    }
  }

  @Override
  public String toString() {
    return "[" + CollectionUtility.format(m_responses.keySet()) + "]";
  }
}
