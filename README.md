# merchandise-in-baggage-frontend

This service is for business travellers carrying commercial goods for both import or export.
It has two modes; Public Facing and Assisted Digital i.e. Admin which requires stride login.

## Starting services

To start the required services via [service manager](https://github.com/hmrc/sm2), run the command:

```shell
sm2 --start MERCHANDISE_IN_BAGGAGE_ALL --appendArgs '{"PAYMENTS_PROCESSOR":["-Dmicroservice.services.merchandise-in-baggage.port=8280"]}'
```

To start the journey in public facing mode, browse the urls below for import and export journeys respectively:

* `Import local url`: http://localhost:8281/declare-commercial-goods/start-import
* `Export local url`: http://localhost:8281/declare-commercial-goods/start-export

To start the journey in admin mode, browse the url:

* `Import or Export Choice local url`: http://localhost:8281/declare-commercial-goods/import-export-choice

The stride roles for the admin mode are `tps_payment_taker_call_handler,digital_mib_call_handler`.

## Running Tests

To run unit tests, integration tests, a11y tests, scalastyle, scalafmt, coverage and check dependencies, execute the script:

```bash
./run_all_tests.sh
```

### Running UI Tests

To run the UI tests against a branch with changes made in this repo, follow the steps below:

* Start all required services and stop this service by running the commands:

```shell
sm2 --start MERCHANDISE_IN_BAGGAGE_ALL --appendArgs '{"PAYMENTS_PROCESSOR":["-Dmicroservice.services.merchandise-in-baggage.port=8280"]}'
sm2 --stop MERCHANDISE_IN_BAGGAGE_FRONTEND
```

* Start this service locally with the correct flags by executing the script:

```bash
./run-locally.sh
```

* Run the UI tests which can be found [here](https://github.com/hmrc/merchandise-in-baggage-ui-tests).

OR

To run the UI tests without any changes made in this repo, follow the steps below:

* Start all required services via [service manager](https://github.com/hmrc/sm2) by running the command:

```shell
sm2 --start MERCHANDISE_IN_BAGGAGE_ALL --appendArgs '{"PAYMENTS_PROCESSOR":["-Dmicroservice.services.merchandise-in-baggage.port=8280"]}'
```

* Start this service locally with the correct flags by executing the script:

```bash
./run-locally.sh
```

* Run the UI tests which can be found [here](https://github.com/hmrc/merchandise-in-baggage-ui-tests).

### Running Accessibility Tests

#### Prerequisites
Have node installed on your machine.

#### Execute tests
To run the tests locally, simply run:
```bash
sbt clean A11y/test
```

## Enabling 'Admin Mode'

This service is built to accept traffic from the `admin.tax.service.gov.uk` domain as well for assisted digital journeys.
The service can detect where the traffic has come from by inspecting the `x-forwarded-host` header, this is done in
`auth/StrideAuthAction.scala` - this will change some content on some of the pages and also alters the payment journey.

When testing locally, you can enable a filter which will add a header to each request to simulate it coming from the admin domain.

This can be done by updating the `adminJourneyFilter.enabled` flag in `application.conf` or `./run-locally.sh` to be `true`
or alternatively passing it in as a system property e.g. `sbt run -DadminJourneyFilter.enabled=true`.

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
