package trains.igalia.com.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import trains.igalia.com.dao.EMF;
import trains.igalia.com.dao.TrainDAO;
import trains.igalia.com.model.ScheduleEntry;
import trains.igalia.com.model.Train;
import trains.igalia.com.parser.ScheduleParser;
import trains.igalia.com.services.CityCode;
import trains.igalia.com.services.RenfeXHR;

public class TrainsServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(TrainsServlet.class
			.getName());

	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-mm-dd");

	private static final String JSON = "json";

	private static final String XML = "xml";

	private String output;

	private PrintWriter out;

	private Train findInCache(String originCode, String destinationCode,
			LocalDate date) {
		return TrainDAO.find(originCode, destinationCode, date);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		// Process parameters
		String origin = InputParametersProcessor.city(req
				.getParameter("origin"));
		String destination = InputParametersProcessor.city(req
				.getParameter("destination"));
		LocalDate date = InputParametersProcessor
				.date(req.getParameter("date"));
		String originCode = req.getParameter("originCode");
		String destinationCode = req.getParameter("destinationCode");

		output = InputParametersProcessor.output(req.getParameter("output"));
		out = resp.getWriter();

		// Check required parameters
		if (origin == null && originCode == null) {
			resp.setContentType("text/html");
			out.println("Please, specify parameter 'origin' or 'originCode'");
			return;
		}

		if (destination == null && destinationCode == null) {
			resp.setContentType("text/html");
			out.println("Please, specify parameter 'destination' or 'destinationCode'");
			return;
		}

		// Get origin and destination codes
		if (originCode == null) {
			originCode = CityCode.getCode(origin);
		}
		if (destinationCode == null) {
			destinationCode = CityCode.getCode(destination);
		}

		// Check if this query is already in cache
		Train train = findInCache(originCode, destinationCode, date);
		if (train != null) {
			writeResponse(train.getScheduleEntries(), resp);
			return;
		} 

		// Query remote service
		RenfeXHR xhr = new RenfeXHR();
		if (originCode != null) {
			xhr.originCode(originCode);
		}
		if (destinationCode != null) {
			xhr.destinationCode(destinationCode);
		}
		if (date != null) {
			xhr.date(date);
		}

		try {
			xhr.execute();
			ScheduleParser parser = new ScheduleParser();
			parser.parseHTML(xhr.getResponseAsStream());

			List<ScheduleEntry> scheduleEntries = parser.getScheduleEntries();
			if (!scheduleEntries.isEmpty()) {
				train = new Train(xhr, scheduleEntries);
				EMF.persist(train);
			}
			writeResponse(scheduleEntries, resp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void writeResponse(List<ScheduleEntry> entries,
			HttpServletResponse resp) {
		if (JSON.equals(output)) {
			resp.setContentType("application/json");
			out.println(Formatter.toJSON(entries));
		}
		if (XML.equals(output)) {
			resp.setContentType("text/xml");
			out.println(Formatter.toXML(entries));
		}
	}

	private static class Formatter {

		public static String toXML(List<ScheduleEntry> schedule) {
			StringBuffer result = new StringBuffer();

			result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			result.append("<trains>");
			for (int i = 0; i < schedule.size(); i++) {
				result.append(schedule.get(i).toXML());
			}
			result.append("</trains>");
			return result.toString();
		}

		public static String toJSON(List<ScheduleEntry> schedule) {
			List<String> elements = new ArrayList<String>();

			for (int i = 0; i < schedule.size(); i++) {
				elements.add(schedule.get(i).toJSON());
			}
			return String.format("[%s]", StringUtils.join(elements, ","));
		}

	}

	/**
	 * 
	 * @author dpino
	 * 
	 */
	private enum Dates {
		DATEBEFOREYESTERDAY, YESTERDAY, TODAY, TOMORROW, DAYAFTERTOMORROW;

		public boolean isEqual(String str) {
			return toString().toLowerCase().equals(str.toLowerCase());
		}

	}

	/**
	 * 
	 * @author dpino
	 * 
	 */
	public static class InputParametersProcessor {

		private static final SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd");

		private static final String XML = "xml";

		private static final LocalDate TODAY = new LocalDate();

		public static LocalDate date(String date) {
			if (date == null) {
				return new LocalDate();
			}

			LocalDate result = fromSpecialDate(date);
			if (result != null) {
				return result;
			}

			try {
				return new org.joda.time.LocalDate(sdf.parse(date));
			} catch (ParseException e) {
				e.printStackTrace();
				return new LocalDate();
			}
		}

		/**
		 * Uppercase str and remove accents
		 * 
		 * @param str
		 * @return
		 */
		public static String city(String str) {
			return str != null ? removeAccents(str.toUpperCase()) : null;
		}

		private static String removeAccents(String str) {
			String result = str;

			result = result.replaceAll("[áÁ]", "a");
			result = result.replaceAll("[éÉ]", "e");
			result = result.replaceAll("[íÍ]", "i");
			result = result.replaceAll("[óÓ]", "o");
			result = result.replaceAll("[úÚ]", "u");

			return result;
		}

		public static String output(String output) {
			if (output == null) {
				output = XML;
			}
			return output.toLowerCase();
		}

		private static LocalDate fromSpecialDate(String date) {
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

	}

}
