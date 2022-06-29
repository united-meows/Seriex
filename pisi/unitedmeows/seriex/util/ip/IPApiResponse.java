package pisi.unitedmeows.seriex.util.ip;

public class IPApiResponse {
	private String status;
	private String message;
	private String query;
	private String country;
	private String countryCode;
	private String region;
	private String regionName;
	private String city;
	private String zip;
	private String lat;
	private String lon;
	private String timezone;
	private String isp;
	private String org;
	private String as;

	public String getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public String getQuery() {
		return query;
	}

	public String getCountry() {
		return country;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getRegion() {
		return region;
	}

	public String getRegionName() {
		return regionName;
	}

	public String getCity() {
		return city;
	}

	public String getZip() {
		return zip;
	}

	public String getLat() {
		return lat;
	}

	public String getLon() {
		return lon;
	}

	public String getTimezone() {
		return timezone;
	}

	public String getIsp() {
		return isp;
	}

	public String getOrg() {
		return org;
	}

	public String getAs() {
		return as;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IPApiResponse [status=").append(status).append(", message=").append(message).append(", query=").append(query).append(", country=").append(country).append(", countryCode=")
					.append(countryCode).append(", region=").append(region).append(", regionName=").append(regionName).append(", city=").append(city).append(", zip=").append(zip).append(", lat=")
					.append(lat).append(", lon=").append(lon).append(", timezone=").append(timezone).append(", isp=").append(isp).append(", org=").append(org).append(", as=").append(as).append("]");
		return builder.toString();
	}
}
