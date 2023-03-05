# Octopus Consumption

Save Octopus Energy electricity and gas consumption data in InfluxDB.

## Installation

Create a list of environment variables. The Octopus specific ones can be found in the [Octopus Developer Dashboard](https://octopus.energy/dashboard/developer/).

```python
token
bucket_name
org_name
influx_url
api_key
electricity_mpan
electricity_serial_number
gas_mpan
gas_serial_number
```

Run using Docker:

```bash
docker run --env-file envs.list stuartb55/octopusconsumption
```

## InfluxDB

A running [InfluxDB](https://portal.influxdata.com/downloads/) instance is required

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.
