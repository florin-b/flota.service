package flota.service.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.OverQueryLimitException;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import flota.service.beans.AdresaOprire;
import flota.service.beans.GoogleContext;
import flota.service.beans.StandardAddress;
import flota.service.enums.EnumJudete;

public class MapUtils {

	private static final Logger logger = LogManager.getLogger(MapUtils.class);
	private static final int MAX_KEYS = 62;

	public static double distanceXtoY(double lat1, double lon1, double lat2, double lon2, String unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344;
		} else if (unit == "N") {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

	public static LatLng geocodeAddress(StandardAddress address) {
		LatLng coordonateGps = new LatLng(0, 0);

		Random rand = new Random(System.currentTimeMillis());
		int value = rand.nextInt((MAX_KEYS - 1) + 1) + 1;

		try {

			Thread.sleep(200);

			StringBuilder strAddress = new StringBuilder();

			if (address.getStreet() != null && !address.getStreet().equals("")) {
				strAddress.append(address.getStreet());
				strAddress.append(",");
			}

			if (address.getNumber() != null && !address.getStreet().equals("")) {
				strAddress.append(address.getNumber());
				strAddress.append(",");
			}

			if (address.getSector() != null && !address.getSector().equals("")) {
				strAddress.append(address.getSector());
				strAddress.append(",");
			}

			if (address.getCity() != null && !address.getCity().equals("")) {
				strAddress.append(address.getCity());
				strAddress.append(",");
			}

			strAddress.append(address.getCountry());
			
			GeoApiContext context = GoogleContext.getContextKey();

			GeocodingResult[] results;

			results = GeocodingApi.geocode(context, strAddress.toString()).await();

			double latitude = 0;
			double longitude = 0;

			if (results.length > 0) {
				latitude = results[0].geometry.location.lat;
				longitude = results[0].geometry.location.lng;
			}

			coordonateGps.lat = latitude;
			coordonateGps.lng = longitude;

		} catch (OverQueryLimitException q) {
			logger.error("geocodeAddress -> " + q.toString() + " , key =" + value);
			MailOperations.sendMail("geocodeAddress -> " + q.toString() + " , key =" + value);

			
		} catch (Exception e) {
			//logger.error("geocodeAddress -> " + Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString() + ", geocodeAddress: , " + address.toString());

		}

		return coordonateGps;
	}

	public static int getDistantaTraseu(List<LatLng> listCoords) {

		int distanta = 0;
		DirectionsRoute[] routes = null;

		try {

			Thread.sleep(200);

			List<String> strList = new ArrayList<>();

			for (LatLng coord : listCoords)
				strList.add(coord.lat + " , " + coord.lng);

			String[] arrayPoints = strList.toArray(new String[strList.size()]);
			
			GeoApiContext context = GoogleContext.getContextKey();

			LatLng start = new LatLng(listCoords.get(0).lat, listCoords.get(0).lng);

			LatLng stop = new LatLng(listCoords.get(listCoords.size() - 1).lat, listCoords.get(listCoords.size() - 1).lng);

			routes = DirectionsApi.newRequest(context).mode(TravelMode.DRIVING).origin(start).destination(stop).waypoints(arrayPoints).mode(TravelMode.DRIVING)
					.optimizeWaypoints(false).await();

			for (int i = 0; i < routes[0].legs.length; i++) {
				distanta += routes[0].legs[i].distance.inMeters;

			}

		} catch (OverQueryLimitException q) {
			//logger.error("getDistantaTraseu -> " + Utils.getStackTrace(q));
			MailOperations.sendMail("getDistantaTraseu -> " + q.toString());
			
		} catch (Exception ex) {
			MailOperations.sendMail("getDistantaTraseu -> " + ex.toString());
			//logger.error(Utils.getStackTrace(ex));
		}

		return distanta / 1000;

	}

	public static int getDistantaTraseuAdrese(List<String> listAdrese) {

		int distanta = 0;
		DirectionsRoute[] routes = null;

		try {

			Thread.sleep(200);

			List<String> strList = new ArrayList<>();

			for (String adresa : listAdrese) {

				strList.add("Romania, " + adresa.split("/")[1] + " , " + adresa.split("/")[0]);
			}

			String[] arrayPoints = strList.toArray(new String[strList.size()]);
			
			GeoApiContext context = GoogleContext.getContextKey();

			String start = "Romania, " + listAdrese.get(0).split("/")[1] + " , " + listAdrese.get(0).split("/")[0];

			String stop = "Romania, " + listAdrese.get(listAdrese.size() - 1).split("/")[1] + " , " + listAdrese.get(listAdrese.size() - 1).split("/")[0];

			routes = DirectionsApi.newRequest(context).mode(TravelMode.DRIVING).origin(start).destination(stop).waypoints(arrayPoints).mode(TravelMode.DRIVING)
					.optimizeWaypoints(false).await();

			for (int i = 0; i < routes[0].legs.length; i++) {
				distanta += routes[0].legs[i].distance.inMeters;

			}

		} catch (OverQueryLimitException q) {
			//logger.error("getDistantaTraseuAdrese -> " + Utils.getStackTrace(q));
			MailOperations.sendMail(q.toString());
		} catch (Exception ex) {
			MailOperations.sendMail(ex.toString());
			//logger.error("getDistantaTraseuAdrese -> " + Utils.getStackTrace(ex));
		}

		return distanta / 1000;

	}

	public static int getDistantaTraseuCoordonate(List<AdresaOprire> listAdrese) {

		int distanta = 0;
		DirectionsRoute[] routes = null;

		Random rand = new Random(System.currentTimeMillis());
		int value = rand.nextInt((MAX_KEYS - 1) + 1) + 1;

		try {

			Thread.sleep(200);

			List<String> strList = new ArrayList<>();

			for (AdresaOprire adresa : listAdrese) {
				strList.add(adresa.getCoordonate().toString());
			}

			String[] arrayPoints = strList.toArray(new String[strList.size()]);
			
			GeoApiContext context = GoogleContext.getContextKey();

			LatLng start = listAdrese.get(0).getCoordonate();

			LatLng stop = listAdrese.get(listAdrese.size() - 1).getCoordonate();

			routes = DirectionsApi.newRequest(context).mode(TravelMode.DRIVING).origin(start).destination(stop).waypoints(arrayPoints).mode(TravelMode.DRIVING)
					.optimizeWaypoints(false).await();

			for (int i = 0; i < routes[0].legs.length; i++) {
				distanta += routes[0].legs[i].distance.inMeters;

			}

		} catch (OverQueryLimitException q) {
			logger.error("getDistantaTraseuCoordonate -> " + Utils.getStackTrace(q) + " key = " + value);
			distanta = -1;
			MailOperations.sendMail(q.toString());
			
		} catch (Exception ex) {
			MailOperations.sendMail(ex.toString());
			logger.error("getDistantaTraseuCoordonate -> " + Utils.getStackTrace(ex));
			distanta = -1;
		}

		return distanta > 0 ? distanta / 1000 : distanta;

	}

	public static List<AdresaOprire> getAdreseCoordonate(List<LatLng> coords) {

		List<AdresaOprire> adrese = new ArrayList<>();


		GeoApiContext context = GoogleContext.getContextKey();

		String adresaStart = "";
		String adresaStop = "";

		String localitate;
		String judet;

		for (int i = 0; i < coords.size(); i++) {

			try {

				Thread.sleep(200);
				GeocodingResult[] results = GeocodingApi.reverseGeocode(context, coords.get(i)).await();

				localitate = "";
				judet = "";

				for (int j = 0; j < results[0].addressComponents.length; j++) {

					AddressComponentType[] adrComponentType = results[0].addressComponents[j].types;

					for (int k = 0; k < adrComponentType.length; k++) {
						if (adrComponentType[k] == AddressComponentType.LOCALITY) {
							localitate = Utils.flattenToAscii(results[0].addressComponents[j].shortName);
							break;
						}

						if (adrComponentType[k] == AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1) {
							judet = Utils.flattenToAscii(results[0].addressComponents[j].shortName);
							break;
						}

					}

					if (!localitate.isEmpty() && !judet.isEmpty()) {

						judet = EnumJudete.getNumeJudet(judet);

						String tempAdr = localitate + " / " + judet;

						if (!judet.isEmpty() && i > 0 && i < coords.size() - 1) {

							if (adrese.isEmpty() || !adrese.get(adrese.size() - 1).getAdresa().equals(tempAdr)) {
								AdresaOprire adresa = new AdresaOprire();
								adresa.setAdresa(localitate + " / " + judet);
								adresa.setCoordonate(coords.get(i));
								adrese.add(adresa);
							}

						}

						if (i == 0)
							adresaStart = localitate + " / " + judet;

						if (i == coords.size() - 1)
							adresaStop = localitate + " / " + judet;

						localitate = "";
						judet = "";
					}

				}

			} catch (OverQueryLimitException q) {
				//logger.error("getAdreseCoordonate -> " + Utils.getStackTrace(q) + " , key = " + value);
				//MailOperations.sendMail("getAdreseCoordonate -> " + Utils.getStackTrace(q) + " , key = " + value);
				
			} catch (Exception e) {
				//logger.error("getAdreseCoordonate -> " + Utils.getStackTrace(e) + " , key = " + value);
				//MailOperations.sendMail("getAdreseCoordonate -> " + e.toString());
			}
		}

		List<AdresaOprire> listAdrese = new ArrayList<>();

		listAdrese.addAll(adrese);

		if (!adresaStart.isEmpty()) {
			if (listAdrese.isEmpty() || !listAdrese.get(0).getAdresa().equals(adresaStart)) {
				AdresaOprire adresa = new AdresaOprire();
				adresa.setAdresa(adresaStart);
				adresa.setCoordonate(coords.get(0));
				listAdrese.add(0, adresa);
			}

		}

		if (!adresaStop.isEmpty() && !listAdrese.get(listAdrese.size() - 1).getAdresa().equals(adresaStop)) {
			AdresaOprire adresa = new AdresaOprire();
			adresa.setAdresa(adresaStop);
			adresa.setCoordonate(coords.get(coords.size() - 1));
			listAdrese.add(listAdrese.size(), adresa);
		}

		return listAdrese;
	}

	private static String getAdresaCoordonate(double lat, double lng) {

		String adresa = "";

		GeoApiContext context = GoogleContext.getContext();

		try {
			GeocodingResult[] results = GeocodingApi.reverseGeocode(context, new LatLng(lat, lng)).await();

			String localitate = Utils.flattenToAscii(results[0].addressComponents[2].shortName);
			String judet = Utils.flattenToAscii(results[0].addressComponents[4].shortName);

			if (judet.equalsIgnoreCase("RO"))
				judet = Utils.flattenToAscii(results[0].addressComponents[3].shortName);

			judet = EnumJudete.getNumeJudet(judet);

			if (!judet.isEmpty())
				adresa = localitate + " / " + judet;

		} catch (Exception e) {
			//logger.error("getAdresaCoordonate -> " + Utils.getStackTrace(e));
			MailOperations.sendMail("getAdresaCoordonate -> " + Utils.getStackTrace(e));
		}

		return adresa;
	}

}
