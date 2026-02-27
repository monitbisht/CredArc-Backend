package com.credarc.credarc.service;



import com.credarc.credarc.dto.SignupRequest;
import com.credarc.credarc.entity.User;
import com.credarc.credarc.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public boolean isEmailRegistered(String email){
        return userRepository.existsByEmail(email);
    }

    public boolean isMobileRegistered(String mobile){
        return userRepository.existsByMobile(mobile);
    }


    public User createUser(SignupRequest request , String hashedPassword){
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setMobile(request.getMobile());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(hashedPassword);

        return userRepository.save(newUser);
    }
}
