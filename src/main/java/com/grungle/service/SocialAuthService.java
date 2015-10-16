package com.grungle.service;

import com.google.common.base.Preconditions;
import com.grungle.domain.User;
import com.grungle.repository.UserRepository;
import com.grungle.security.social.GithubTokenRequest;
import com.grungle.security.social.GithubTokenResponse;
import com.grungle.security.xauth.Token;
import com.grungle.security.xauth.TokenProvider;
import com.grungle.web.rest.dto.user.GithubUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.Optional;


@Service
public class SocialAuthService {

    private static final Logger LOG = LoggerFactory.getLogger(SocialAuthService.class);

    private static final String BASE_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_URL = "https://api.github.com/user?access_token=";

    @Inject
    private RestTemplate restTemplate;

    @Inject
    private RegistrationService registrationService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private TokenProvider tokenProvider;

    @Inject
    private UserDetailsService userDetailsService;


    public Token getTokenResponse(String code) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        GithubTokenRequest req = new GithubTokenRequest(code);
        HttpEntity<GithubTokenRequest> request = new HttpEntity<>(req, headers);
        GithubTokenResponse response = restTemplate.postForObject(BASE_URL, request, GithubTokenResponse.class);
        LOG.debug("Getting user Object using access token");
        GithubUser ghUser = getUser(response.getAccessToken());
        User user;
        Optional<User> dbUser = userRepository.findOneByLogin(ghUser.getEmail());
        if (!dbUser.isPresent()) {
            // Register
            user = registrationService.registerSocialUser(ghUser.getEmail(), getFirstName(ghUser.getName()), null, "en", "Github");
        } else {
            // Already registered, just log them in
            user = dbUser.get();
        }
        return generateToken(user.getLogin());
    }


    private Token generateToken(String login) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(login);
        return tokenProvider.createToken(userDetails);
    }

    private String getFirstName(String name) {
        return name.split(" ")[0];
    }


    public GithubUser getUser(String accessToken) {
        String url = USER_URL + accessToken;
        ResponseEntity<GithubUser> response = restTemplate.getForEntity(url, GithubUser.class);
        LOG.debug("Obtained Github user = {}", response);
        return response.getBody();
    }

}