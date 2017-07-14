package org.eclipse.scout.rt.shared.http.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.StreamingContent;

/**
 * <p>
 * Internal {@link LowLevelHttpRequest} for {@link ApacheHttpTransport}.
 * </p>
 *
 * @see ApacheHttpTransport
 */
public class ApacheHttpRequest extends LowLevelHttpRequest {

  private final HttpClient m_httpClient;

  private final HttpRequestBase m_request;

  public ApacheHttpRequest(HttpClient httpClient, HttpRequestBase request) {
    m_httpClient = httpClient;
    m_request = request;
  }

  @Override
  public void addHeader(String name, String value) throws IOException {
    m_request.addHeader(name, value);
  }

  @Override
  public void setTimeout(int connectTimeout, int readTimeout) throws IOException {
    super.setTimeout(connectTimeout, readTimeout);
    RequestConfig config = m_request.getConfig();
    Builder configBuilder = config != null ? RequestConfig.copy(config) : RequestConfig.custom();
    configBuilder.setConnectTimeout(connectTimeout);
    configBuilder.setSocketTimeout(readTimeout);
    m_request.setConfig(configBuilder.build());

  }

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    final StreamingContent streamingContent = getStreamingContent();
    if (streamingContent != null) {
      if (!(m_request instanceof HttpEntityEnclosingRequest)) {
        throw new ProcessingException("This request {} does not support content.", m_request);
      }

      AbstractHttpEntity entity = new AbstractHttpEntity() {

        @Override
        public void writeTo(OutputStream outstream) throws IOException {
          streamingContent.writeTo(outstream);
        }

        @Override
        public boolean isStreaming() {
          return true;
        }

        @Override
        public boolean isRepeatable() {
          if (streamingContent instanceof HttpContent) {
            return ((HttpContent) streamingContent).retrySupported();
          }
          return false;
        }

        @Override
        public long getContentLength() {
          return ApacheHttpRequest.this.getContentLength();
        }

        @Override
        public InputStream getContent() throws IOException {
          throw new UnsupportedOperationException("Streaming entity cannot be represented as an input stream.");
        }
      };
      ((HttpEntityEnclosingRequest) m_request).setEntity(entity);
      entity.setContentEncoding(getContentEncoding());
      entity.setContentType(getContentType());
    }
    return createResponseInternal();
  }

  protected ApacheHttpResponse createResponseInternal() throws IOException {
    return new ApacheHttpResponse(m_request, m_httpClient.execute(m_request));
  }

}
