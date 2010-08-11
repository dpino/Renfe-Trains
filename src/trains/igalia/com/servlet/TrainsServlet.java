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
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		String origin = req.getParameter("origin");
		String destination = req.getParameter("destination");
		String date = req.getParameter("date");		// Format ISO 8601
		
		RenfeXHR xhr = (new RenfeXHR()).origin(origin).destination(destination);
		if (date != null) {
			xhr.date(date);
		}
		
		try {
			xhr.execute();
			ScheduleParser parser = new ScheduleParser();
			parser.parseHTML(xhr.getResponseAsStream());
			System.out.println("Response: " + parser.getResponseXML());
			
			// Write response
			resp.setContentType("text/xml");
		    PrintWriter out = resp.getWriter();
		    out.println(parser.getResponseXML());
		    			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
//		System.out.printf("trainsservlet (GET): %s, %s, %s", origin, destination, date);
	}

}
