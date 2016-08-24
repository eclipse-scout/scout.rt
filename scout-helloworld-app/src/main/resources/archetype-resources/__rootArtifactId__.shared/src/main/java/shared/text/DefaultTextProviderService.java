#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.shared.text;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.common.text.AbstractDynamicNlsTextProviderService;

/**
 * <h3>{@link DefaultTextProviderService}</h3>
 *
 * @author ${userName}
 */
@Order(-2000)
public class DefaultTextProviderService extends AbstractDynamicNlsTextProviderService {
  @Override
  public String getDynamicNlsBaseName() {
    return "${package}.shared.texts.Texts";
  }
}
