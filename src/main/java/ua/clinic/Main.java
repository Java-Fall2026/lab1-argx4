package ua.clinic;

import ua.clinic.model.Appointment;
import ua.clinic.model.AppointmentStatus;
import ua.clinic.model.Doctor;
import ua.clinic.model.MedicalRecord;
import ua.clinic.model.Patient;
import ua.clinic.model.Specialization;
import ua.clinic.util.ClinicUtils;

import java.time.LocalDate;
import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Створення record-об'єкта і звернення до його компонентів ===");
        Patient patient = new Patient("ID-00142", "Іван Коваленко", LocalDate.of(1995, 5, 14));
        Doctor doctor = new Doctor("DOC-777", "Олена Петрівна", Specialization.CARDIOLOGY);
        System.out.println("ID пацієнта через patient.nationalId(): " + patient.nationalId());
        System.out.println("ПІБ пацієнта через patient.fullName(): " + patient.fullName());
        System.out.println("Дата народження через patient.birthDate(): " + patient.birthDate());
        System.out.println("Лікар: " + doctor.name() + " (" + doctor.specialization() + ")");

        System.out.println("\n=== 2. Enum у роботі (values, власне поле, перехоплення valueOf) ===");
        System.out.println("Перелік усіх спеціалізацій та їх коефіцієнтів ціни:");
        for (Specialization spec : Specialization.values()) {
            System.out.println(" - " + spec + " (коефіцієнт: " + spec.getFeeMultiplier() + ")");
        }
        try {
            System.out.println("Спроба спарсити неіснуючий статус через valueOf...");
            AppointmentStatus.valueOf("INVALID_STATUS");
        } catch (IllegalArgumentException e) {
            System.out.println("Успішно перехоплено виняток для невідомого enum: " + e.getMessage());
        }

        System.out.println("\n=== 3. Обидва switch-вирази ===");
        String department = ClinicUtils.getSpecializationDepartment(doctor.specialization());
        System.out.println("Switch 1 (без default, локація відділення): " + department);

        Appointment appointment = new Appointment(
                patient,
                doctor,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(10, 45),
                AppointmentStatus.SCHEDULED);
        String actionScheduled = ClinicUtils.getNextStepInstruction(appointment.getStatus());
        System.out.println("Switch 2 (дія для SCHEDULED): " + actionScheduled);

        appointment.setStatus(AppointmentStatus.COMPLETED);
        String actionCompleted = ClinicUtils.getNextStepInstruction(appointment.getStatus());
        System.out.println("Switch 2 (дія для COMPLETED): " + actionCompleted);

        System.out.println(
                "\n=== 4. Record з невалідними даними -> IllegalArgumentException з компактного конструктора ===");
        try {
            new Patient("   ", "Тестовий Пацієнт", LocalDate.of(2000, 1, 1));
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка при порожньому ID: " + e.getMessage());
        }
        try {
            new Patient("ID-999", "Тестовий Пацієнт", LocalDate.now().plusDays(10));
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка при даті народження в майбутньому: " + e.getMessage());
        }

        System.out.println("\n=== 5. Два однакові record-об'єкти: equals і hashCode (згенеровані компілятором) ===");
        Patient patientCopy = new Patient("ID-00142", "Іван Коваленко", LocalDate.of(1995, 5, 14));
        Patient anotherPatient = new Patient("ID-99999", "Марія Сидоренко", LocalDate.of(2001, 3, 10));

        System.out.println("patient.equals(patientCopy): " + patient.equals(patientCopy));
        System.out.println(
                "patient.hashCode() == patientCopy.hashCode(): " + (patient.hashCode() == patientCopy.hashCode()));
        System.out.println("patient.equals(anotherPatient): " + patient.equals(anotherPatient));

        System.out.println("\n=== 6. Вивід record через згенерований toString() ===");
        MedicalRecord medicalRecord = new MedicalRecord("REC-9001", appointment, "Первинний огляд кардіолога", 850.0);
        System.out.println("Згенерований toString() для Patient: " + patient);
        System.out.println("Згенерований toString() для Doctor: " + doctor);
        System.out.println("Згенерований toString() для MedicalRecord: " + medicalRecord);
        System.out.println("Клас Appointment (звичайний toString): " + appointment);
    }
}
