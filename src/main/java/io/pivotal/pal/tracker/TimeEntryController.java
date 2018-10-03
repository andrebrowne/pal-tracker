package io.pivotal.pal.tracker;

import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/time-entries")
public class TimeEntryController {

    private final CounterService counter;
    private final GaugeService gauge;
    private TimeEntryRepository timeEntryRepository;

    public TimeEntryController(
            TimeEntryRepository timeEntryRepository,
            CounterService counter,
            GaugeService gauge
    ) {
        this.timeEntryRepository = timeEntryRepository;
        this.counter = counter;
        this.gauge = gauge;
    }

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TimeEntry> create(@RequestBody TimeEntry timeEntry) {
        TimeEntry createdTimeEntry = timeEntryRepository.create(timeEntry);
        updateMetrics(Optional.of("created"), Optional.of(timeEntryRepository.list().size()));
        return new ResponseEntity<TimeEntry>(createdTimeEntry, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TimeEntry> read(@PathVariable("id") Long id) {
        TimeEntry readTimeEntry = timeEntryRepository.find(id);
        if (readTimeEntry != null) {
            updateMetrics(Optional.of("read"), Optional.empty());
        }
        return generateTimeEntryResponse(readTimeEntry);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TimeEntry> update(@PathVariable("id") Long id, @RequestBody TimeEntry timeEntry) {
        TimeEntry updatedTimeEntry = timeEntryRepository.update(id, timeEntry);
        if (updatedTimeEntry != null) {
            updateMetrics(Optional.of("updated"), Optional.empty());
        }
        return generateTimeEntryResponse(updatedTimeEntry);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<TimeEntry> delete(@PathVariable("id") Long id) {
        timeEntryRepository.delete(id);
        updateMetrics(Optional.of("deleted"), Optional.of(timeEntryRepository.list().size()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TimeEntry>> list() {
        updateMetrics(Optional.of("listed"), Optional.empty());
        return new ResponseEntity<List<TimeEntry>>(timeEntryRepository.list(), HttpStatus.OK);
    }

    private ResponseEntity<TimeEntry> generateTimeEntryResponse(TimeEntry timeEntry) {
        return new ResponseEntity<TimeEntry>(timeEntry, timeEntry == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    private void updateMetrics(Optional<String> counterName, Optional<Integer> gaugeValue) {
        gaugeValue.ifPresent(value -> gauge.submit("timeEntries.count", value));
        counterName.ifPresent(name -> counter.increment("TimeEntry." + name));
    }
}

