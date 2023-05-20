# Redis

## 캐싱 전략
### Cache-Aside (Lazy Loading)
- 항상 캐시를 먼저 체크하고, 없으면 원본(ex. DB)에서 읽어온 후 캐시에 저장
- 장점
  - 필요한 데이터만 캐시에 저장되고, Cache Miss가 있어도 치명적이지 않음
- 단점
  - 최초 접근은 느림, 업데이트 주기가 일정하지 않기 때문에 캐시가 최신 데이터가 아닐 수 있음
### Write-Through
- 데이터를 쓸 때 항상 캐시를 업데이트하여 최신 상태를 유지함
- 장점
  - 캐시가 항상 동기화되어 데이터가 최신이다.
- 단점
  - 자주 사용하지 않는 데이터도 캐시되고, 쓰기 지연시간이 증가한다.
### Write-back
- 데이터를 캐시에만 쓰고, 캐시의 데이터를 일정 주기로 DB에 업데이트
- 장점
  - 쓰기가 많은 경우 DB 부하를 줄일 수 있음
- 단점
  - 캐시가 DB에 쓰기 전에 장애가 생기면 데이터 유실 가능

## 데이터 제거 방식
- 캐시에서 어떤 데이터를 언제 제거할 것인가?
### Expiration
각 데이터에 TTL(Time-To-Live)을 설정해 시간 기반으로 삭제 
### Eviction Algorithm
공간을 확보해야 할 경우 어떤 데이터를 삭제할지 결정하는 방식 
#### LRU(Least Recently Used)
가장 오랫동안 사용되지 않은 데이터를 삭제
#### LFU(Least Frequently Used)
가장 적게 사용된 데이터를 삭제(최근에 사용되었더라도)
#### FIFO(First In First Out)
먼저 들어온 데이터를 삭제

## Spring의 캐시 추상화
- CacheManager를 통해 일반적인 캐시 인터페이스 구현(다양한 캐시 구현체가 존재)
- 어노테이션만으로 메서드에 캐시를 손쉽게 적용 가능

### @Cacheable
메서드에 캐시를 적용한다. (Cache-Aside 패턴 수행)
### @CachePut
메서드의 리턴값을 캐시에 설정한다.
### @CacheEvict
메서드의 키값을 기반으로 캐시를 삭제한다.

--- 

## Redis의 Pub/Sub 특징
- 메시지가 큐에 저장되지 않음
- Kafka 컨슈머 그룹같은 분산 처리 개념이 없음
- 메시지 발행 시 Push 방식으로 Subscriber들에게 전송 (전송 후 메시지 삭제)
- Subscriber가 늘어날수록 성능이 저하

### Pub/Sub 유즈케이스
- 실시간으로 빠르게 전송되어야 하는 메시지
- 메시지 유실을 감내할 수 있는 케이스
- 최대 1회 전송(at-most-once) 패턴이 적합한 경우 (중복 메시지는 X)
- Subscriber들이 다양한 채널을 유동적으로 바꾸면서 한시적으로 구독하는 경우
