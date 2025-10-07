package net.ensah.demo.service;

import net.ensah.demo.Entity.UserEntity;
import net.ensah.demo.dao.UserDao;
import net.ensah.demo.dtos.UserRequestDto;
import net.ensah.demo.dtos.UserResponseDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDto> getAllUsers(){
        return userDao.findAll().stream().map(
                user->new UserResponseDto(user.getId(),user.getFirstName(),user.getLastName(),user.getEmail())
        ).toList();
    }

    @Transactional
    public UserResponseDto addUser(UserRequestDto request) {
        if (userDao.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email exists");
        }
        UserEntity userEntity=UserEntity.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();
        userDao.save(userEntity);
        return new UserResponseDto(userEntity.getId(),userEntity.getFirstName(),userEntity.getLastName(),userEntity.getEmail());
    }

    public UserResponseDto getUserByEmail(String email) {
        UserEntity userEntity = userDao.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Email not found"));
        return new UserResponseDto(userEntity.getId(),userEntity.getFirstName(),userEntity.getLastName(),userEntity.getEmail());

    }
}
