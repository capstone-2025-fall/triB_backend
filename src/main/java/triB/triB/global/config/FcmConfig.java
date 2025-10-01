package triB.triB.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${firebase.credentials.path}")
    private String firebaseCredPath;

    @PostConstruct
    public void initFirebaseSDK() throws Exception{
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(firebaseCredPath)))
                .build();
        FirebaseApp.initializeApp(options);
    }
}
