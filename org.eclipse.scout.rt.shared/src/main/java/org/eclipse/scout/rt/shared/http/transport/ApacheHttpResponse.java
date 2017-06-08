package org.eclipse.scout.rt.shared.http.transport;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import com.google.api.client.http.LowLevelHttpResponse;

/**
 * <p>
 * Internal {@link LowLevelHttpResponse} for {@link ApacheHttpTransport}.
 * </p>
 *
 * @see ApacheHttpTransport
 */
public class ApacheHttpResponse extends LowLevelHttpResponse {

  private final HttpRequestBase m_request;
  private final HttpResponse m_response;

  public ApacheHttpResponse(HttpRequestBase request, HttpResponse response) {
    m_request = request;
    m_response = response;
  }

  @Override
  public InputStream getContent() throws IOException {
    HttpEntity entity = m_response.getEntity();
    return entity != null ? entity.getContent() : null;
  }

  @Override
  public String getContentEncoding() throws IOException {
    HttpEntity entity = m_response.getEntity();
    Header contentEncoding = entity != null ? entity.getContentEncoding() : null;
    return contentEncoding != null ? contentEncoding.getValue() : null;
  }

  @Override
  public long getContentLength() throws IOException {
    return m_response.getEntity().getContentLength();
  }

  @Override
  public String getContentType() throws IOException {
    HttpEntity entity = m_response.getEntity();
    Header contentType = entity != null ? entity.getContentType() : null;
    return contentType != null ? contentType.getValue() : null;
  }

  @Override
  public String getStatusLine() throws IOException {
    return m_response.getStatusLine().toString();
  }

  @Override
  public int getStatusCode() throws IOException {
    return m_response.getStatusLine().getStatusCode();
  }

  @Override
  public String getReasonPhrase() throws IOException {
    return m_response.getStatusLine().getReasonPhrase();
  }

  @Override
  public int getHeaderCount() throws IOException {
    return m_response.getAllHeaders().length;
  }

  @Override
  public String getHeaderName(int index) throws IOException {
    return m_response.getAllHeaders()[index].getName();
  }

  @Override
  public String getHeaderValue(int index) throws IOException {
    return m_response.getAllHeaders()[index].getValue();
  }

  @Override
  public void disconnect() throws IOException {
    m_request.abort();
  }

}
