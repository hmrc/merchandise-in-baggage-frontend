# merchandise-in-baggage-frontend

## Who uses the repo/service

Business travellers carrying commercial goods for both import or export.

## How to start the service locally

`sbt run` This will only start the service as standalone but unable to interact with any other services including Backend and DataBase

To load all related services:

```bash
sm2 --start MERCHANDISE_IN_BAGGAGE_ALL
```

This will start all the required services to complete a journey

`local url` http://localhost:8281/declare-commercial-goods/start-import
`local url` http://localhost:8281/declare-commercial-goods/start-export

## How to run tests

`./run_all_tests` will run all the tests, including unit, UI and consumer contract tests.

## UI Tests Repo

See the companion Repository https://github.com/hmrc/merchandise-in-baggage-ui-tests for UI tests using this repo.

1: To run all mods services and stop the frontend.

```bash
sm2 --start MERCHANDISE_IN_BAGGAGE_ALL -wait 30 && sm2 --stop MERCHANDISE_IN_BAGGAGE_FRONTEND
```

2: Start this service locally with the correct flags.

```bash
./run-locally.sh
```

3: Run the UI tests

OR

Perform: `sm2 --start MERCHANDISE_IN_BAGGAGE_ALL` to run all mods services, and then separately run the UI tests.
(won't test the branch)

## Accessibility Tests

### Prerequisites
Have node installed on your machine

### Execute tests
To run the tests locally, simply run:
```bash
sbt clean A11y/test
```

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
