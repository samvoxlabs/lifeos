package com.familyos.familyos.authentication.service;

import com.familyos.familyos.authentication.exception.UnauthorizedException;
import com.familyos.familyos.dto.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Authentication required");
        }

        if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser;
        }

        throw new UnauthorizedException("Unsupported authenticated principal");
    }
}
