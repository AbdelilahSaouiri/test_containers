package net.ensah.demo.dao;

import net.ensah.demo.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByFirstNameAndLastName(String firstName, String lastName);
    Optional<UserEntity> findByEmail(String email);
}
