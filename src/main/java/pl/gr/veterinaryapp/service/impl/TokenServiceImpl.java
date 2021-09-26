package pl.gr.veterinaryapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import pl.gr.veterinaryapp.common.AuthToken;
import pl.gr.veterinaryapp.exception.IncorrectDataException;
import pl.gr.veterinaryapp.jwt.TokenProvider;
import pl.gr.veterinaryapp.model.dto.LoginUser;
import pl.gr.veterinaryapp.model.entity.BlockedToken;
import pl.gr.veterinaryapp.model.entity.VetAppUser;
import pl.gr.veterinaryapp.repository.BlockedTokenRepository;
import pl.gr.veterinaryapp.repository.UserRepository;
import pl.gr.veterinaryapp.service.TokenService;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static pl.gr.veterinaryapp.common.TokenConstants.ACCESS_TOKEN_VALIDITY_SECONDS;
import static pl.gr.veterinaryapp.common.TokenConstants.TOKEN_PREFIX;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final BlockedTokenRepository blockedTokenRepository;
    private final BCryptPasswordEncoder bcryptEncoder;
    private final TokenProvider jwtTokenUtil;
    private final Clock systemClock;

    public AuthToken register(@RequestBody LoginUser loginUser) {
        var user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password."));
        if (!bcryptEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUser.getUsername(),
                        loginUser.getPassword(),
                        getAuthority(user)
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String token = jwtTokenUtil.generateToken(authentication);
        return new AuthToken(token);
    }

    private Set<SimpleGrantedAuthority> getAuthority(VetAppUser vetAppUser) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + vetAppUser.getRole().getName()));
        return authorities;
    }

    @Transactional
    public void logout(String header) {
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            throw new IncorrectDataException("Incorrect authentication header.");
        }

        String authToken = header.replace(TOKEN_PREFIX, "");
        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(authToken);

        var token = new BlockedToken();
        token.setAuthToken(authToken);
        token.setExpirationTime(OffsetDateTime.ofInstant(expirationDate.toInstant(), ZoneOffset.systemDefault()));

        blockedTokenRepository.save(token);
    }

    @Scheduled(fixedDelay = ACCESS_TOKEN_VALIDITY_SECONDS * 1000)
    @Transactional
    public void checkExpiredVisits() {
        blockedTokenRepository.deleteAllByExpirationTimeBefore(OffsetDateTime.now(systemClock));
    }
}

