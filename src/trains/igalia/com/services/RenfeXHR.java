package trains.igalia.com.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class RenfeXHR {

	// http://horarios.renfe.com/HIRRenfeWeb/buscar.do?O=22303&D=31400&AF=2010&MF=08&DF=11&SF=3&ID=s
	
	private static final String URL = "http://horarios.renfe.com/HIRRenfeWeb/buscar.do?O=%%%ORIGIN%%%&D=%%%DESTINATION%%%&AF=%%%YEAR%%%&MF=%%%MONTH%%%&DF=%%%DATE%%%&SF=3&ID=s";

	private static Map<String, Integer> cityCodes = new HashMap<String, Integer>();
	
	private InputStream response;
	
	private String responseAsString;

	private Integer originCode;

	private Integer destinationCode;
		
	private org.joda.time.LocalDate date;
	
	public RenfeXHR() {
		
	}	
	
	public RenfeXHR originCode(Integer originCode) {
		this.originCode = originCode;
		return this;
	}
	
	public RenfeXHR destinationCode(Integer destinationCode) {
		this.destinationCode = destinationCode;
		return this;
	}

	public RenfeXHR date(org.joda.time.LocalDate date) {
		this.date = date;
		return this;
	}
	
	public RenfeXHR origin(String origin) {
		originCode = Integer.parseInt(origin);
		if (originCode == null) {
			originCode = getCode(origin);
		}
		System.out.println("### origin: " + origin);
		return this;
	}

	public RenfeXHR destination(String destination) {
		destinationCode = Integer.parseInt(destination);
		if (destinationCode == null) {
			destinationCode = getCode(destination);
		}
		System.out.println("### destination: " + destination);
		return this;
	}
	
	public RenfeXHR date(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
		try {
			this.date = new org.joda.time.LocalDate(sdf.parse(date));
		} catch (ParseException e) {
			this.date = new org.joda.time.LocalDate();
			e.printStackTrace();
		}
		return this;
	}
	
	private Integer getCode(String city) {
		return cityCodes.get(city);
	}
		
	private String composeURL() throws IllegalArgumentException {
		String result = URL;
		
		if (originCode == null || destinationCode == null) {
			throw new IllegalArgumentException("Origin and destination cannot be null or empty");
		}
		result = result.replace("%%%ORIGIN%%%", originCode.toString());
		result = result.replace("%%%DESTINATION%%%", destinationCode.toString());
		
		if (date == null) {
			date = new org.joda.time.LocalDate();
		}
		result = result.replace("%%%DATE%%%", new Integer(date.getDayOfMonth()).toString());
		result = result.replace("%%%MONTH%%%", new Integer(date.getMonthOfYear()).toString());
		result = result.replace("%%%YEAR%%%", new Integer(date.getYear()).toString());
		
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
	
}

