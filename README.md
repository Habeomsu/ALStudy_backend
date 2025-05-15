# 프로젝트명 (예:Algorithm Study Backend(알쓰))

## 📝 소개
이 프로젝트는 알고리즘 스터디를 위한 백엔드 서버입니다.  
사용자들은 그룹을 생성하여 알고리즘 문제를 함께 해결하고,  
자신의 코드와 그룹원들의 코드를 비교하며 알고리즘 실력을 향상시킬 수 있습니다.

## 🔍 프로젝트 특징
### 1. 통일된 API 페이로드
모든 API가 동일한 페이로드 구조를 사용하여 일관된 데이터 형식을 제공합니다.  
이는 클라이언트와 서버 간의 통신을 간소화하고 유지 보수를 용이하게 합니다.

## 🚀 주요 기능
### 1. 그룹 관리  
- 사용자는 **스터디 그룹을 생성**하고, 비밀번호를 입력해 가입이 가능합니다.  
- 그룹장은 **문제 생성** 및 **그룹 설정 변경**이 가능합니다.
- 관련 링크  
- https://github.com/Habeomsu/ALStudy/blob/main/Als/src/main/java/main/als/group/service/GroupServiceImpl.java  
- 그룹생성<img width="1697" alt="스크린샷 2025-02-24 오후 3 48 03" src="https://github.com/user-attachments/assets/f481f00e-b144-4227-9eaf-354fa9f74b9c" />

### 2. 문제 풀이 및 채점  
- 사용자는 그룹 내에서 **알고리즘 문제를 제출 및 풀이**할 수 있습니다.  
- 코드 실행 및 채점을 통해 정답 여부를 확인할 수 있으며, 그룹원들의 코드와 비교할 수도 있습니다.  
- 관련 링크
- https://github.com/Habeomsu/ALStudy/blob/main/Als/src/main/java/main/als/problem/service/SubmissionServiceImpl.java  
- 문제 코드 제출<img width="1697" alt="스크린샷 2025-02-24 오후 3 47 26" src="https://github.com/user-attachments/assets/ef2bcc7d-fed8-474a-9eed-2ad526f67b6d" />  
- 그룹원 코드 비교 <img width="1697" alt="스크린샷 2025-02-24 오후 3 47 12" src="https://github.com/user-attachments/assets/cab49f52-f1df-4295-b149-83adc9327b57" />
### 3. 그룹 채팅 및 소통  
- 그룹별 **실시간 채팅 기능**을 제공하여, 문제 풀이 관련 논의 및 스터디 진행이 가능합니다.  
- 특정 코드나 문제에 대한 의견을 공유할 수도 있습니다.  
- 관련 링크
- https://github.com/Habeomsu/ALStudy/blob/main/Als/src/main/java/main/als/websocket/service/MessageServiceImpl.java  
- 그룹 채팅 <img width="1699" alt="스크린샷 2025-02-24 오후 3 46 51" src="https://github.com/user-attachments/assets/628b593d-8fc5-4320-aaed-bdbd4ea29c02" />
### 4. 예치금 충전 및 환급  
- 사용자들은 서비스 이용을 위해 **예치금을 충전**할 수 있으며, **사용 후 남은 금액을 환급**받을 수도 있습니다.  
- 결제 및 환불 시스템을 통해 안전하게 거래가 진행됩니다.  
- 관련 링크
- https://github.com/Habeomsu/ALStudy/blob/main/Als/src/main/java/main/als/payment/service/PaymentServiceImpl.java  
- 예치금 충전 <img width="1710" alt="스크린샷 2025-02-24 오후 3 46 37" src="https://github.com/user-attachments/assets/aff5f1a8-c26a-46ca-85b9-15eff4f1fe59" />

### 5. 사용자 인증 (로그인/로그아웃)  
- 로그인한 사용자만 그룹 생성 및 가입, 문제 풀이 등의 기능을 사용할 수 있습니다.  
- JWT를 사용하여 보안을 강화합니다.

## 📌 테스트 커버리지
<img width="1264" alt="스크린샷 2025-05-15 오후 3 49 30" src="https://github.com/user-attachments/assets/2455720d-439a-4e0f-a291-da4626089511" />   

- 관련 링크
- https://github.com/Habeomsu/ALStudy_backend/tree/main/Als/src/test/java/main/als

###  커버리지 미달 원인 정리

### 1. 컨버터 (Converter)

- 각 도메인 패키지 내부의 `Converter` 클래스는 테스트 코드 미작성.
- 대부분 단순 `Entity → DTO` 변환만 수행하므로 생략했으나, **Method / Line 커버리지 하락에 직접 영향**.

### 2. DTO (Data Transfer Object)

- `group`, `problem`, `user` 등 각 패키지 내 `DTO 클래스`는 테스트 대상에서 제외.
- DTO는 테스트 대상은 아니지만, **Class 커버리지에 포함**되어 수치 하락에 영향을 줌.

### 3. Util 클래스 (`payment.util.FlaskCommunicationUtil`)

- 외부 HTTP 통신(Flask API)과 파일 입출력을 포함하고 있어 테스트 작성이 어려움.
- 이로 인해 **payment 패키지 Line 커버리지 하락**.

### 4. `config`, `valid`, `aws.s3` 패키지

- 전반적인 설정, 인증 관련 클래스만 포함되어 있어 테스트 작성 제외.
- 전혀 테스트되지 않아 `Class`, `Method`, `Line` 모두 0%.

## 🏗️ 프로젝트 구조
```
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

```
## ⚙️ 기술 스택
- **Backend**: Spring Boot, Java, Spring MVC, Spring Data JPA, Flask
- **STOMP**: RabbitMQ  
- **Database**: MySQL
- **API**: Flask API, TOSS_PAYMENTS API  


## 📖 API 문서
추후 스웨거 문서 사용

## 🛠️ 배포
[여러가지 배포 과정을 사용](https://github.com/Habeomsu/ALStudy_deploy)

