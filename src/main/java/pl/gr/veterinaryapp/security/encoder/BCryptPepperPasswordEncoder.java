package pl.gr.veterinaryapp.security.encoder;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class BCryptPepperPasswordEncoder extends BCryptPasswordEncoder {

    @Value("${app.security.pepper:pepper}")
    private String pepper;

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }

        return super.encode(combinePasswordWithPepper(rawPassword.toString()));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }

        return super.matches(combinePasswordWithPepper(rawPassword.toString()), encodedPassword);
    }

    private String combinePasswordWithPepper(String password) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(pepper.getBytes(), "HmacSHA256");
            hmacSHA256.init(secret_key);
            return Base64.encodeBase64String(hmacSHA256.doFinal(password.getBytes()));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Couldn't encode password", e);
        }
    }
}
