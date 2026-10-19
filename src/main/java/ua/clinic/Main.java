package ua.clinic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.clinic.io.DoctorLoader;
import ua.clinic.io.PatientLoader;
import ua.clinic.model.Appointment;
import ua.clinic.model.Doctor;
import ua.clinic.model.MedicalRecord;
import ua.clinic.model.Patient;
import ua.clinic.util.ClinicUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("=== 1. Читання CSV-файлів (коректні + некоректні рядки) ===");
        log.info("Application started");

        List<Patient> patients = PatientLoader.loadFromCsv(Path.of("data/patients.csv"));
        System.out.println("Завантажено пацієнтів: " + patients.size());
        for (Patient p : patients) {
            System.out.println("  " + p);
        }

        List<Doctor> doctors = DoctorLoader.loadFromCsv(Path.of("data/doctors.csv"));
        System.out.println("Завантажено лікарів: " + doctors.size());
        for (Doctor d : doctors) {
            System.out.println("  " + d);
        }

        System.out.println("\n=== 2. Спроба прочитати відсутній файл ===");
        List<Patient> ghost = PatientLoader.loadFromCsv(Path.of("data/nonexistent.csv"));
        System.out.println("Результат з неіснуючого файлу: " + ghost.size() + " записів");

        System.out.println("\n=== 3. Некоректне значення в сеттері (Appointment.setStatus) ===");
        Patient patient = Patient.of("ID-00142", "Іван Коваленко", LocalDate.of(1995, 5, 14));
        Doctor doctor = Doctor.of("DOC-777", "Олена Петрівна", "CARDIOLOGY");
        Appointment appointment = new Appointment(
                patient, doctor,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(10, 45),
                "SCHEDULED"
        );
        System.out.println("Поточний статус: " + appointment.getStatus());

        try {
            appointment.setStatus("INVALID_STATUS");
        } catch (IllegalArgumentException e) {
            log.warn("Setter validation failed: {}", e.getMessage());
            System.out.println("Перехоплено помилку сеттера: " + e.getMessage());
        }

        appointment.setStatus("COMPLETED");
        System.out.println("Статус після коректної зміни: " + appointment.getStatus());

        System.out.println("\n=== 4. Демонстрація логування (INFO, WARN, ERROR) ===");
        log.info("Інформаційне повідомлення: програма працює нормально");
        log.warn("Попередження: тестовий некоректний рядок у файлі");
        log.error("Помилка: демонстрація рівня ERROR");

        System.out.println("\n=== 5. Обчислювані методи ===");
        System.out.println("Тривалість прийому (хв): " + ClinicUtils.durationMinutes(appointment));
        System.out.println("Повних років пацієнта: " + ClinicUtils.patientAge(patient));

        System.out.println("\n=== 6. Вивід об'єктів через toString() ===");
        MedicalRecord record = new MedicalRecord("REC-9001", appointment, "Первинний огляд", 550.0);
        System.out.println(patient);
        System.out.println(doctor);
        System.out.println(appointment);
        System.out.println(record);

        log.info("Application finished");
    }
}