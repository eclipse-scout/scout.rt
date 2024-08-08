/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.meta;

import java.util.function.Function;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.scout.rt.api.data.meta.MetaVersionInfoDo;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationNameProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationVersionProperty;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.JarManifestHelper;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.rest.IRestResource;
import org.eclipse.scout.rt.rest.doc.ApiDocDescription;
import org.eclipse.scout.rt.rest.doc.ApiDocGenerator;
import org.eclipse.scout.rt.rest.doc.ApiDocIgnore;

/**
 * Provides meta information about the API.
 */
@Path("meta")
@ApiDocDescription(text = "Provides meta information about the API.")
public class MetaResource implements IRestResource {

  @GET
  @Path("version")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiDocDescription(text = "Returns the API version and application meta data in JSON format.")
  public MetaVersionInfoDo getMetaVersionInfo() {
    var apiVersion = BEANS.get(IApiVersion.class);
    return BEANS.get(MetaVersionInfoDo.class)
        .withApiVersion(apiVersion.getVersion())
        .withApplicationName(CONFIG.getPropertyValue(ApplicationNameProperty.class))
        .withApplicationVersion(CONFIG.getPropertyValue(ApplicationVersionProperty.class))
        .withBuildDate(BEANS.get(JarManifestHelper.class).getBuildDateAttribute(apiVersion.getClass()));
  }

  @GET
  @Path("version")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiDocDescription(text = "Returns the API version in text format. If 'verbose' is true, the application name and version are returned as well.")
  public String getApiVersion(@QueryParam("verbose") boolean verbose) {
    if (verbose) {
      return StringUtility.join("",
          "API version: " + BEANS.get(IApiVersion.class).getVersion(),
          StringUtility.box(" (", StringUtility.join(" ",
              CONFIG.getPropertyValue(ApplicationNameProperty.class),
              CONFIG.getPropertyValue(ApplicationVersionProperty.class)), ")"));
    }
    return BEANS.get(IApiVersion.class).getVersion();
  }

  @GET
  @Path("ping")
  @Produces(MediaType.TEXT_PLAIN)
  public String ping() {
    return "Hello";
  }

  @GET
  @Path("echo{path: $|/.*}")
  @Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
  public String echo(@Context Request request, @Context UriInfo uriInfo) {
    Function<MultivaluedMap<String, String>, String> mapToString = (map) -> {
      if (map.isEmpty()) {
        return "(empty)";
      }
      return map.entrySet().stream()
          .map(entry -> entry.getKey() + " = " + CollectionUtility.format(entry.getValue()))
          .collect(Collectors.joining("\n"));
    };
    String line = StringUtility.repeat("=", 50) + "\n";
    return line + "Path\n" + line + uriInfo.getPath(false) + "\n\n" +
        line + "Path (decoded)\n" + line + uriInfo.getPath() + "\n\n" +
        line + "Query parameters\n" + line + mapToString.apply(uriInfo.getQueryParameters(false)) + "\n\n" +
        line + "Query parameters (decoded)\n" + line + mapToString.apply(uriInfo.getQueryParameters()) + "\n\n";
  }

  @GET
  @Path("whoami")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiDocDescription(text = "Returns the principal name.")
  public String whoAmI() {
    Subject subject = RunContext.CURRENT.get().getSubject();
    return ObjectUtility.nvl(SecurityUtility.getPrincipalNames(subject), "(nobody)");
  }

  @GET
  @Path("locale")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiDocDescription(text = "Returns the current locale.")
  public String locale() {
    return NlsLocale.get().toLanguageTag();
  }

  @GET
  @Path("doc")
  @ApiDocIgnore
  public Response getDocAsHtml(@QueryParam(ApiDocGenerator.STATIC_RESOURCE_PARAM) String staticResource, @QueryParam(ApiDocGenerator.SCOPE_PARAM) String scope) {
    return BEANS.get(ApiDocGenerator.class).getWebContent(staticResource, scope);
  }

  @GET
  @Path("doc/csv")
  @ApiDocIgnore
  @Produces(MediaType.TEXT_PLAIN)
  public Response getDocAsText(@QueryParam(ApiDocGenerator.SCOPE_PARAM) String scope) {
    return BEANS.get(ApiDocGenerator.class).getTextContent(scope);
  }

  @GET
  @Path("doc/json")
  @ApiDocIgnore
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDocAsJson(@QueryParam(ApiDocGenerator.SCOPE_PARAM) String scope) {
    return BEANS.get(ApiDocGenerator.class).getJsonContent(scope);
  }
}
