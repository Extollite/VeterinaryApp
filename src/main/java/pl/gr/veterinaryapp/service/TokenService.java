package pl.gr.veterinaryapp.service;

import pl.gr.veterinaryapp.common.AuthToken;
import pl.gr.veterinaryapp.model.dto.LoginUser;

public interface TokenService {

    AuthToken register(LoginUser loginUser);
}
