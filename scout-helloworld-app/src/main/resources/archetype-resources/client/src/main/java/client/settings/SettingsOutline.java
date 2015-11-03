#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.client.settings;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.shared.TEXTS;

import ${groupId}.shared.Icons;

/**
 * <h3>{@link SettingsOutline}</h3>
 *
 * @author ${userName}
 */
@Order(3000)
public class SettingsOutline extends AbstractOutline {

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Settings");
  }
  
  @Override
  protected String getConfiguredIconId() {
    return Icons.Gear;
  }
}
