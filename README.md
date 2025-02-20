--- 알고리즘 스터디 프로젝트 ---
# 프로젝트명 (예:Algorithm Study Backend(알쓰))

## 📝 소개
이 프로젝트는 알고리즘 스터디를 위한 백엔드 서버입니다.  
사용자들은 그룹을 생성하여 알고리즘 문제를 함께 해결하고,  
자신의 코드와 그룹원들의 코드를 비교하며 알고리즘 실력을 향상시킬 수 있습니다.

## 🚀 주요 기능
### 1. 그룹 관리  
- 사용자는 **스터디 그룹을 생성**하고, 비밀번호를 입력해 가입이 가능합니다.  
- 그룹장은 **문제 생성** 및 **그룹 설정 변경**이 가능합니다.  

### 2. 문제 풀이 및 채점  
- 사용자는 그룹 내에서 **알고리즘 문제를 제출 및 풀이**할 수 있습니다.  
- 코드 실행 및 채점을 통해 정답 여부를 확인할 수 있으며, 그룹원들의 코드와 비교할 수도 있습니다.  

### 3. 그룹 채팅 및 소통  
- 그룹별 **실시간 채팅 기능**을 제공하여, 문제 풀이 관련 논의 및 스터디 진행이 가능합니다.  
- 특정 코드나 문제에 대한 의견을 공유할 수도 있습니다.  

### 4. 예치금 충전 및 환급  
- 사용자들은 서비스 이용을 위해 **예치금을 충전**할 수 있으며, **사용 후 남은 금액을 환급**받을 수도 있습니다.  
- 결제 및 환불 시스템을 통해 안전하게 거래가 진행됩니다.  

### 5. 사용자 인증 (로그인/로그아웃)  
- 로그인한 사용자만 그룹 생성 및 가입, 문제 풀이 등의 기능을 사용할 수 있습니다.  
- JWT를 사용하여 보안을 강화합니다.
## 🏗️ 프로젝트 구조
📦 als  
 ┣ 📂 apiPayload                 # API 응답 및 오류 처리 관련  
 ┃ ┣ 📂 code                     # 코드 상태 및 오류 관련 클래스  
 ┃ ┣ 📂 exception                # 예외 처리 클래스  
 ┃ ┗ 📜 ApiResult.java           # API 결과 형식 정의  
 ┣ 📂 aws                        # AWS 관련 설정  
 ┃ ┗ 📂 s3                       # S3 관리 클래스  
 ┣ 📂 config                     # 애플리케이션 설정  
 ┣ 📂 group                      # 그룹 관련 기능  
 ┃ ┣ 📂 controller               # 그룹 관련 API 컨트롤러  
 ┃ ┣ 📂 dto                      # 그룹 관련 DTO 클래스  
 ┃ ┣ 📂 entity                   # 그룹 관련 엔터티  
 ┃ ┣ 📂 repository               # 그룹 데이터 접근 객체  
 ┃ ┣ 📂 converter                # 그룹 관련 컨버터  
 ┃ ┗ 📂 service                  # 그룹 관련 서비스  
 ┣ 📂 page                       # 페이지 관련 기능  
 ┣ 📂 payment                    # 결제 관련 기능  
 ┣ 📂 problem                    # 문제 및 제출 관련 기능  
 ┣ 📂 user                       # 사용자 관련 기능  
 ┣ 📂 valid                      # 유효성 검사 관련 기능  
 ┣ 📂 websocket                  # WebSocket 관련 기능  
 ┣ 📜 AlsApplication.java        # 애플리케이션 시작점  


## ⚙️ 기술 스택
- **Backend**: Spring Boot, Java, Spring MVC, Spring Data JPA  
- **Database**: MySQL / PostgreSQL  
- **AI Model**: Flask, Scikit-learn, TensorFlow  
- **API**: Spotify API, Flask API  
- **Infra**: Docker, AWS (EC2, S3), Nginx  

## 📖 API 문서
(추후 Swagger 혹은 Postman 문서를 링크)  

## 🛠️ 배포
