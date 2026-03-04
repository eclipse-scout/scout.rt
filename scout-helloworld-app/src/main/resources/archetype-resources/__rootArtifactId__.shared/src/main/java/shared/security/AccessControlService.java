#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.shared.security;

import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.security.AbstractAccessControlService;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.security.IPermissionCollection;

/**
 * Default {@link IAccessControlService} implementation.
 * <p>
 * Replace this service at server side to load permission collection. It is <b>not</b> required to implement
 * {@link #execLoadPermissions(User)} at client side.
 *
 * @author ${userName}
 */
public class AccessControlService extends AbstractAccessControlService {

  @Override
  protected IPermissionCollection execLoadPermissions(User user) {
    return null;
  }
}
