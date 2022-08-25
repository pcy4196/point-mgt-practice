# point-management-practice
포인트 관리하기 예시 <SpringBatch 프로젝트>

#### 2022.08.10 
+ new project and gradle setting (initial)
    + 프로젝트 세팅(gradle) 및 github push - spring boot 2.5.4
#### 2022.08.15
+ Batch Application 구현
  1. PointMgtPracticeApplication.java
  2. BatchConfig.java
  3. application.yml
#### 2022.08.16
+ MySQL DB 설정(연동)
  1. application.yml 파일 수정
+ Entity, Repository
  1. Message
  2. MessageRepository
  3. PointReservation
  4. PointReservationRepository
  5. PointWallet
  6. PointWalletRepository
  7. IdEntity
  8. Point
  9. PointRepository
+ SpringBatch TEST 공통
  1. BatchTestSupport
  2. application.yml(Test) 구현
#### 2022.08.21
+ SpringBatch 로직 구현 - 포인트 만료 01 -
  + 수정
    1. application.yml
    2. PointWallet.java
    3. Point.java
  + 추가
    1. ExpirePointJobConfigurationTest.java
    2. ExpirePointJobConfiguration.java
    3. ExpirePointStepConfiguration.java
+ SpringBatch 로직 구현 - 포인트 만료 02 -
  + 추가구현(Validator, Incrementer)
  + 추가
    1. TodayJobParameterValidator.java
  + 수정
    1. ExpirePointJobConfiguration.java
#### 2022.08.25
+ SpringBatch 로직 구현 - 포인트 예약 적립 -
  + 수정
    1. BatchTestSupport.java - TEST 종료 시 DATA 삭제 순서 변화(CASCADE 때문)
    2. PointReservation.java
  + 추가
    1. ExecutePointReservationJobConfigurationTest.java
    2. ExecutePointReservationJobConfiguration.java
    3. ExecutePointReservationStepConfiguration.java
