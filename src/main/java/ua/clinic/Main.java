package ua.clinic;

import ua.clinic.model.Appointment;
import ua.clinic.model.Doctor;
import ua.clinic.model.MedicalRecord;
import ua.clinic.model.Patient;
import ua.clinic.util.ClinicUtils;

import java.time.LocalDate;
import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Створення об'єктів (фабрика та конструктор) ===");
        Patient patient = Patient.of("ID-00142", "Іван Коваленко", LocalDate.of(1995, 5, 14));
        Doctor doctor = Doctor.of("DOC-777", "Олена Петрівна", " cardiology ");
        Appointment appointment = new Appointment(
                patient,
                doctor,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(10, 45),
                " scheduled "
        );
        MedicalRecord record = new MedicalRecord("REC-9001", appointment, "Первинний огляд", 550.0);
        System.out.println("Пацієнт (через of): " + patient);
        System.out.println("Лікар (через of): " + doctor);
        System.out.println("Запис (через new): " + appointment);
        System.out.println("Медкарта (через new): " + record);

        System.out.println("\n=== 2. Нормалізація значень ===");
        System.out.println("Спеціалізація після нормалізації: [" + doctor.getSpecialization() + "]");
        System.out.println("Статус запису після нормалізації: [" + appointment.getStatus() + "]");

        System.out.println("\n=== 3. Перехоплення помилок валідації (три різні випадки) ===");
        try {
            Patient.of("   ", "Тест", LocalDate.of(2000, 1, 1));
        } catch (IllegalArgumentException e) {
            System.out.println("Випадок 1 (порожній ID): " + e.getMessage());
        }

        try {
            Patient.of("ID-999", "Тест", LocalDate.now().plusDays(5));
        } catch (IllegalArgumentException e) {
            System.out.println("Випадок 2 (дата в майбутньому): " + e.getMessage());
        }

        try {
            new Appointment(patient, doctor, LocalDate.now(), LocalTime.of(15, 0), LocalTime.of(14, 0), "SCHEDULED");
        } catch (IllegalArgumentException e) {
            System.out.println("Випадок 3 (endTime раніше за startTime): " + e.getMessage());
        }

        System.out.println("\n=== 4. Зміна стану сеттером та перевірка валідації ===");
        appointment.setStatus("COMPLETED");
        System.out.println("Успішно змінено статус на: " + appointment.getStatus());
        try {
            appointment.setStatus("UNKNOWN_STATUS");
        } catch (IllegalArgumentException e) {
            System.out.println("Спроба встановити некоректний статус: " + e.getMessage());
        }

        System.out.println("\n=== 5. Порівняння об'єктів (==, equals, hashCode) ===");
        Patient samePatient = Patient.of("ID-00142", "Петро Сидоренко", LocalDate.of(1980, 1, 1));
        Patient anotherPatient = Patient.of("ID-99999", "Марія Коваль", LocalDate.of(2001, 3, 10));

        System.out.println("samePatient == patient: " + (samePatient == patient));
        System.out.println("samePatient.equals(patient): " + samePatient.equals(patient));
        System.out.println("Хеш-коди збігаються: " + (samePatient.hashCode() == patient.hashCode()));
        System.out.println("anotherPatient.equals(patient): " + anotherPatient.equals(patient));

        System.out.println("\n=== 6. Обчислювані методи ===");
        System.out.println("Тривалість прийому (хв): " + ClinicUtils.durationMinutes(appointment));
        System.out.println("Повних років пацієнта: " + ClinicUtils.patientAge(patient));

        System.out.println("\n=== 7. Вивід усіх сутностей через toString() ===");
        System.out.println(patient);
        System.out.println(doctor);
        System.out.println(appointment);
        System.out.println(record);

        System.out.println("\n=== 8. Демонстрація інкапсуляції ===");
        // ua.clinic.util.ValidationHelper.requireNotNull(null, "test"); // Compilation error: ValidationHelper has package-private access
        System.out.println("Пункт 8: прямий виклик ValidationHelper з Main не компілюється через package-private доступ.");
    }
}