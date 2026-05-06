# common

dot env 파일에 DB 연결 정보를 설정.

```text
db_host=localhost:1521
db_username=system
db_password=oracle
```

만약 컴퓨터에 Oracle DB가 설치되어 있지 않다면, Docker를 이용하여 Oracle DB를 실행할 수 있습니다.

```bash
docker run -d --name oracle-xe \
  -p 1521:1521 \
  -e ORACLE_PASSWORD=Oracle1234 \
  container-registry.oracle.com/database/express:21.3.0-xe

```
