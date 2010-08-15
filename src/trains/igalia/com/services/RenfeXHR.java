package trains.igalia.com.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;

public class RenfeXHR {

	private static final String URL = "http://horarios.renfe.com/HIRRenfeWeb/buscar.do?O=%%%ORIGIN%%%&D=%%%DESTINATION%%%&AF=%%%YEAR%%%&MF=%%%MONTH%%%&DF=%%%DATE%%%";

	private final LocalDate TODAY = new org.joda.time.LocalDate();

	private static Map<String, String> cityCodes = new HashMap<String, String>();

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

    private LocalDate fromSpecialDate(String date) {
		if (Dates.DATEBEFOREYESTERDAY.isEqual(date)) {
			return TODAY.minusDays(2);
		} else if (Dates.YESTERDAY.isEqual(date)) {
			return TODAY.minusDays(1);
		} else if (Dates.TODAY.isEqual(date)) {
			return TODAY;
		} else if (Dates.TOMORROW.isEqual(date)) {
			return TODAY.plusDays(1);
		} else if (Dates.DAYAFTERTOMORROW.isEqual(date)) {
			return TODAY.plusDays(2);
		}

		return null;
    }

	public RenfeXHR date(String date) {
		LocalDate result = fromSpecialDate(date);
		if (result != null) {
			this.date = result;
			return this;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			this.date = new org.joda.time.LocalDate(sdf.parse(date));
		} catch (ParseException e) {
			this.date = TODAY;
			e.printStackTrace();
		}
		return this;
	}

	private String getCode(String city) {
		return cityCodes.get(city);
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
			throw new IllegalArgumentException("Origin and destination cannot be null or empty");
		}
		result = result.replace("%%%ORIGIN%%%", encode(originCode.toString()));
		result = result.replace("%%%DESTINATION%%%", encode(destinationCode.toString()));

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
		System.out.println("URL: " + url);
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

	private enum Dates {
		DATEBEFOREYESTERDAY, YESTERDAY, TODAY, TOMORROW, DAYAFTERTOMORROW;

		public boolean isEqual(String str) {
			System.out.println("toStr: " + toString().toLowerCase());
			System.out.println("str: " + str.toLowerCase());
			return toString().toLowerCase().equals(str.toLowerCase());
		}

	}

