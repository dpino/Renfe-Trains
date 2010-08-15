package trains.igalia.com.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.joda.time.LocalDate;

import trains.igalia.com.services.RenfeXHR;

@Entity(name = "Train")
public class Train {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String originCode;

	private String destinationCode;

	private Date date;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	List<ScheduleEntry> scheduleEntries;

	public List<ScheduleEntry> getScheduleEntries() {
		return scheduleEntries;
	}

	private Date toDate(LocalDate date) {
		return date.toDateTimeAtStartOfDay().toDate();
	}

	public Train(RenfeXHR xhr, List<ScheduleEntry> scheduleEntries) {
		this.originCode = xhr.getOriginCode();
		this.destinationCode = xhr.getDestinationCode();
		this.date = toDate(xhr.getDate());
		this.scheduleEntries = scheduleEntries;
	}

	public Train(String originCode, String destinationCode, LocalDate date) {
		this.originCode = originCode;
		this.destinationCode = destinationCode;
		this.date = toDate(date);
	}

	public Long getId() {
		return id;
	}

	public String getOriginCode() {
		return originCode;
	}

	public void setOriginCode(String originCode) {
		this.originCode = originCode;
	}

	public String getDestinationCode() {
		return destinationCode;
	}

	public void setDestinationCode(String destinationCode) {
		this.destinationCode = destinationCode;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = toDate(date);
	}

}
