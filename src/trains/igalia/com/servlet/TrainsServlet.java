package trains.igalia.com.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import trains.igalia.com.parser.ScheduleParser;
import trains.igalia.com.services.RenfeXHR;

public class TrainsServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(TrainsServlet.class
			.getName());

	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-mm-dd");

	private static final String JSON = "json";

	private static final String XML = "xml";

	private String today() {
		return sdf.format(new Date());
	}

	/**
	 * Uppercase str and remove accents
	 * 
	 * @param str
	 * @return
	 */
	private String normalize(String str) {
		return str != null ? removeAccents(str.toUpperCase()) : null;
	}

	private String removeAccents(String str) {
		String result = str;

		result = result.replaceAll("[áÁ]", "a");
		result = result.replaceAll("[éÉ]", "e");
		result = result.replaceAll("[íÍ]", "i");
		result = result.replaceAll("[óÓ]", "o");
		result = result.replaceAll("[úÚ]", "u");

		return result;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String origin = req.getParameter("origin");
		String destination = req.getParameter("destination");
		String originCode = req.getParameter("originCode");
		String destinationCode = req.getParameter("destinationCode");
		String date = req.getParameter("date"); // Format ISO 8601
		String output = req.getParameter("output");

		origin = normalize(origin);
		destination = normalize(destination);

		if (output == null) {
			output = XML;
		}
		output = output.toLowerCase();

		PrintWriter out = resp.getWriter();

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

		// Prepare query
		RenfeXHR xhr = new RenfeXHR();
		if (origin != null) {
			xhr.origin(origin);
		}
		if (destination != null) {
			xhr.destination(destination);
		}
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

			// Write response
			if (JSON.equals(output)) {
				resp.setContentType("application/json");
				out.println(parser.getResponseJSON());
			}
			if (XML.equals(output)) {
				resp.setContentType("text/xml");
				out.println(parser.getResponseXML());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
