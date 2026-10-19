package ua.clinic.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.clinic.exception.InvalidDataException;
import ua.clinic.model.Doctor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DoctorLoader {

    private static final Logger log = LoggerFactory.getLogger(DoctorLoader.class);

    private DoctorLoader() {
    }

    public static List<Doctor> loadFromCsv(Path path) {
        List<Doctor> doctors = new ArrayList<>();
        log.info("Starting to load doctors from file: {}", path);

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                log.warn("File is empty: {}", path);
                return doctors;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    Doctor doctor = parseLine(line, lineNumber);
                    doctors.add(doctor);
                    log.info("Line {}: loaded doctor [{}]", lineNumber, doctor.getLicenseNumber());
                } catch (InvalidDataException e) {
                    log.warn("Line {}: skipped — {}", lineNumber, e.getMessage());
                }
            }
        } catch (NoSuchFileException | SecurityException e) {
            log.error("Cannot open file: {} — {}", path, e.getMessage());
        } catch (IOException e) {
            log.error("I/O error while reading file: {}", path, e);
        } finally {
            log.info("Finished loading doctors. Total loaded: {}", doctors.size());
        }

        return doctors;
    }

    public static Doctor parseLine(String line, int lineNumber) throws InvalidDataException {
        String[] parts = line.split(",", -1);
        if (parts.length != 3) {
            throw new InvalidDataException(
                    "line " + lineNumber + ": expected 3 fields, got " + parts.length);
        }

        String licenseNumber = parts[0].trim();
        String name = parts[1].trim();
        String specialization = parts[2].trim();

        try {
            return Doctor.of(licenseNumber, name, specialization);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(
                    "line " + lineNumber + ": " + e.getMessage(), e);
        }
    }
}