	static {
		cityCodes.put("A CORUÑA", "31412");
		cityCodes.put("LA CORUÑA", "31412");
		cityCodes.put("ABRANTES", "94707");
		cityCodes.put("ALICANTE", "60911");
		cityCodes.put("ALACANT", "60911");
		cityCodes.put("ALBACETE", "60600");
		cityCodes.put("ALCÁZAR DE SAN JUAN", "60400");
		cityCodes.put("ALGECIRAS", "55020");
		cityCodes.put("ALMERÍA", "56312");
		cityCodes.put("ANTEQUERA", "ANTEQ");
		cityCodes.put("AVILA", "10400");
		cityCodes.put("BADAJOZ", "37606");
		cityCodes.put("BARCELONA", "BARCE");
		cityCodes.put("BARDONECCHIA", "83005");
		cityCodes.put("BENICASSIM", "65318");
		cityCodes.put("BERNA", "85031");
		cityCodes.put("BERN", "85031");
		cityCodes.put("BILBAO", "13200");
		cityCodes.put("ABANDO", "13200");
		cityCodes.put("BLOIS", "87546");
		cityCodes.put("BOBADILLA", "54400");
		cityCodes.put("BURGOS ROSA DE LIMA", "11014");
		cityCodes.put("CÁCERES", "35400");
		cityCodes.put("CÁDIZ", "51405");
		cityCodes.put("CALATAYUD", "70600");
		cityCodes.put("CARTAGENA", "61307");
		cityCodes.put("CASTELLÓN", "65300");
		cityCodes.put("CASTELLÓ", "65300");
		cityCodes.put("CIUDAD REAL", "37200");
		cityCodes.put("CÓRDOBA", "50500");
		cityCodes.put("CUENCA", "66100");
		cityCodes.put("ELDA", "60905");
		cityCodes.put("PETRER", "60905");
		cityCodes.put("ENTRONCAMENTO", "94428");
		cityCodes.put("FERROL", "21010");
		cityCodes.put("FIGUERES", "79309");
		cityCodes.put("FRIBURGO", "85410");
		cityCodes.put("FRIBOURG", "85410");
		cityCodes.put("GANDÍA", "69110");
		cityCodes.put("GINEBRA", "85444");
		cityCodes.put("GENEVE", "85444");
		cityCodes.put("GIJÓN", "GIJON");
		cityCodes.put("GIRONA", "79300");
		cityCodes.put("GRANADA", "05000");
		cityCodes.put("GUADALAJARA", "GUADA");
		cityCodes.put("HENDAYA", "11602");
		cityCodes.put("HUELVA", "43019");
		cityCodes.put("HUESCA", "74200");
		cityCodes.put("IRÚN", "11600");
		cityCodes.put("JAÉN", "03100");
		cityCodes.put("JEREZ DE LA FRONTERA", "51300");
		cityCodes.put("LEÓN", "15100");
		cityCodes.put("LIMOGES", "87034");
		cityCodes.put("LINARES", "50300");
		cityCodes.put("BAEZA", "50300");
		cityCodes.put("LISBOA", "LISBO");
		cityCodes.put("LLEIDA", "78400");
		cityCodes.put("LOGROÑO", "81100");
		cityCodes.put("LORCA", "06006");
		cityCodes.put("SUTULLENA", "06006");
		cityCodes.put("LUGO", "20309");
		cityCodes.put("MADRID", "MADRI");
		cityCodes.put("MÁLAGA MARÍA ZAMBRANO", "54413");
		cityCodes.put("BEIRA", "94401");
		cityCodes.put("MARVAO", "94401");
		cityCodes.put("MEDINA DEL CAMPO", "10500");
		cityCodes.put("MÉRIDA", "37500");
		cityCodes.put("MILÁN", "83111");
		cityCodes.put("MILANO", "83111");
		cityCodes.put("MIRANDA DE EBRO", "11200");
		cityCodes.put("MONFORTE DE LEMOS", "20300");
		cityCodes.put("MONTPELLIER", "87173");
		cityCodes.put("MONZÓN", "78301");
		cityCodes.put("RÍO CINCA", "78301");
		cityCodes.put("MURCIA", "61200");
		cityCodes.put("NAVALMORAL DE LA MATA", "35206");
		cityCodes.put("NOVARA", "83008");
		cityCodes.put("LES AUBRAIS", "87004");
		cityCodes.put("ORLEANS", "87004");
		cityCodes.put("OROPESA", "65304");
		cityCodes.put("ORPESA", "65304");
		cityCodes.put("OURENSE", "22100");
		cityCodes.put("OVIEDO", "15211");
		cityCodes.put("PALENCIA", "14100");
		cityCodes.put("PAMPLONA", "80100");
		cityCodes.put("IRUÑA", "80100");
		cityCodes.put("PARÍS AUSTERLITZ", "87011");
		cityCodes.put("PONFERRADA", "20200");
		cityCodes.put("PONTEVEDRA", "23004");
		cityCodes.put("PORTBOU", "79315");
		cityCodes.put("PUENTE GENIL", "PTE G");
		cityCodes.put("PUERTO SANTA MARÍA", "51400");
		cityCodes.put("PUERTOLLANO", "37300");
		cityCodes.put("REUS", "71400");
		cityCodes.put("SAHAGÚN", "15009");
		cityCodes.put("SALAMANCA", "SALAM");
		cityCodes.put("SAN FERNANDO", "S FER");
		cityCodes.put("DONOSTIA", "11511");
		cityCodes.put("SAN SEBASTIÁN", "11511");
		cityCodes.put("SANTANDER", "14223");
		cityCodes.put("SANTIAGO DE COMPOSTELA", "31400");
		cityCodes.put("SEGOVIA", "SEGOV");
		cityCodes.put("SEVILLA", "51003");
		cityCodes.put("SORIA", "82100");
		cityCodes.put("TARRAGONA", "TARRA");
		cityCodes.put("TERUEL", "67200");
		cityCodes.put("TOLEDO", "92102");
		cityCodes.put("TORINO", "83002");
		cityCodes.put("TURÍN", "83002");
		cityCodes.put("TUDELA DE NAVARRA", "81202");
		cityCodes.put("VALDEPEÑAS", "50102");
		cityCodes.put("VALENCIA", "VALEN");
		cityCodes.put("VALLADOLID CAMPO GRANDE", "10600");
		cityCodes.put("VIGO", "22303");
		cityCodes.put("VILLENA", "60902");
		cityCodes.put("VITORIA", "11208");
		cityCodes.put("GASTEIZ", "11208");
		cityCodes.put("XÀTIVA", "64100");
		cityCodes.put("ZAMORA", "30200");
		cityCodes.put("ZARAGOZA", "ZARAG");
		cityCodes.put("ZURICH", "85200");
	}

}
