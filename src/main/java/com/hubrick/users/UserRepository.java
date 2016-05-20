package com.hubrick.users;

import org.springframework.data.repository.CrudRepository;

/**
 * UserRepository implementation is provided by Spring data rest.
 */
public interface UserRepository extends CrudRepository<User, Long> {

}
