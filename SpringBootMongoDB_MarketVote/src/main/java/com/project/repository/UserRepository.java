package com.project.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.model.User;

public interface UserRepository extends MongoRepository<User, String>{

	public User findOneByName(String name);
}
