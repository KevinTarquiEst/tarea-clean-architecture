package org.cat.usercleanarchitecture.infraestructure.adapters.output;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntityRepository extends JpaRepository<UserEntity, Integer> {
}
