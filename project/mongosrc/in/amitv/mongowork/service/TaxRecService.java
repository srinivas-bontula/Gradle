package in.amitv.mongowork.service;

import in.amitv.mongowork.dto.TaxRecDTO;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@WebService(name="taxRecService")
public interface TaxRecService {

	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/getrec")
	public Response getRec(String id);
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/getallrecs")
	public Response getAllRecs();

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/getrecs/{logType}/{pageNo}/{count}")
	public Response getPagedRecs(@PathParam("logType") String logType, @PathParam("pageNo") int pageNo, @PathParam("count") int count);
	
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/addrec")
	public Response addTaxRec(TaxRecDTO rec);

	
	
}
