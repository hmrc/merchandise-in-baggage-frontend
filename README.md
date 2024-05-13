# merchandise-in-baggage-frontend

## Who uses the repo/service

Business travellers carrying commercial goods for both import or export.

## How to start the service locally

`sbt run` This will only start the service as standalone but unable to interact with any other services including Backend and Database

To load all related services:

```bash
sm2 --start MERCHANDISE_IN_BAGGAGE_ALL
```

This will start all the required services to complete a journey

`local url` http://localhost:8281/declare-commercial-goods/start-import
`local url` http://localhost:8281/declare-commercial-goods/start-export

## How to run tests

`./run_all_tests` will run all the tests, including unit, UI and consumer contract tests. The consumer tests will generate
contract files stored in the project root directory folder `pact`
The generated contracts test will then being used from the Backend contract verifier by running the script:
`checkincheck.sh`. However, currently contracts test only runs for local build.

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

## How to enable 'Admin Mode'

This service is built to accept traffic from the `admin.tax.service.gov.uk` domain as well for assisted digital journeys.
The service can detect where the traffic has come from by inspecting the `x-forwarded-host` header, this is done in
`auth/StrideAuthAction.scala` - this will change some content on some of the pages and also alters the payment journey.

When testing locally, you can enable a filter which will add a header to each request to simulate it coming from the admin domain.

This can be done by updating the `adminJourneyFilter.enabled` flag in application.conf to be `true` or alternatively passing it in as
a system property e.g. `sbt run -DadminJourneyFilter.enabled=true`

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
