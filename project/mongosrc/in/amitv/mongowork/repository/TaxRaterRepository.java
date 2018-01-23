package in.amitv.mongowork.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.saba.taxpro.rater.ProTaxRater;

public interface TaxRaterRepository extends MongoRepository<ProTaxRater, String> {
	public ProTaxRater findByName(String name);
}
