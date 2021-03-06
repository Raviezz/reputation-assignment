package com.reputation.trailing.controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.reputation.trailing.TrailingException;
import com.reputation.trailing.models.NAVConstants;
import com.reputation.trailing.models.NetAssetValue;
import com.reputation.trailing.models.Trailing;
import com.reputation.trailing.validators.IRequestValidator;

/**
 * 
 * @author ravitejab
 *
 */

@CrossOrigin(origins = "http://localhost:3000") // To Avoid CORS error for React app
@RestController
@RequestMapping("api/v1")
public class NetAssetValueController {

	private Logger logger = LoggerFactory.getLogger(NetAssetValueController.class);
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(NAVConstants.API_DATE_FORMATTER, Locale.ENGLISH);

	@Autowired
	@Qualifier("trailingValidator")
	private IRequestValidator<Trailing> iValidator;

	@Value("${mfapi.api.url}")
	private String apiUrl;

	/**
	 * 
	 * @param trailRequest
	 * @return {@link ResponseEntity}
	 */
	@PostMapping("/getTrailingReturns")
	public ResponseEntity<String> getTrailingReturnsForHorizon(@RequestBody final Trailing trailRequest) {

		String apiResponse = null;
		RestTemplate restTemplate = null;
		ObjectNode response = null;
		LocalDate today = LocalDate.now();
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			restTemplate = new RestTemplate();
			response = mapper.createObjectNode();
			logger.info("Validating the request ",trailRequest);
			Trailing trailing = iValidator.validateReqParam(trailRequest);
			String finalUrl = apiUrl.concat("/").concat(trailing.getSchemeNumber()); // prepare final url with scheme no

			try { // get NAV details through api call
				apiResponse = restTemplate.exchange(finalUrl, HttpMethod.GET, getDefaultEntity(), String.class)
						.getBody();
			} catch (Exception e1) {
				logger.error("Exception in mfapi call", e1);
				return new ResponseEntity<String>(e1.getMessage(), HttpStatus.BAD_REQUEST);
			}
			if (apiResponse != null) {
				ArrayNode arrayNode = new ObjectMapper().createArrayNode();
				JsonNode rootNode = mapper.readTree(apiResponse);
				JsonNode internalNode = rootNode.path(NAVConstants.DATA_NODE); // get data field from response

				List<NetAssetValue> dataset = mapper.readerFor(new TypeReference<List<NetAssetValue>>() {
				}).readValue(internalNode); // convert jsonnode to list of objects

				Map<String, NetAssetValue> data = dataset.stream()
						.collect(Collectors.toMap(NetAssetValue::getRecordedDate, p -> p)); // Change list of objects to HashMap of date & NetAssetValue object usong streams 

				// Take initial date , i.e todays date of format dd-MM-yyyy
				String initialPoint = (today.getDayOfMonth() < 10 ? ("0" + today.getDayOfMonth())
						: today.getDayOfMonth()) + "-" + today.getMonthValue() + "-" + (today.getYear());

				for (int period = 0; period < (12 * trailing.getHorizon()); period++) { // now loop 12 months*no of horizons
					while (data.get(initialPoint) == null) { // if current date isn't in dataset then fetch next available date
						initialPoint = previousDateString(initialPoint);
					}
					double endNAV = Double.parseDouble(data.get(initialPoint).getNavValue()); // get its nav value

					int currMonth = today.getMonthValue();

					// get start date with a gap of current & investment period given
					String startDate = (today.getDayOfMonth() < 10 ? ("0" + today.getDayOfMonth())
							: today.getDayOfMonth()) + "-" + (currMonth < 10 ? ("0" + currMonth) : currMonth) + "-"
							+ (today.getYear() - trailing.getInvestmentPeriod()); 

					while (data.get(startDate) == null) { // if null loop to get next available
						startDate = previousDateString(startDate);
					}
					double startNAV = Double.parseDouble(data.get(startDate).getNavValue());

					// Prepare response
					ObjectNode nodeDetails = new ObjectMapper().createObjectNode();
					String month = today.getMonth() + "-" + (today.getYear());
					nodeDetails.put(NAVConstants.MONTH, month); 
					nodeDetails.put(NAVConstants.END_DATE, initialPoint);
					nodeDetails.put(NAVConstants.START_DATE, startDate);
					nodeDetails.put(NAVConstants.RETURNS, calculateNAVReturns(endNAV, startNAV, trailing.getHorizon())); // Calculate trailing returns value
					arrayNode.add(nodeDetails);

					today = today.minusMonths(1);// now minus a month from current month - go back to previous month same day
					
					currMonth = today.getMonthValue();
					
					// set the end date to previous month i.e -> 05-10-2020 to 05-09-2020
					initialPoint = (today.getDayOfMonth() < 10 ? ("0" + today.getDayOfMonth()) : today.getDayOfMonth())
							+ "-" + (currMonth < 10 ? ("0" + currMonth) : currMonth) + "-" + (today.getYear());

				} // end of for loop

				if (!arrayNode.isEmpty()) { // if arrayNode isn't empty retuen response
					response.put(NAVConstants.STATUS, NAVConstants.SUCCESS);
					response.put(NAVConstants.DATA_NODE, arrayNode);
					return new ResponseEntity<>(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response),
							HttpStatus.OK);
				}
				
			}

		} catch (TrailingException requestError) { // Catch validation Userdefined Exception
			response.put(NAVConstants.STATUS, NAVConstants.ERROR);
			response.put(NAVConstants.ERROR_MSG, requestError.getErrorMsg());
			response.put(NAVConstants.ERROR_CODE, requestError.getErrorCode());
			return new ResponseEntity<String>(response.toPrettyString(), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			response.put(NAVConstants.STATUS, NAVConstants.ERROR);
			response.put(NAVConstants.ERROR_MSG, e.getMessage());
		}
		return new ResponseEntity<String>(response.toPrettyString(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * 
	 * @param endNAV
	 * @param startNAV
	 * @param horizon
	 * @return trailing returns
	 * @formula (EndValue/StartValue)^(1/horizon)-1
	 */
	public static double calculateNAVReturns(double endNAV, double startNAV, int horizon) {
		double powVal = (double) 1 / horizon;
		double trailingVal = (double) endNAV / startNAV;
		double sum = (double) Math.pow(trailingVal, powVal) - 1;
		return (double) Math.round(sum * 100) / 100;
	}

	/**
	 * 
	 * @param dateString
	 * @return previousDate (String)
	 */
	public static String previousDateString(String dateString) {
		DateFormat dateFormat = new SimpleDateFormat(NAVConstants.API_DATE_FORMATTER);
		String result = "";
		Date myDate = null;
		try {
			myDate = dateFormat.parse(dateString);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(myDate);
			calendar.add(Calendar.DAY_OF_YEAR, -1); // minus indicate minus one day
			Date previousDate = calendar.getTime();
			result = dateFormat.format(previousDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 * @return defaultEntity
	 */
	public HttpEntity<String> getDefaultEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		return entity;
	}

}
