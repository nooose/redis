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

---

# Redis의 백업과 장애 복구
## RDB(Redis DataBase)를 사용한 백업
- 특정 시점의 스냅샷으로 데이터 저장
- 재시작 시 RDB 파일이 있으면 읽어서 복구

### RDB 사용의 장점
- 작은 파일 사이즈로 백업 파일 관리가 용이
- fork를 이용해 백업하므로 서비스 중인 프로세스는 성능에 영향 없음 (fork time이 매우 낮다)
- 데이터 스냅샷 방식이므로 빠른 복구가 가능

### RDB 사용의 단점
- 스냅샷을 저장하는 시점 사이의 데이터 변경사항은 유실될 수 있음
- fork를 이용하기 때문에 시간이 오래 걸릴 수 있고, CPU와 메모리 자원을 많이 소모
- 데이터 무결성이나 정합성에 대한 요구가 크지 않은 경우 사용 가능 (마지막 백업 시 에러 발생 등의 문제)

### RDB 설정
- 설정 파일이 없어도 기본값으로 RDB가 활성화되어 있음
- 설정 파일을 만드려면 [템플릿](https://redis.io/docs/management/config/)을 받아서 사용

- 예시
  - `save 60 10`: 저장 주기 설정(60초 마다 10개 이상의 변경이 있을 때 수행)
  - `dbfilename dump.rdb`: 스냅샷을 저장할 파일 이름
  - `bgsave`: 수동으로 스냅샷 저장

## AOF(Append Only File)를 사용한 백업
- 모든 쓰기 요청에 대한 로그를 저장
- 재시작 시 AOF에 기록된 모든 동작을 재수행해서 데이터를 복구

### AOF 사용의 장점
- 모든 변경사항이 기록되므로 RDB 방식 대비 안정적으로 데이터 백업 가능
- AOF 파일은 append-only 방식이므로 백업 파일이 손상될 위험이 적음
- 실제 수행된 명령어가 저장되어 있으므로 사람이 보고 이해할 수 있고 수정도 가

### AOF 사용의 단점
- RDB 방식보다 파일 사이즈가 커짐
- RDB 방식 대비 백업&복구 속도가 느림 (백업 성능은 fsync 정책에 따라 조절 가능)

### AOF 설정
- 예시
  - `appendonly yes`: AOF 사용 (기본값 no)
  - `appendfilename appendonly.aof`: AOF 파일 이름
  - `appendfsync everysec`: fsync 정책 설정 (always, everysec, no)

#### fsync 정책 (appendfsync 설정 값)
- fsync() 호출은 OS에게 데이터를 디스크에 쓰도록  함
- 가능한 옵션과 설명
  - always: 새로운 커맨드가 추가될 때마다 수행. 가장 안전하지만 가장 느림
  - everysec: 1초마다 수행, 성능은 RDB 수준에 근접
  - no: OS에 맡김. 가장 빠르지만 덜 안전한 방법 (커널마다 수행 시간이 다를 수 있음)

### AOF 관련 개념
- Log rewriting: 최종 상태를 만들기 위한 최소한의 로그만 남기기 위해 일부를 새로 씀
  
  (ex. 1개의 key 값을 100번 수정해도 최종 상태는 1개이므로 SET 1개로 대체 가능)
- Multi Part AOF: Redis 7.0 부터 AOF가 단일 파일에 저장되지 않고 여러 개가 사용됨
  - base file: 마지막 rewrite 시의 스냅샷을 저장
  - incremental file: 마지막으로 base file이 생성된 이후의 변경사항이 쌓임
  - manifest file: 파일들을 관리하기 위한 메타 데이터를 저장

## Redis replication(복제)
- 백업만으로는 장애 대비에 부족함 (백업 실패 가능성, 복구에 소요되는 시간)
- Redis도 복제를 통해 가용성을 확보하고 빠른 장애조치가 가능
- Master가 죽었을 경우 replica 중 하나를 master로 전환해 즉시 서비스 정상화 가능
- 복제본(replica)은 read-only 노드로 사용 가능하므로 traffic 분산도 가능

```bash
replicaof 127.0.0.1 6379 # Replica로 동작하도록 설정
replica-read-only
```

## Redis Sentinel
- Redis에서 HA를 제공하기 위한 장치
- master-replica 구조에서 master가 다운 시 replica를 master로 승격시키는 auto-failover를 수행
- SDOWN(Subjective down)과 ODOWN(Objective down)의 2가지 판단이 있음
  - SDOWN: Sentinel 1대가 down으로 판단
  - ODOWN: Quorum(정족수)가 충족되어 down으로 판단(객관적)
- master 노드가 down된걸로 판단되기 위해서는 Sentinel 노드들이 Quorum(정족수)을 충족해야 함
- 클라이언트는 Sentinel을 통해 master의 주소를 얻어내야 함

---

## Redis Cluster
- 여러 노드에 자동적인 데이터 분산
- 일부 노드의 실패나 통신 단절에도 계속 작동하는 가용성
- 고성능을 보장하면서 선형 확장성을 제공

### Redis Cluster 특징
- full-mesh 구조로 통신
- cluster bus라는 추가 채널(port) 사용
- `gossip protocol` 사용: 모든 노드가 통신하지 않도록 함 
- hash slot을 사용한 키 관리
- DB0만 사용 가능
- multi key 명령어가 제한됨
- 클라이언트는 모든 노드에 접속

### Sentinel과의 차이점
- 클러스터는 데이터 분산(샤딩)을 제공
- 클러스터는 자동 장애조치를 위한 모니터링 노드(Sentinel)를 추가 배치할 필요가 없음
- 클러스터에서는 multi key 오퍼레이션이 제한됨
- Sentinel은 비교적 단순하고 소규모의 시스템에서 HA가 필요할 때 채

### 데이터를 분산하는 기준
- 보통 분산 시스템에서 해싱이 사용됨
- 단순 해싱으로는 노드의 개수가 변할 때 모든 매핑이 새로 계산되어야 하는 문제가 있음
- 특정 key의 데이터가 어느 노드(shard)에 속할 것인지 결정하는 메커니즘이 있어야 함
  - Hash Slot을 이용한 데이터 분산
    - Redis는 16384개의 hash slot으로 key 공간을 나누어 관리
    - 각 키는 CRC16 해싱 후 16384로 modulo 연산을 해 각 hash slot에 매핑
    - hash slot은 각 노드들에게 나누어 분배됨

### 클라이언트의 데이터 접근
- 클러스터 노드는 요청이 온 key에 해당하는 노드로 자동 redirect를 해주지 않는다.
- 클라이언트는 MOVED 에러를 받으면 해당 노드로 다시 요청해야 함

#### 클러스터를 사용할 때의 성능
- 클라이언트가 MOVED 에러에 대해 재요청을 해야하는 문제
  - 클라이언트(라이브러리)는 key-node 맵을 캐싱하므로 대부분의 경우 발생하지 않음
- 클라이언트는 단일 인스턴스의 Redis를 이용할 때와 같은 성능으로 이용 가능
- 분산 시스템에서 성능은 데이터 일관성(consistency)과 trade-off가 있음
  - Redis Cluster는 고성능의 확장성을 제공하면서 적절한 수준의 데이터 안정성과 가용성을 유지하는 것을 목표로 설계됨

### 클러스터의 데이터 일관성
- Redis Cluster는 strong consistency를 제공하지 않음
  - 높은 성능을 위해 비동기 복제를 하기 때문
  -  예) 복제가 완료되기 전에 master가 죽으면 데이터가 유실됨
