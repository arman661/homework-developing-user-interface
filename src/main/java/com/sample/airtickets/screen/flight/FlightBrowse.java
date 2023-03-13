package com.sample.airtickets.screen.flight;

import com.sample.airtickets.entity.Flight;
import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.StandardLookup;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

@UiController("Flight.browse")
@UiDescriptor("flight-browse.xml")
@LookupComponent("flightsTable")
public class FlightBrowse extends StandardLookup<Flight> {
}