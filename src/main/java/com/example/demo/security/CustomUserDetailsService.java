package com.example.demo.security;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;


/**
 * This class implements Spring Security's {@link UserDetailsService} interface.
 * It is responsible for loading user-specific data from the database during the
 * authentication process.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method is called by Spring Security when a user tries to authenticate.
     * It loads the user's details from the database based on their email address.
     *
     * @param email The email address of the user to load.
     * @return A {@link UserDetails} object containing the user's credentials and authorities.
     * @throws UsernameNotFoundException if no user is found with the given email address.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Find the user in the database by their email address.
        User user = userRepository.findByEmailWithProfile(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Create a set of authorities (roles) for the user.
        // Spring Security requires roles to be prefixed with "ROLE_".
        Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // 3. Return a Spring Security User object.
        // This object contains the user's email (as the username), password, and authorities.
        // Spring Security will use this object to verify the user's credentials.
        return new org.springframework.security.core.userdetails.User(user.getEmail(),
                user.getPassword(),
                authorities);
    }
}