### 클러스터의 가용성 - auto failover
- 일부 노드(master)가 실패(또는 네트워크 단절)하더라도 과반수 이상의 master가 남아있고, 사라진 master의 replica들이 있다면 클러스터는 failover가 되어 가용한 상태가 된다.
- node timeout동안 과반수의 master와 통신하지 못한 master는 스스로 error state로 빠지고 write 요청을 받지 않음
- replica가 다른 master로 migrate 해서 가용성을 높인다.

### 클러스터 제약사항1 - 클러스터에서 DB0만 사용 가능
- Redis는 한 인스턴스에 여러 데이터베이스를 가질 수 있으며 default는 16
  - `databases 16`
- Multi DB는 용도별로 분리해서 관리를 용이하게 하기 위한 목적
- 클러스터에서는 해당 기능을 사용할 수 없고 DB0으로 고정됨

### 클러스터 제약사항2 - Multi key operation 사용의 제약
- key들이 각각 다른 노드에 저장되므로 MSET과 같은 multi-key operation은 기본적으로 사용할 수 없다.
- 같은 노드 안에 속한 key들에 대해서는 multi-key operation이 가능
- hash tags 기능을 사용하면 여러 key들을 같은 hash slot에 속하게 할 수 있음
  - key 값 중 `{}` 안에 들어간 문자열에 대해서만 해싱을 수행하는 원리

