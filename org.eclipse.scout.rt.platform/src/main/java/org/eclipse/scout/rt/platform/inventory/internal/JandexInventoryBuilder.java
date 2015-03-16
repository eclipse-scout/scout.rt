package org.eclipse.scout.rt.platform.inventory.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

public class JandexInventoryBuilder extends AbstractInventoryBuilder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JandexInventoryBuilder.class);

  private final Indexer indexer = new Indexer();
  private Index m_index;

  @Override
  protected void handleClass(URL url) {
    try (InputStream in = url.openStream()) {
      indexer.index(in);
    }
    catch (IOException ex) {
      LOG.error("class at " + url, ex);
    }
  }

  @Override
  public void finish() {
    m_index = indexer.complete();
  }

  public Index getIndex() {
    return m_index;
  }
}
