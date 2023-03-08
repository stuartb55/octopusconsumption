package com.stuartb55.octopusconsumption;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

@SpringBootApplication
@EnableScheduling
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class OctopusConsumptionApplication {
	private static final String TOKEN = System.getenv("token");
	private static final String BUCKET = System.getenv("bucket_name");
	private static final String ORG = System.getenv("org_name");
	private static final String FIELD_NAME = "value";
	private static final String INFLUX_URL = System.getenv("influx_url");
	private static final String API_KEY = System.getenv("api_key");
	private static final String E_MPAN = System.getenv("electricity_mpan");
	private static final String E_SERIAL_NUMBER = System.getenv("electricity_serial_number");
	private static final String G_MPAN = System.getenv("gas_mpan");
	private static final String G_SERIAL_NUMBER = System.getenv("gas_serial_number");
	private static final String TIME_ZONE = "Europe/London";
	private static final Logger logger = LogManager.getLogger(OctopusConsumptionApplication.class);
	private static final String CONSUMPTION = "consumption";
	private static final Integer LIMIT = 150;
	private static final String ELECTRICITY_URL = "https://api.octopus.energy/v1/electricity-meter-points/" + E_MPAN
			+ "/meters/" + E_SERIAL_NUMBER + "/consumption/?page=1";
	private static final String GAS_URL = "https://api.octopus.energy/v1/gas-meter-points/" + G_MPAN + "/meters/"
			+ G_SERIAL_NUMBER + "/consumption/?page=1";

	public static void main(String[] args) {
		SpringApplication.run(OctopusConsumptionApplication.class, args);

		logger.info("Token: " + TOKEN);
		logger.info("Bucket: " + BUCKET);
		logger.info("Org: " + ORG);
		logger.info("Influx URL: " + INFLUX_URL);
		logger.info("Octopus Username: " + API_KEY);
		logger.info("Electricity MPAN: " + E_MPAN);
		logger.info("Electricity Serial Number: " + E_SERIAL_NUMBER);
		logger.info("Gas MPAN: " + G_MPAN);
		logger.info("Gas Serial Number: " + G_SERIAL_NUMBER);
	}

	@Scheduled(cron = "00 00 10 * * *")
	public void run() {
		try {
			if (TOKEN != null && BUCKET != null && ORG != null && INFLUX_URL != null && API_KEY != null
					&& E_MPAN != null
					&& E_SERIAL_NUMBER != null && G_MPAN != null && G_SERIAL_NUMBER != null) {
				logger.info("Environment Variable Check - Ok");
				saveDataInInflux("electricity", getDataFromOctopus(ELECTRICITY_URL));
				saveDataInInflux("gas", getDataFromOctopus(GAS_URL));
			} else {
				logger.error("!!! Missing Environment Variables - Exiting !!!");
			}
		} catch (Exception e) {
			logger.error("Error in run method", e);
		}
	}

	public JSONArray getDataFromOctopus(String url) {
		RestTemplateBuilder builder = new RestTemplateBuilder();
		RestTemplate restTemplate = builder.basicAuthentication(API_KEY, "").build();
		return new JSONObject(restTemplate.getForObject(url, String.class)).getJSONArray("results");
	}

	public void saveDataInInflux(String type, JSONArray data) {
		logger.info("Running: " + type);
		InfluxDBClient influxDBClient = InfluxDBClientFactory.create(INFLUX_URL, TOKEN.toCharArray(), ORG, BUCKET);
		try {
			IntStream.range(0, data.length()).forEach(i -> {
				Instant instant = LocalDateTime.parse(data.getJSONObject(i).getString("interval_start"),
						DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.of(TIME_ZONE)).toInstant();
				if (data.getJSONObject(i).getFloat(CONSUMPTION) < LIMIT) {
					Point point = Point.measurement("energy").addTag("type", type)
							.addField(FIELD_NAME, data.getJSONObject(i).getFloat(CONSUMPTION))
							.time(instant, WritePrecision.S);
					try (WriteApi writeApi = influxDBClient.makeWriteApi()) {
						writeApi.writePoint(point);
					}
				}
			});
		} catch (Exception e) {
			logger.error("Error saving data to Influx", e);
		} finally {
			influxDBClient.close();
		}
	}
}
