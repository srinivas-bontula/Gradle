package in.amitv.mongowork.service;

import in.amitv.mongowork.dto.TaxRecDTO;
import in.amitv.mongowork.repository.TaxRecRepository;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class TaxRecServiceImpl implements TaxRecService {

	@Autowired
	TaxRecRepository taxRecRepository;
	
	@Override
	public Response getRec(String id) {
		return null;
	}

	@Override
	public Response getAllRecs() {
		return Response.ok(taxRecRepository.findAll()).build();
	}

	@Override
	public Response addTaxRec(TaxRecDTO rec) {
		return Response.ok(taxRecRepository.insert(rec)).build();
	}

	@Override
	public Response getPagedRecs(String logType, int pageNo, int count) {
		Page<TaxRecDTO> pagedtos = taxRecRepository.findAll(new PageRequest(pageNo,count,new Sort(Sort.Direction.DESC,"req.ts")));
		return Response.ok(pagedtos).build();
	}

}
