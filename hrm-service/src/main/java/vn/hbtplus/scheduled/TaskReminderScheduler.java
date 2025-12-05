package vn.hbtplus.scheduled;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.hbtplus.services.impl.TaskReminderService;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Profile("!prod")
public class TaskReminderScheduler {

    private final TaskReminderService reminderService;

    @Scheduled(cron = "0 */5 17 * * *")
    public void spamUnloggedUsersUntilLogged() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(17, 30))) {
            reminderService.sendReminders();
        }
    }

    @Scheduled(cron = "0 */5 17-23 * * *")
    public void repeatReminder() {
        System.out.println("🔁 Kiểm tra và gửi lại nhắc nhở cho người chưa log task");
        reminderService.resendRemindersIfStillPending();
    }

}