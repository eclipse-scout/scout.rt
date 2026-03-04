#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.core;

import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.security.AbstractAccessControlService;
import org.eclipse.scout.rt.security.IPermissionCollection;

public class AccessControlService extends AbstractAccessControlService {

  @Override
  protected IPermissionCollection execLoadPermissions(User user) {
    return null;
  }
}
