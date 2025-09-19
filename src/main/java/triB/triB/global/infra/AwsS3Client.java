package triB.triB.global.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AwsS3Client {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.s3.region}")
    private String region;

    // 이미지 업로드
    public String uploadFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);
        } catch (IOException e){
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    // 파일 삭제
    public void delete(String fileName) {
        if (fileName == null || fileName.isEmpty())
            return;

        try{
            String fileKey = fileName.substring(fileName.lastIndexOf("/") + 1);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e){
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

}
