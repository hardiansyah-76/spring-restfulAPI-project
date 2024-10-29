package yukinari.software.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import yukinari.software.entity.User;
import yukinari.software.model.LoginUserRequest;
import yukinari.software.model.TokenResponse;
import yukinari.software.repository.UserRepository;
import yukinari.software.security.BCrypt;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public TokenResponse login(LoginUserRequest request) {
        //validate request
        validationService.validate(request);

        //check in db if name already registered
        User user = userRepository.findById(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username already registered"));

        //validate password
        if (BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            //if login success create token and token expired
            user.setToken(UUID.randomUUID().toString());
            user.setTokenExpiredAt(expired30Days());
            userRepository.save(user);

            return TokenResponse.builder()
                    .token(user.getToken())
                    .expiredAt(user.getTokenExpiredAt())
                    .build();

        } else {
            //login failed
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username already registered");
        }

    }

    //example method expaired
    private Long expired30Days (){
        return System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 30);
    }

    @Transactional
    public void logout (User user) {
        user.setToken(null);
        user.setTokenExpiredAt(null);
        userRepository.save(user);
    }

}
