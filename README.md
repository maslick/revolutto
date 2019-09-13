# =revolutto=
money transfer REST API (see [specs](ASSIGNMENT.md))

[![Build Status](https://travis-ci.org/maslick/revolutto.svg?branch=master)](https://travis-ci.org/maslick/revolutto)
[![Docker image](https://img.shields.io/microbadger/image-size/maslick/revolutto/latest.svg)](https://cloud.docker.com/u/maslick/repository/docker/maslick/revolutto)
[![Maintainability](https://api.codeclimate.com/v1/badges/f36549893cc694d0f271/maintainability)](https://codeclimate.com/github/maslick/revolutto/maintainability)
[![codecov](https://codecov.io/gh/maslick/revolutto/branch/master/graph/badge.svg)](https://codecov.io/gh/maslick/revolutto)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)


## Features
* gradle, Kotlin, coroutines :heart:
* Web framework: [Ktor](https://ktor.io/)
* Reactive runtime: Netty
* Dependency injection: [Koin](https://insert-koin.io/)
* Production ready: Dockerfile + k8s yaml

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
* see [Dockerfile](Dockerfile)
* using [minimalka](https://github.com/maslick/minimalka) docker image
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