package com.sample.airtickets.screen.purchasedtickets;

import com.google.common.collect.ImmutableMap;
import com.sample.airtickets.entity.Ticket;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.navigation.UrlIdSerializer;
import io.jmix.ui.navigation.UrlParamsChangedEvent;
import io.jmix.ui.navigation.UrlRouting;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Route("purchased-tickets/view")
@UiController("PurchasedTicketsScreen")
@UiDescriptor("purchased-tickets-screen.xml")
public class PurchasedTicketsScreen extends Screen {
    @Autowired
    private CollectionLoader<Ticket> ticketsDl;
    @Autowired
    private UrlRouting urlRouting;

    private UUID id;

    public PurchasedTicketsScreen withId(UUID id) {
        this.id = id;
        ticketsDl.setParameter("id", id);
        ticketsDl.load();

        return this;
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        String serializeId = UrlIdSerializer.serializeId(id);
        urlRouting.replaceState(this, ImmutableMap.of("id", serializeId));
    }

    @Subscribe
    public void onUrlParamsChanged(UrlParamsChangedEvent event) {
        String serializedId = event.getParams().get("id");

        id = (UUID) UrlIdSerializer.deserializeId(UUID.class, serializedId);
        ticketsDl.setParameter("id", id);
        ticketsDl.load();
    }
}