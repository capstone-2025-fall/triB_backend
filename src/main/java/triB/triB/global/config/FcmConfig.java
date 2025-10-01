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
    public void initFirebaseSDK() throws Exception {
        try (FileInputStream in = new FileInputStream(firebaseCredPath)) {
            GoogleCredentials creds = GoogleCredentials.fromStream(in);
            FirebaseOptions.Builder b = FirebaseOptions.builder().setCredentials(creds);

            // credentials에서 projectId 추출해 명시적으로 주입(일부 환경에서 null인 경우가 있음)
            String projectId = null;
            if (creds instanceof com.google.auth.oauth2.ServiceAccountCredentials sac) {
                projectId = sac.getProjectId();
            }
            if (projectId != null) {
                b.setProjectId(projectId);
            }

            FirebaseOptions options = b.build();
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase initialized. projectId={}", app.getOptions().getProjectId());
        }
    }
}
