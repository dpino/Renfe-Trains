package trains.igalia.com.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;

import trains.igalia.com.servlet.TrainsServlet;

public class RenfeXHR {

	private static final String URL = "http://horarios.renfe.com/HIRRenfeWeb/buscar.do?O=%%%ORIGIN%%%&D=%%%DESTINATION%%%&AF=%%%YEAR%%%&MF=%%%MONTH%%%&DF=%%%DATE%%%";

	private final LocalDate TODAY = new org.joda.time.LocalDate();

	private InputStream response;

	private String responseAsString;

	private String originCode;

	private String destinationCode;

	private org.joda.time.LocalDate date;

	public RenfeXHR() {

	}

	public RenfeXHR originCode(String originCode) {
		this.originCode = originCode;
		return this;
	}

	public RenfeXHR destinationCode(String destinationCode) {
		this.destinationCode = destinationCode;
		return this;
	}

	public RenfeXHR date(org.joda.time.LocalDate date) {
		this.date = date;
		return this;
	}

	public RenfeXHR origin(String origin) {
		originCode = getCode(origin.toUpperCase());
		if (originCode == null) {
			originCode = origin;
		}
		return this;
	}

	public RenfeXHR destination(String destination) {
		destinationCode = getCode(destination.toUpperCase());
		if (destinationCode == null) {
			destinationCode = destination;
		}
		return this;
	}

	public RenfeXHR date(String date) {
		this.date = TrainsServlet.InputParametersProcessor.date(date);
		return this;
	}

	private String getCode(String city) {
		return CityCode.getCode(city);
	}

	private String encode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}

	private String composeURL() throws IllegalArgumentException {
		String result = URL;

		if (originCode == null || destinationCode == null) {
			throw new IllegalArgumentException(
					"Origin and destination cannot be null or empty");
		}
		result = result.replace("%%%ORIGIN%%%", encode(originCode.toString()));
		result = result.replace("%%%DESTINATION%%%",
				encode(destinationCode.toString()));

		if (date == null) {
			date = new org.joda.time.LocalDate();
		}
		result = result.replace("%%%DATE%%%",
				new Integer(date.getDayOfMonth()).toString());
		result = result.replace("%%%MONTH%%%",
				new Integer(date.getMonthOfYear()).toString());
		result = result.replace("%%%YEAR%%%",
				new Integer(date.getYear()).toString());

		return result;
	}

	private String toString(InputStream inputStream) {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(inputStream, writer);
			return writer.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public void execute() throws Exception {
		URL url = new URL(composeURL());
		response = url.openStream();
		responseAsString = null;
	}

	public InputStream getResponseAsStream() throws Exception {
		return response;
	}

	public String getResponseAsString() throws Exception {
		if (responseAsString == null) {
			responseAsString = toString(response);
		}
		return responseAsString;
	}

	public String getOriginCode() {
		return originCode;
	}

	public String getDestinationCode() {
		return destinationCode;
	}

	public org.joda.time.LocalDate getDate() {
		return date;
	}

}
