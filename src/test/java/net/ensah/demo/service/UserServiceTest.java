package net.ensah.demo.service;

import net.ensah.demo.Entity.UserEntity;
import net.ensah.demo.dao.UserDao;
import net.ensah.demo.dtos.UserRequestDto;
import net.ensah.demo.dtos.UserResponseDto;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService underTest;

    @Test
    void shouldCreateUserSuccessfully()
    {
     var userRequestDto=new UserRequestDto("abdelilah","saouiri","abdelilah@gmail","1234");

     when(userDao.findByEmail(userRequestDto.email())).thenReturn(Optional.empty());
     when(passwordEncoder.encode("1234")).thenReturn("HASHED_1234");
     when(userDao.save(any(UserEntity.class))).thenAnswer(invocation -> {
         UserEntity u = invocation.getArgument(0);
         u.setId(1L);
         return u;
     });
     UserResponseDto result = underTest.addUser(userRequestDto);
     assertThat(result).isNotNull();
     assertThat(result.email()).isEqualTo("abdelilah@gmail");
     assertThat(result.firstName()).isEqualTo("abdelilah");
     assertThat(result.lastName()).isEqualTo("saouiri");
     assertThat(result.id()).isEqualTo(1L);

     ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
     verify(passwordEncoder).encode("1234");
     verify(userDao).save(captor.capture());
     UserEntity value = captor.getValue();
     assertThat(value.getEmail()).isEqualTo("abdelilah@gmail");
     assertThat(value.getFirstName()).isEqualTo("abdelilah");
     assertThat(value.getLastName()).isEqualTo("saouiri");
     assertThat(value.getPassword()).isEqualTo("HASHED_1234");
     assertThat(value.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists(){
        var request=new UserRequestDto("abdelilah","saouiri","abdelilah@gmail","1234");
        var existingUser=UserEntity.builder()
                .id(1L)
                .email("abdelilah@gmail")
                .firstName("abdelilah")
                .lastName("saouiri")
                .password("HASHED_1234")
                .build();
        when(userDao.findByEmail(request.email())).thenReturn(Optional.of(existingUser));
        assertThatThrownBy(()->underTest.addUser(request)).isInstanceOf(IllegalArgumentException.class).hasMessage("Email exists");
    }

    @Test
    void shouldFindUserByEmail() {
        String email="abdelilah@gmail";
        var expected=UserEntity.builder()
                .firstName("abdelilah")
                .lastName("saouiri")
                .email("abdelilah@gmail.com")
                .password("HASHED_1234")
                .build();
        when(userDao.findByEmail(email)).thenReturn(Optional.of(expected));
        assertThat(underTest.getUserByEmail(email)).usingRecursiveComparison().ignoringFields("id").isEqualTo(expected);
    }

    @Test
    void shouldNotFindUserByEmail() {
        String email="abdelilah@gmail";
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        assertThatThrownBy(()->underTest.getUserByEmail(email)).isInstanceOf(IllegalArgumentException.class).hasMessage("Email not found");
    }

    @Test
    void ShouldFindAllUsers() {
        List<UserResponseDto> expected=List.of(
                new UserResponseDto(1L,"abdelilah","saouiri","abdelilah@gmail.com"),
                new UserResponseDto(2L,"abdelilah","saouiri","abdelilah@gmail.com")
        );
        List<UserEntity> entities=List.of(
                new UserEntity(1L,"abdelilah","saouiri","abdelilah@gmail.com","HASHED_1234"),
                new UserEntity(2L,"abdelilah","saouiri","abdelilah@gmail.com","HASHED_2345")
        );
        when(userDao.findAll()).thenReturn(entities);
        var result = underTest.getAllUsers();
        assertThat(result.size()).isEqualTo(expected.size());
        assertThat(result).usingRecursiveComparison().isEqualTo(expected);
    }
}