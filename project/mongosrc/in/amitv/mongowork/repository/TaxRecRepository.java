package in.amitv.mongowork.repository;

import java.util.List;

import in.amitv.mongowork.dto.TaxRecDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaxRecRepository extends MongoRepository<TaxRecDTO, String> {
	@Override
	public List<TaxRecDTO> findAll();
	
	@Override
	public Page<TaxRecDTO> findAll(Pageable pageable);
}
