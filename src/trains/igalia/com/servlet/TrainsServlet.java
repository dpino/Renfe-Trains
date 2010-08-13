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
	
	private static final Logger log = Logger.getLogger(TrainsServlet.class.getName());

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {		
		System.out.println("trainsservlet (POST)");
	}
	
	private String today() {
		return sdf.format(new Date());
	}
	
	private static final String JSON = "json";
	
	private static final String XML = "xml";
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		String origin = req.getParameter("origin");
		String destination = req.getParameter("destination");
		String date = req.getParameter("date");					// Format ISO 8601		
		String output = req.getParameter("output");
		
		if (output == null) {
			output = XML;
		}
		output = output.toLowerCase();		
		
		PrintWriter out = resp.getWriter();
		
		if (origin == null || origin.isEmpty()) {
			resp.setContentType("text/html");
			out.println("Parameter 'origin' is missing");
			return;
		}
		
		if (destination == null || destination.isEmpty()) {
			resp.setContentType("text/html");
			out.println("Parameter 'destination' is missing");
			return;
		}
		
		RenfeXHR xhr = (new RenfeXHR()).origin(origin).destination(destination);
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
		
//		System.out.printf("trainsservlet (GET): %s, %s, %s", origin, destination, date);
	}

}
