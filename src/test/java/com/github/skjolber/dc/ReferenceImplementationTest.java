package com.github.skjolber.dc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import com.github.skjolber.dc.model.Service;

/**
 * 
 * Compare databinding between implementations
 *
 */

public class ReferenceImplementationTest {

	private static File file = new File("./src/test/resources/rb_rut-aggregated-gtfs.zip");

	@Test
	public void compareImplementations() throws Exception {

		GtfsFeed newDao = createNew(file);
		GtfsRelationalDao referenceDao = createReference(file);
		
		Collection<Transfer> allTransfers = referenceDao.getAllTransfers();
		List<com.github.skjolber.dc.model.Transfer> transfers = newDao.getTransfers();
		
		transfers:
		for(Transfer t : allTransfers) {
			for (com.github.skjolber.dc.model.Transfer transfer : transfers) {
				if(transfer.getTo().getId().equals(t.getToStop().getId().getId())) {
					if(transfer.getFrom().getId().equals(t.getFromStop().getId().getId())) {
						
						if(t.getTransferType() == transfer.getType()) {
							continue transfers;
						}
					}
				}
			}
			fail("Can't find transfer " + t + " in " + transfers.size());
		}		
		
		for (Agency agency : referenceDao.getAllAgencies()) {
			com.github.skjolber.dc.model.Agency agencyForId = newDao.getAgency(agency.getId());
			assertNotNull(agency.getId() + " not in " + newDao.getAgencies(), agencyForId);
			
			assertAgencyEquals(agency, agencyForId);
		}

		for (Route route : referenceDao.getAllRoutes()) {
			com.github.skjolber.dc.model.Route routeForId = newDao.getRoute(route.getId().getId());
			assertNotNull(routeForId);
			
			assertRouteEquals(route, routeForId);
		}

		for (Trip trip : referenceDao.getAllTrips()) {
			com.github.skjolber.dc.model.Trip forId = newDao.getTrip(trip.getId().getId());
			assertNotNull(forId);

			assertTripEquals(trip, forId);
			
			List<StopTime> referenceStopTimesForStop = new ArrayList<>(referenceDao.getStopTimesForTrip(trip));
			
			if(referenceStopTimesForStop.size()  !=  forId.getStopTimes().size()) {

				System.out.println("Reference:");
				for(StopTime ref : referenceStopTimesForStop) {
					System.out.println(ref.getArrivalTime() + " " + ref.getStop().getName() + " " + ref.getStopSequence() + " " + ref.getTrip().getId());
				}

				System.out.println("New:");
				for(com.github.skjolber.dc.model.StopTime ref : forId.getStopTimes()) {
					System.out.println(ref.getArrivalTime() + " " + ref.getStop().getName() + " " + ref.getStopSequence() + " " + ref.getTrip().getId());
				}

				assertEquals(trip.getId().getId(), referenceStopTimesForStop.size(), forId.getStopTimes().size());
			}
			
			for(int i = 0; i < referenceStopTimesForStop.size(); i++) {
				assertStopEquals("Invalid index " + i + "/" + referenceStopTimesForStop.size() + " for trip " + trip, referenceStopTimesForStop.get(i), forId.getStopTimes().get(i));
			}
		}

		for (Stop stop : referenceDao.getAllStops()) {
			com.github.skjolber.dc.model.Stop stopForId = newDao.getStop(stop.getId().getId());
			assertNotNull(stopForId);
			
			assertStopEquals(stop, stopForId);
		}
		
		Set<String> oldServiceIds = new HashSet<>();

		for (AgencyAndId agencyAndId : referenceDao.getAllServiceIds()) {
			oldServiceIds.add(agencyAndId.getId());
		}

		for (Entry<String, Service> entry : newDao.getServices().entrySet()) {
			oldServiceIds.remove(entry.getKey());
		}
		assertTrue(oldServiceIds.isEmpty());
		
		Collection<ServiceCalendarDate> referenceAllCalendarDates = referenceDao.getAllCalendarDates();
		Collection<com.github.skjolber.dc.model.ServiceCalendarDate> newAllCalendarDates = newDao.getCalendarDates();
		assertEquals(newAllCalendarDates.size(), referenceAllCalendarDates.size());
		
		compare:
		for (ServiceCalendarDate referenceServiceCalendarDate : referenceAllCalendarDates) {
			for (com.github.skjolber.dc.model.ServiceCalendarDate newServiceCalendarDate : newAllCalendarDates) {
				if(compare(referenceServiceCalendarDate, newServiceCalendarDate)) {
					continue compare;
				}
			}

			fail(referenceServiceCalendarDate.toString());
		}
	}
	
	private void assertStopEquals(Stop a, com.github.skjolber.dc.model.Stop b) {
		assertEquals(a.getCode(), b.getCode());
		assertEquals(a.getDesc(), b.getDesc());
		assertEquals(Math.toRadians(a.getLat()), b.getLat(), 0);
		assertEquals(Math.toRadians(a.getLon()), b.getLon(), 0);
		assertEquals(a.getLocationType(), b.getLocationType());
		assertEquals(a.getName(), b.getName());
		if(a.getParentStation() != null) {
			assertEquals(a.getParentStation(), b.getParentStation().getId());
		} else {
			// assertNull(b.getParentStation());
		}
		assertEquals(a.getTimezone(), b.getTimezone());
		assertEquals(a.getUrl(), b.getUrl());
		assertEquals(a.getVehicleType(), b.getVehicleType());
		assertEquals(a.getWheelchairBoarding(), b.getWheelchairBoarding());
		assertEquals(a.getZoneId(), b.getZoneId());
	}

