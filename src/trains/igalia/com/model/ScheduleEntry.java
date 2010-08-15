package trains.igalia.com.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.google.appengine.api.datastore.Key;

@Entity(name = "ScheduleEntry")
public class ScheduleEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key id;

	private String code;

	private String departure;

	private String arrive;

	private String length;

	@ManyToOne
	private Train train;

	public ScheduleEntry(String code, String departure, String arrive,
			String length) {
		this.code = code;
		this.departure = departure;
		this.arrive = arrive;
		this.length = length;
	}

	public Key getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDeparture() {
		return departure;
	}

	public void setDeparture(String departure) {
		this.departure = departure;
	}

	public String getArrive() {
		return arrive;
	}

	public void setArrive(String arrive) {
		this.arrive = arrive;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public Train getTrain() {
		return train;
	}

	public void setTrain(Train train) {
		this.train = train;
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
		result.append(String
				.format("{\"code\": \"%s\", \"departure\": \"%s\", \"arrive\": \"%s\", \"length\": \"%s\"}",
						code, departure, arrive, length));
		return result.toString();
	}

}