### 클러스터 제약사항3 - 클라이언트 구현의 강제
- 클라이언트는 클러스터의 모든 노드에 접속해야 함
- 클라이언트는 redirect 기능을 구현해야 함(MOVED 에러의 대응)
- 클라이언트 구현이 잘 된 라이브러리가 없는 환경도 있을 수 있음 (Spring, Node 등등은 모두 잘 구현되어 있음)

---

## Redis 성능 튜닝
### Eviction 정책
- 메모리가 한계에 도달했을 때 어떤 조치가 일어날지 결정
- Eviction policy

### Redis 메모리 관리
```bash
mammemory 100mb # 지정하지 않으면 32bit 에서는 3GB, 64bit에서는 0(무제한)으로 설정
mammemory-policy noeviction # maxmemory 도달한 경우 eviction 정책 설정
```

#### mammemory-policy 옵션
- noeviction: eviciton 없음, 추가 데이터는 저장되지 않고 에러 발생
- allkeys-lru: 가장 최근에 사용된 키들을 남기고 나머지를 삭제 (LRU: Least Recently Used)
- allkeys-lfu: 가장 빈번하게 사용된 키들을 남기고 나머지를 삭제 (LFU: Least Frequently Used)
- volatile-lru: LRU를 사용하되 expire field가 true로 설정된 항목들 중에서만 삭제
- volatie-lfu: LFU를 사용하되 expire field가 true로 설정된 항목들 중에서만 삭제
- allkeys-random: 랜덤하게 삭제
- volatile-random: expired field가 true로 설정된 항목들 중에서 랜덤하게 삭제
- volatile-ttl: expired field가 true로 설정된 항목들 중에서 짧은 TTL 순으로 삭제

### Redis 성능 측정 (redis-benchmark)
- redis-benchmark 유틸리티를 이용해 Redis 성능을 측정할 수 있음
```bash
# redis-benchmark [-h host] [-p port] [-c clients] [-n requests]
redis-benchmark -c 100 -n 100 -t SET
```
#### Redis 성능에 영향을 미치는 요소들
- network bandwidth & latency: Redis의 throughput은 주로 network에 의해 결정되는 경우가 많음, 운영 환경에 런치하기 전에 배포 환경의 network 대역폭과 실제 throughput을 체크하는 것이 좋다
- CPU: 싱글 스레드로 동작하는 Redis 특성 상 CPU 성능이 중요, 코어 수보다는 큰 cache를 가진 빠른 CPU가 선호됨
- RAM 속도 & 대역폭: 10KB 이하 데이터 항목들에 대해서는 큰 영향이 없음
- 가상화 환경의 영향: VM에서 실행되는 경우 개별적인 영향이 있을 수 있음(non-local disk, 오래된 hypervisor의 느린 fork 구현 등)

### 성능에 영향을 미치는 Redis 설정
- rdbcompression <yes/no>: RDB 파일을 압축할지 여부로, CPU를 절약하고 싶은 경우 no 선택
- rdbchecksum <yes/no>: 사용시 RDB의 안정성을 높일 수 있으나 파일 저장/로드 시에 10% 정도의 성능 저하 있음
- save: RDB 파일 생성 시 시스템 자원이 소모되므로 성능에 영향이 있음

### SLOWLOG 설정
- 수행 시간이 설정한 기준 시간 이상인 쿼리의 로그를 보여줌
- 측정 기준인 수행시간은 I/O 동작을 제외함
```bash
slowlog-log-slower-than 10000 # 로깅되는 기준 시간(microseconds)
slowlog-max-len 128 # 로그 최대 길이
slowlog len # log 개수 확인
slowlog get [count] # slowlog 조회
```
