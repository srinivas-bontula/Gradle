package com.saba.taxpro;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.saba.tax.common.TaxExemptInfoRequestDetail;
import com.saba.tax.common.TaxInfoRequestDetail;
import com.saba.taxpro.rater.ProTaxRaterWrapper;


@Path("/")
@WebService(name="proTaxService", targetNamespace="http://www.saba.com/ecommerce/tax")
public interface ProTaxService {

	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/gettax")
	public Response getTax(TaxInfoRequestDetail request);
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/gettaxconfig")
	public Response getTaxConfig();
	
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/settaxconfig")
	public Response setTaxConfig(ProTaxRaterWrapper wrapper);
	
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/gettaxexempt")
	public Response getTaxExemptionState(TaxExemptInfoRequestDetail request);

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/sysconfig")
	public Response getSysConfig();
	
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/sysconfig/{config}")
	public Response setSysConfig(String value,@PathParam("config") String config);
	
	
}
