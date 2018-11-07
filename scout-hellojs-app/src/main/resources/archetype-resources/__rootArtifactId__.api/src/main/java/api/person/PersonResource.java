#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.api.person;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.IRestResource;

import ${package}.core.person.PersonService;
import ${package}.data.person.PersonDo;
import ${package}.data.person.PersonResponse;
import ${package}.data.person.PersonRestrictionDo;

@Path("persons")
public class PersonResource implements IRestResource {

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public PersonResponse getById(@PathParam("id") String id) {
    return BEANS.get(PersonService.class)
        .getById(id)
        .map(item -> BEANS.get(PersonResponse.class).withItem(item))
        .orElseGet(PersonResponse::new);
  }

  @POST
  @Path("list")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PersonResponse list(PersonRestrictionDo restrictions) {
    return BEANS.get(PersonResponse.class)
        .withItems(BEANS.get(PersonService.class).list(restrictions));
  }

  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PersonResponse store(@PathParam("id") String id, PersonDo person) {
    return BEANS.get(PersonResponse.class)
        .withItem(BEANS.get(PersonService.class).store(id, person));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PersonResponse create(PersonDo person) {
    return BEANS.get(PersonResponse.class)
        .withItem(BEANS.get(PersonService.class).create(person));
  }

  @DELETE
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public void remove(@PathParam("id") String id) {
    BEANS.get(PersonService.class).remove(id);
  }
}
