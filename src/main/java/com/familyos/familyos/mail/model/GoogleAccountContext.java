package com.familyos.familyos.mail.model;

import com.familyos.familyos.authentication.entity.OAuthAccount;

public record GoogleAccountContext(
        OAuthAccount account,
        String accessToken
) {
}
