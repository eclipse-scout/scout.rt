package org.eclipse.scout.rt.server.servlet31;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.servlet.IServletHelperService;
import org.eclipse.scout.service.AbstractService;

/**
 * {@link IServletHelperService} for servlet 3.1
 */
public class ServletHelperService extends AbstractService implements IServletHelperService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServletHelperService.class);

  @Override
  public ServletInputStream createInputStream(final InputStream in) {
    return new ServletInputStream() {
      private ReadListener m_readListener;
      private boolean m_finished = false;

      @Override
      public int read() throws IOException {
        final int next = in.read();
        if (next == -1) {
          m_finished = true;
          if (m_readListener != null) {
            m_readListener.onAllDataRead();
          }
        }
        return next;
      }

      @Override
      public boolean isFinished() {
        return m_finished;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener readListener) {
        m_readListener = readListener;
        if (m_readListener != null) {
          try {
            m_readListener.onDataAvailable();
          }
          catch (IOException e) {
            LOG.error("Error reading stream", e);
            m_readListener.onError(e);
          }
        }
      }
    };
  }

}
