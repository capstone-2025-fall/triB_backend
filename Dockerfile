# 베이스 이미지
FROM eclipse-temurin:17-jre

# 빌드 된 파일 경로 변수 설정
ARG JAR_FILE=build/libs/*.jar

# jar 파일 복사
# 위에서 지정한 jar 파일을 도커 이미지 안의 app.jar 라는 이름으로 복사
COPY ${JAR_FILE} app.jar

# 애플리케이션 실행
ENTRYPOINT ["java","-Duser.timezone=Asia/Seoul","-jar","/app.jar"]