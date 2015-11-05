#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.server.permission;

import java.security.AllPermission;
import java.security.Permissions;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.server.services.common.security.AbstractAccessControlService;
import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;

/**
 * <h3>{@link AccessControlService}</h3>
 *
 * @author ${userName}
 */
@Order(-1)
public class AccessControlService extends AbstractAccessControlService {
  @Override
  protected Permissions execLoadPermissions() {
    Permissions permissions = new Permissions();
    permissions.add(new RemoteServiceAccessPermission("*.shared.*", "*"));

    //TODO [${userName}]: Fill access control service
    permissions.add(new AllPermission());
    return permissions;
  }
}
