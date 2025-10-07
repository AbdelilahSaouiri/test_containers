package net.ensah.demo.dao;

import net.ensah.demo.Entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.*;


@DataJpaTest
@Import(UserDaoTest.TestConfig.class)
@EntityScan(basePackages = "net.ensah.demo.Entity")
@EnableJpaRepositories(basePackages = "net.ensah.demo.dao")
class UserDaoTest {

    @Configuration
    static class TestConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder(4);
        }
    }


    @Autowired
    private  UserDao userDao;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private UserEntity savedUser1;
    private UserEntity savedUser2;


    @BeforeEach
    void setUp() {
        savedUser1 = userDao.saveAndFlush(UserEntity.builder()
                .firstName("abdelilah")
                .lastName("saouir")
                .email("abdelilah@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .build());
        savedUser2=userDao.saveAndFlush(UserEntity.builder()
                .firstName("sal")
                .lastName("fes")
                .email("sal@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .build());
    }

    @Test
    void shouldFindUserByFirstNameAndLastName(){
        String firstName="abdelilah";
        String lastName="saouir";
        var result = userDao.findByFirstNameAndLastName(firstName, lastName);
        assertThat(result).isPresent();
        assertThat(passwordEncoder.matches("1234", result.get().getPassword())).isTrue();
        assertThat(result.get()).usingRecursiveComparison().ignoringFields("id").isEqualTo(savedUser1);
    }
    @Test
    void shouldNotFindUserByFirstNameAndLastName(){
        String firstName="said";
        String lastName="saouir";
        var result = userDao.findByFirstNameAndLastName(firstName, lastName);
        assertThat(result).isNotPresent();
    }

    @Test
    void shouldFindUserByEmail(){
       String email="sal@gmail.com";
        var result = userDao.findByEmail(email);
        assertThat(result).isPresent();
        assertThat(result.get()).usingRecursiveComparison().ignoringFields("id").isEqualTo(savedUser2);
    }

    @Test
    void shouldNotFindUserByEmail(){
        String email="test@gmail.com";
        var result = userDao.findByEmail(email);
        assertThat(result).isNotPresent();
    }
}