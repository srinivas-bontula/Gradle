package in.amitv.mongowork.repository;

import in.amitv.mongowork.dto.MongoMan;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoManRepository extends MongoRepository<MongoMan, String> {
	public MongoMan findByEmpId(String empId);
	
}
