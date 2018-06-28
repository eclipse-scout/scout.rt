#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.core;

import java.security.PermissionCollection;

import org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService;

public class AccessControlService extends AbstractAccessControlService<String> {

  @Override
  protected String getCurrentUserCacheKey() {
    return getUserIdOfCurrentUser();
  }

  @Override
  protected PermissionCollection execLoadPermissions(String userId) {
    return null;
  }
}
