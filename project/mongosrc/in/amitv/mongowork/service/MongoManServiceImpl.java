package in.amitv.mongowork.service;

import in.amitv.mongowork.dto.MongoMan;
import in.amitv.mongowork.repository.MongoManRepository;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

public class MongoManServiceImpl implements MongoManService {

	@Autowired
	MongoManRepository mongoManRepository;
	
	@Override
	public Response getMan(String empId) {
		
		return null;
	}

	@Override
	public Response getAllMen() {
		
		return null;
	}

	@Override
	public Response addMan(MongoMan man) {
		return Response.ok(mongoManRepository.insert(man)).build();
	}

}
