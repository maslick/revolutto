# =revolutto=
money transfer REST API (see [specs](ASSIGNMENT.md))

[![Build Status](https://travis-ci.org/maslick/revolutto.svg?branch=master)](https://travis-ci.org/maslick/revolutto)
[![image size](https://img.shields.io/badge/image%20size-68MB-blue.svg)](https://hub.docker.com/r/maslick/revolutto)
[![Maintainability](https://api.codeclimate.com/v1/badges/f36549893cc694d0f271/maintainability)](https://codeclimate.com/github/maslick/revolutto/maintainability)
[![codecov](https://codecov.io/gh/maslick/revolutto/branch/master/graph/badge.svg)](https://codecov.io/gh/maslick/revolutto)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)


## Features
* Gradle, Kotlin, coroutines :heart:
* Lightweight executable jar: ~13MB
* Web framework: [Ktor](https://ktor.io/)
* Reactive runtime: Netty
* Dependency injection: [Koin](https://insert-koin.io/)
* Production ready: Dockerfile + k8s yaml

## TODO
* account management API (adding new accounts), at the moment accounts are hard-coded for simplicity's sake
* improve Account's balance type (support different currencies, etc.), at the moment ``BigInteger`` is used
* substitute the in-memory data store with a production-ready one (e.g. Hazelcast, Postgres, etc.)
* performance tests (right now a simple [lock-based synchronization](https://github.com/maslick/revolutto/blob/2a56fa0ed175d2cd3994554fdae62b10b3008f05/src/main/kotlin/io/maslick/revolutto/BusinessLogic.kt#L14) is used, other techniques e.g. ``thread confinement`` can be investigated)
* improve the CI/CD pipeline (send artifacts to bintray/nexus, push to docker registry, deploy to k8s cluster)
* add Ingress controller (e.g. Nginx) + YAML definition

## Installation
```
./gradlew clean build
```

## API
* Get balance: ``GET v1/{username}/balance``
* Transfer money: ``POST v1/transfer``

```json
{
  "from": "scrooge",
  "to": "daisy",
  "amount": 100.0
}
```

## Usage
```zsh
$ java -jar build/libs/revolutto-0.1.jar

$ http :8080/v1/daisy/balance | jq
{
  "balance": 100,
  "username": "daisy"
}

$ http :8080/v1/scrooge/balance | jq
{
  "balance": 10000,
  "username": "scrooge"
}

$ http POST :8080/v1/transfer <<< '{"from": "scrooge", "to": "daisy", "amount": 10000.0}' | jq
{
  "success": true,
  "from": "scrooge",
  "to": "daisy",
  "amount": 10000
}

$ http POST :8080/v1/transfer <<< '{"from": "daisy", "to": "scrooge", "amount": 10000.0}' | jq
{
  "success": true,
  "from": "daisy",
  "to": "scrooge",
  "amount": 10000
}
```

## Load test
```zsh
$ echo "POST http://localhost:8080/v1/transfer" | vegeta attack -body payload.json -header="Content-Type: application/json" -rate=2000 -duration=5s | tee results.bin | vegeta report
Requests      [total, rate, throughput]  10000, 2000.21, 2000.15
Duration      [total, attack, wait]      4.999619114s, 4.999462585s, 156.529µs
Latencies     [mean, 50, 95, 99, max]    219.424µs, 175.472µs, 367.947µs, 814.346µs, 6.17098ms
Bytes In      [total, mean]              590000, 59.00
Bytes Out     [total, mean]              570000, 57.00
Success       [ratio]                    100.00%
Status Codes  [code:count]               200:10000
Error Set:

$ cat results.bin | vegeta report -type="hist[0,1ms,5ms,10ms,20ms,50ms,100ms,500ms,1000ms]"
Bucket           #     %       Histogram
[0s,     1ms]    9912  99.12%  ##########################################################################
[1ms,    5ms]    83    0.83%
[5ms,    10ms]   5     0.05%
[10ms,   20ms]   0     0.00%
[20ms,   50ms]   0     0.00%
[50ms,   100ms]  0     0.00%
[100ms,  500ms]  0     0.00%
[500ms,  1s]     0     0.00%
[1s,     +Inf]   0     0.00%

$ cat results.bin | vegeta plot > plot.html
$ open plot.html
```

## Docker
* Uses [Minimalka](https://github.com/maslick/minimalka) lightweight JDK 11 Docker image
* See [Dockerfile](Dockerfile)
```zsh
$ ./gradlew clean build -x test
$ docker build -t revolutto .
$ docker run -d -p 8081:8080 revolutto:latest
$ http `docker-machine ip default`:8081/v1/daisy/balance | jq
$ http `docker-machine ip default`:8081/v1/health
```

## k8s
```zsh
$ k apply -f k8s/deployment.yaml
$ k get all -l project=revolutto
$ k port-forward revolutto-api-5b58b69647-877qd 8083:8082
$ http :8083/v1/health
```

## Links
* [httpie](https://httpie.org/)
* [vegeta](https://github.com/tsenart/vegeta)
* [jq](https://stedolan.github.io/jq/)
