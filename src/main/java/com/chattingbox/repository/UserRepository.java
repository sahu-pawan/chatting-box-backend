package com.chattingbox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.chattingbox.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    // User findByUsername(String username); // pahle
    User findByEmail(String email);
}
