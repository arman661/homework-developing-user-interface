package com.sample.airtickets.screen.flight;

import com.sample.airtickets.entity.Airline;
import io.jmix.core.DataManager;
import io.jmix.ui.screen.*;
import com.sample.airtickets.entity.Flight;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@UiController("Flight.edit")
@UiDescriptor("flight-edit.xml")
@EditedEntityContainer("flightDc")
public class FlightEdit extends StandardEditor<Flight> {
    @Autowired
    private DataManager dataManager;

    @Install(to = "metaClassField", subject = "searchExecutor")
    private List<Airline> metaClassFieldSearchExecutor(String searchString, Map<String, Object> searchParams) {
        if(searchString.length() < 2) {
            return Collections.emptyList();
        }
        return dataManager.load(Airline.class)
                .query("e.name like ?1 order by e.name", "(?i)%" + searchString + "%")
                .list();
    }
}