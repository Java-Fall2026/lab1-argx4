package ua.clinic.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.clinic.exception.InvalidDataException;
import ua.clinic.model.Patient;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PatientLoader {

    private static final Logger log = LoggerFactory.getLogger(PatientLoader.class);

    private PatientLoader() {
    }

    public static List<Patient> loadFromCsv(Path path) {
        List<Patient> patients = new ArrayList<>();
        log.info("Starting to load patients from file: {}", path);

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                log.warn("File is empty: {}", path);
                return patients;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    Patient patient = parseLine(line, lineNumber);
                    patients.add(patient);
                    log.info("Line {}: loaded patient [{}]", lineNumber, patient.getNationalId());
                } catch (InvalidDataException e) {
                    log.warn("Line {}: skipped — {}", lineNumber, e.getMessage());
                }
            }
        } catch (NoSuchFileException | SecurityException e) {
            log.error("Cannot open file: {} — {}", path, e.getMessage());
        } catch (IOException e) {
            log.error("I/O error while reading file: {}", path, e);
        } finally {
            log.info("Finished loading patients. Total loaded: {}", patients.size());
        }

        return patients;
    }

    public static Patient parseLine(String line, int lineNumber) throws InvalidDataException {
        String[] parts = line.split(",", -1);
        if (parts.length != 3) {
            throw new InvalidDataException(
                    "line " + lineNumber + ": expected 3 fields, got " + parts.length);
        }

        String nationalId = parts[0].trim();
        String fullName = parts[1].trim();
        String dateStr = parts[2].trim();

        try {
            LocalDate birthDate = LocalDate.parse(dateStr);
            return Patient.of(nationalId, fullName, birthDate);
        } catch (DateTimeParseException e) {
            throw new InvalidDataException(
                    "line " + lineNumber + ": invalid date format '" + dateStr + "'", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(
                    "line " + lineNumber + ": " + e.getMessage(), e);
        }
    }
}
