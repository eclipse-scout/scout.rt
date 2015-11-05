#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.client.search;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractSearchOutline;

import ${groupId}.shared.Icons;

/**
 * <h3>{@link SearchOutline}</h3>
 *
 * @author ${userName}
 */
@Order(2000)
public class SearchOutline extends AbstractSearchOutline {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SearchOutline.class);

  @Override
  protected void execSearch(final String query) {
    LOG.info("Search started");
    // TODO [${userName}]: Implement search
  }

  @Override
  protected String getConfiguredIconId() {
    return Icons.Search;
  }
}