	private void assertTripEquals(Trip a, com.github.skjolber.dc.model.Trip b) {
		assertEquals(a.getTripShortName(), b.getShortName());
		assertEquals(a.getBikesAllowed(), b.getBikesAllowed());
		assertEquals(a.getDirectionId(), b.getDirectionId());
		assertEquals(a.getTripHeadsign(), b.getHeadsign());
		assertEquals(a.getWheelchairAccessible(), b.getWheelchairAccessible());
		if(a.getShapeId() != null) {
			assertEquals(a.getShapeId().getId(), b.getShapeId().getId());
		}
	}

	private void assertRouteEquals(Route a, com.github.skjolber.dc.model.Route b) {
		assertEquals(a.getShortName(), b.getShortName());
		assertEquals(a.getBikesAllowed(), b.getBikesAllowed());
		assertEquals(a.getBrandingUrl(), b.getBrandingUrl());
		if(a.getColor() == null) {
			assertEquals("000000", getHex(b.getColor()));
		} else {
			assertEquals(a.getColor(), getHex(b.getColor()));
		}
		assertEquals(a.getDesc(), b.getDesc());
		assertEquals(a.getLongName(), b.getLongName());
		assertEquals(a.getSortOrder(), b.getSortOrder());
		if(a.getColor() == null) {
			assertEquals("000000", getHex(b.getTextColor()));
		} else {
			assertEquals(a.getTextColor(), getHex(b.getTextColor()));
		}
		assertEquals(a.getType(), b.getType());
		assertEquals(a.getUrl(), b.getUrl());
	}

	private String getHex(int color) {
		
		String str = Integer.toHexString(color);
		while(str.length() != 6) {
			str = '0' + str;
		}
		return str;
	}

	private void assertAgencyEquals(Agency a, com.github.skjolber.dc.model.Agency b) {
		assertEquals(a.getName(), b.getName());
		assertEquals(a.getBrandingUrl(), b.getBrandingUrl());
		assertEquals(a.getFareUrl(), b.getFareUrl());
		assertEquals(a.getLang(), b.getLang());
		assertEquals(a.getPhone(), b.getPhone());
		assertEquals(a.getTimezone(), b.getTimezone());
		assertEquals(a.getUrl(), b.getUrl());
	}

	private void assertStopEquals(String message, StopTime a, com.github.skjolber.dc.model.StopTime b) {
		assertEquals(message, a.getStopSequence(), b.getStopSequence());
		assertEquals(message, a.getDepartureTime(), b.getDepartureTime());
		assertEquals(message, a.getArrivalTime(), b.getArrivalTime());
		assertEquals(message, a.getDropOffType(), b.getDropOffType());
		assertEquals(message, a.getFarePeriodId(), b.getFarePeriodId());
		assertEquals(message, a.getPickupType(), b.getPickupType());
		assertEquals(message, a.getRouteShortName(), b.getRouteShortName());
		assertEquals(message, a.getShapeDistTraveled(), b.getShapeDistTraveled(), 0.1);
		assertEquals(message, a.getStopHeadsign(), b.getStopHeadsign());
		assertEquals(message, a.getTimepoint(), b.getTimepoint());
	}

	private static boolean compare(ServiceCalendarDate oldServiceCalendarDate, com.github.skjolber.dc.model.ServiceCalendarDate newServiceCalendarDate) {
		if(oldServiceCalendarDate.getExceptionType() != newServiceCalendarDate.getExceptionType()) {
			return false;
		}
		
		if(!oldServiceCalendarDate.getServiceId().getId().equals(newServiceCalendarDate.getService().getId())) {
			return false;
		}

		ServiceDate date = oldServiceCalendarDate.getDate();
		if(date.getDay() != newServiceCalendarDate.getDate().getDayOfMonth()) {
			return false;
		}
		if(date.getMonth() != newServiceCalendarDate.getDate().getMonthValue()) {
			return false;
		}
		if(date.getYear() != newServiceCalendarDate.getDate().getYear()) {
			return false;
		}

		return true;
	}

	public static GtfsRelationalDao createReference(File file) throws IOException {
		long time = System.currentTimeMillis();
        GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();

        GtfsReader reader = new GtfsReader();
        reader.setInputLocation(file);
        reader.setEntityStore(dao);

        reader.setDefaultAgencyId("RB");

        reader.run();
        
        System.out.println("Loaded in " + (System.currentTimeMillis() - time) + " millis");
        
        return dao;
	}

	public static GtfsFeed createNew(File file) throws Exception {
		long time = System.currentTimeMillis();
		GtfsFeed build = GtfsFeedBuilder.newInstance().withFile(file).build();
        System.out.println("Loaded in " + (System.currentTimeMillis() - time) + " millis");
        return build;
	}
	
	public static void main(String[] args) throws Exception {
		
		for(int i = 0; i < 100; i++) {
		
			GtfsFeed newDao = createNew(file);
			if(newDao == null) {
				throw new RuntimeException();
			}
			/*
			GtfsRelationalDao referenceDao = createReference(file);
			if(referenceDao == null) {
				throw new RuntimeException();
			}
			*/
		}
	}

}
