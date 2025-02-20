--- 알고리즘 스터디 프로젝트 ---
# 프로젝트명 (예: Algorithm Study Backend(알쓰))

## 📝 소개
이 프로젝트는 알고리즘 스터디 백엔드 서버입니다.  
사용자들은 그룹을 만들어서 알고리즘 스터디를 진행합니다.
자신의 코드와 그룹원들의 코드를 비교를 통해 알고리즘 능력을 향상시킬수 있습니다.

## 🚀 주요 기능
### 1. 음악 파일 업로드 및 처리
- 사용자가 음악 파일을 업로드하면 AI 모델을 통해 장르를 분석합니다.
- 분석된 장르에 따라 관련 음악을 데이터베이스에서 검색합니다.

### 2. 음악 유사도 분석
- 검색된 음악 목록을 Flask AI 서버에 전송하여 유사도를 계산합니다.
- 계산된 유사도 점수를 기반으로 최적의 추천 곡을 반환합니다.

### 3. Spotify API 연동
- Spotify API를 활용하여 데이터베이스에서 검색된 곡들의 미리듣기(preview URL) 및 메타데이터(앨범 이미지, 아티스트 등)를 가져옵니다.

### 4. REST API 제공
- 프론트엔드 및 외부 서비스와의 통신을 위한 RESTful API를 제공합니다.

## 🏗️ 프로젝트 구조

하하하 

## ⚙️ 기술 스택
- **Backend**: Spring Boot, Java, Spring MVC, Spring Data JPA  
- **Database**: MySQL / PostgreSQL  
- **AI Model**: Flask, Scikit-learn, TensorFlow  
- **API**: Spotify API, Flask API  
- **Infra**: Docker, AWS (EC2, S3), Nginx  

## 📖 API 문서
(추후 Swagger 혹은 Postman 문서를 링크)  

## 🛠️ 배포
