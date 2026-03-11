package com.collabhub.security;

import com.collabhub.domain.User;
import com.collabhub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2UserServiceImpl.class);
    private final UserRepository userRepository;

    public OAuth2UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Use the result of orElseGet
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User(name, email, "DEVELOPER", "OAUTH@_NO_PASSWORD");
            return userRepository.save(newUser);
        });

        // Optional: do something with `user` here (e.g., logging)
        LOG.info("OAuth2 user logged in: {} ({})", user.getName(), user.getEmail());

        return oAuth2User;
    }
}