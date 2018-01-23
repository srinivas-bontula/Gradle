package in.amitv.mongowork.service;

import in.amitv.mongowork.dto.MongoMan;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@WebService(name="mongoManService", targetNamespace="http://in.amitv/mongoman")
public interface MongoManService {

	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/getmen")
	public Response getMan(String empId);
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/getallmen")
	public Response getAllMen();
	
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/addman")
	public Response addMan(MongoMan man);

}
