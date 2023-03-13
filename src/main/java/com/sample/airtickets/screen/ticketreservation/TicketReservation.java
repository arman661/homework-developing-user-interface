package com.sample.airtickets.screen.ticketreservation;

import com.sample.airtickets.app.TicketService;
import com.sample.airtickets.entity.Airport;
import com.sample.airtickets.entity.Flight;
import com.sample.airtickets.entity.Ticket;
import com.sample.airtickets.screen.purchasedtickets.PurchasedTicketsScreen;
import com.sample.airtickets.screen.ticket.TicketBrowse;
import io.jmix.core.DataManager;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.DialogOutcome;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.*;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@UiController("TicketReservation")
@UiDescriptor("TicketReservation.xml")
public class TicketReservation extends Screen {

    @Autowired
    private EntityComboBox<Airport> to;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private CollectionContainer<Flight> flightsDc;
    @Autowired
    private DateField departureDate;
    @Autowired
    private EntityComboBox<Airport> from;
    @Autowired
    private ProgressBar progressBar;
    @Autowired
    private Notifications notifications;
    @Autowired
    private BackgroundWorker backgroundWorker;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private ScreenBuilders screenBuilders;

    @Subscribe("searchBtn")
    public void onSearchBtnClick(Button.ClickEvent event) {
        if (from.getValue() == null && to.getValue() == null) {
            notifications.create()
                    .withCaption("Please fill at least one filter field")
                    .withType(Notifications.NotificationType.WARNING).show();
            return;
        }
        BackgroundTask<Integer, Void> backgroundTask = new SearchTask();
        progressBar.setVisible(true);
        backgroundWorker.handle(backgroundTask).execute();
    }

    @Install(to = "flightsTable.reserve", subject = "columnGenerator")
    private Component flightsTableReserveColumnGenerator(Flight flight) {
        LinkButton linkButton = uiComponents.create(LinkButton.class);
        linkButton.setCaption("Reserve");
        linkButton.setAction(new BaseAction("reserve") {
            @Override
            public void actionPerform(Component component) {
                dialogs.createInputDialog(TicketReservation.this)
                        .withActions(DialogActions.OK_CANCEL)
                        .withParameters(
                                InputParameter.stringParameter("passengerName").withCaption("Passenger name"),
                                InputParameter.stringParameter("passportNumber").withCaption("Passport number"),
                                InputParameter.stringParameter("telephone").withCaption("Telephone"))
                        .withCloseListener(event -> {
                            if (event.closedWith(DialogOutcome.OK)) {
                                Ticket ticket = dataManager.create(Ticket.class);
                                ticket.setPassengerName(event.getValue("passengerName"));
                                ticket.setPassportNumber(event.getValue("passportNumber"));
                                ticket.setTelephone(event.getValue("telephone"));
                                ticket.setFlight(flight);

                                ticketService.saveTicket(ticket);

                                screenBuilders.screen(TicketReservation.this)
                                        .withScreenClass(PurchasedTicketsScreen.class)
                                        .withOpenMode(OpenMode.NEW_TAB)
                                        .build()
                                        .withId(ticket.getId())
                                        .show();
                            }
                        })
                        .withCaption("Enter reservation details")
                        .show();
            }
        });
        return linkButton;
    }

    @Subscribe("inputDialog")
    public void onInputDialogInputDialogClose(InputDialog.InputDialogCloseEvent event) {
        if (event.closedWith(DialogOutcome.OK)) {
            String passengerName = event.getValue("passengerName");
            String passportNumber = event.getValue("passportNumber");
            String telephone = event.getValue("telephone");

            Ticket ticket = dataManager.create(Ticket.class);
            ticket.setPassengerName(passengerName);
            ticket.setPassportNumber(passportNumber);
            ticket.setTelephone(telephone);

            ticketService.saveTicket(ticket);

            screenBuilders.screen(TicketReservation.this)
                    .withScreenClass(TicketBrowse.class)
                    .withOpenMode(OpenMode.NEW_TAB)
                    .show();
        }
    }

    private class SearchTask extends BackgroundTask<Integer, Void> {
        private List<Flight> flightList;

        protected SearchTask() {
            super(1, TimeUnit.SECONDS, TicketReservation.this);
        }

        @Override
        public Void run(TaskLifeCycle<Integer> taskLifeCycle) {
            LocalDate localDate = departureDate.getValue() == null ? null
                    : OffsetDateTime.parse(departureDate.getValue().toString()).atZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDate();
            flightList = ticketService.searchFlights(from.getValue(), to.getValue(), localDate);
            return null;
        }

        @Override
        public void done(Void result) {
            flightsDc.setItems(flightList);
            progressBar.setVisible(false);
        }
    }
}