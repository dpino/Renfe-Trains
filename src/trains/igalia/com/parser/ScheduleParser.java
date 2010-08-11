package trains.igalia.com.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class ScheduleParser {
		
	List<ScheduleRow> schedule = new ArrayList<ScheduleRow>();
	
	public void parse(String filename) throws Exception {
		File file = new File(filename);
		parseHTML(FileUtils.readFileToString(file));	
	}
	
	public void parseHTML(InputStream is) {
		parseHTML(toString(is));
	}
	
	private String toString(InputStream is) {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(is, writer);
			return writer.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";		
	}
	
	public void parseHTML(String html) {
		List<String> rows = getAllRows("class=\"odd\"",html);
		rows.addAll(getAllRows("class=\"even\"", html));
		
		for (int i = 0; i < rows.size(); i++) {
			List<String> columns = parseRow(rows.get(i));
			schedule.add(new ScheduleRow(columns));
		}		
	}
	
	/**
	 * Parsing using SAX or DOM failed, so I've gone for a regexp parsing instead
	 * 
	 * Returns all <tr> elements in the html document that match pattern, i.e:
	 * <tr class="odd"..., <tr class="even..., etc
	 * 
	 * @param pattern
	 * @param html
	 * @return
	 */
	private List<String> getAllRows(String pattern, String html) {
		List<String> result = new ArrayList<String>();
		int start, end = 0;
		
		while (true) {
			start = html.indexOf("<tr " + pattern + ">", end);
			if (start == -1) {
				break;
			}
			end = html.indexOf("</tr>", start);
			if (end != -1) {
				result.add(html.substring(start, end));
			}
		}
		return result;
	}

	/**
	 * Parses a row, splitting its contents by columns. Only the first 4 columns
	 * are interesting
	 * 
	 * @param row
	 * @return
	 */
	private List<String> parseRow(String row) {
		List<String> columns = new ArrayList<String>();
		String column = "";
		int start = 0, end = 0;		
		
		while (true) {
			end = row.indexOf("</td>", start);
			column = row.substring(start, end);
			start = row.indexOf(">", end) + 1;
								
			columns.add(column);
			if (columns.size() == 4) {
				break;
			}
		}
		return columns;		
	}
	
	public List<ScheduleRow> getSchedule() {
		return schedule;
	}
	
	public String getResponseXML() {
		StringBuffer result = new StringBuffer();

		result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		result.append("<trains>");
		List<ScheduleRow> schedule = getSchedule();
		for (int i = 0; i < schedule.size(); i++) {			
			result.append(schedule.get(i).toXML());			
		}
		result.append("</trains>");
		return result.toString();
	}
	
	public String getResponseJSON() {
		List<String> elements = new ArrayList<String>();
		
		for (int i = 0; i < schedule.size(); i++) {
			elements.add(schedule.get(i).toJSON());
		}
		return String.format("[%s]", StringUtils.join(elements, ","));
	}
	
	/**
	 * 
	 * @author Diego Pino <dpino@igalia.com>
	 *
	 */
	public class ScheduleRow {
		
		private String code;
	
		private String departure;
		
		private String arrive;
		
		private String length;
		
		public ScheduleRow(List<String> columns) {
			code = parseTrainColumn(columns.get(0));
			departure = parseColumn(columns.get(1));
			arrive = parseColumn(columns.get(2));
			length = parseColumn(columns.get(3));
		}
		
		/**
		 * Gets the content between and open tag and a closing tag
		 * 
		 * @param html
		 * @return
		 */
		private String parseColumn(String html) {
			int start = html.indexOf(">") + 1;
			int end = html.indexOf("<", start) - 1;
			
			String result = (end > 0) ? html.substring(start, end) : html
					.substring(start);
			return StringUtils.trim(result);
		}

		/**
		 * The columns that contains the train code is different as the code is
		 * between a <a></a> pair of tags
		 * 
		 * @param html
		 * @return
		 */
		private String parseTrainColumn(String html) {
			int start = html.indexOf("<a");
			start = html.indexOf(">", start) + 1;
			int end = html.indexOf("</a>", start) - 1;
			
			String result = (end > 0) ? html.substring(start, end) : html
					.substring(start);
			return StringUtils.trim(result);			
		}
		
		public String toXML() {
			StringBuffer result = new StringBuffer();
			result.append("<train>");
			result.append("<code>" + code + "</code>");
			result.append("<departure>" + departure + "</departure>");
			result.append("<arrive>" + arrive + "</arrive>");
			result.append("<length>" + length + "</length>");
			result.append("</train>");			
			return result.toString();
		}
		
		public String toJSON() {
			StringBuffer result = new StringBuffer();
			result.append(String.format(
					"{\"code\": \"%s\", \"departure\": \"%s\", \"arrive\": \"%s\", \"length\": \"%s\"}",
					code, departure, arrive, length));
			return result.toString();
		}
	
		public String getTrain() {
			return code;
		}

		public String getDeparture() {
			return departure;
		}

		public String getArrive() {
			return arrive;
		}

		public String getLength() {
			return length;
		}
		
		public void show() {
			System.out.printf("(%s, %s, %s, %s)", code, departure, arrive, length);
		}
		
	}
		
}
