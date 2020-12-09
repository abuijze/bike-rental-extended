package io.axoniq.demo.bikerental.rental.command;

import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.stereotype.Component;

@Component("bikeSnapshotDefinition")
public class BikeSnapshotDefinition extends EventCountSnapshotTriggerDefinition {

    public BikeSnapshotDefinition(Snapshotter snapshotter) {
        super(snapshotter, 10);
    }
}
