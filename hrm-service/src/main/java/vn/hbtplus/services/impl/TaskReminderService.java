package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.hbtplus.feigns.TelegramFeignClient;
import vn.hbtplus.models.response.LogTasKResponse;
import vn.hbtplus.repositories.impl.LogTaskRepository;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskReminderService {

    private final LogTaskRepository logTaskRepository;
    private final TelegramFeignClient telegramClient;
    @Value("${telegram.bot.token:N/A}")
    private String botToken;

    private final Set<String> pendingEmployees = new HashSet<>();

    public List<LogTasKResponse.UnloggedUser> findUnloggedUsersToday() {
        return logTaskRepository.findUnloggedUsersToday();
    }

    public void sendReminders() {
        List<LogTasKResponse.UnloggedUser> unloggedUsers = findUnloggedUsersToday();

        pendingEmployees.clear();

        for (LogTasKResponse.UnloggedUser user : unloggedUsers) {
            if (user.getTelegramChatId() == null) continue;

            pendingEmployees.add(user.getEmployeeCode());
            String name = user.getFullName() != null ? user.getFullName() : user.getEmployeeCode();
            String text = String.format(
                    "⚠️ Chào %s!\nBạn chưa khai báo task cho ngày %s.\nVui lòng log task trước 17h30 nhé.",
                    name, LocalDate.now()
            );

            try {
                telegramClient.sendMessage(botToken,Map.of(
                        "chat_id", user.getTelegramChatId(),
                        "text", text
                ));
            } catch (Exception e) {
                System.err.println("Lỗi gửi Telegram tới " + user.getEmployeeCode() + ": " + e.getMessage());
            }
        }
    }

    public void resendRemindersIfStillPending() {
        if (pendingEmployees.isEmpty()) return;

        List<LogTasKResponse.UnloggedUser> stillUnlogged = findUnloggedUsersToday();
        Set<String> stillUnloggedCodes = new HashSet<>();
        stillUnlogged.forEach(u -> stillUnloggedCodes.add(u.getEmployeeCode()));

        for (String code : pendingEmployees) {
            if (stillUnloggedCodes.contains(code)) {
                LogTasKResponse.UnloggedUser user = stillUnlogged.stream()
                        .filter(u -> u.getEmployeeCode().equals(code))
                        .findFirst().orElse(null);
                if (user == null || user.getTelegramChatId() == null) continue;
                String name = user.getFullName() != null ? user.getFullName() : user.getEmployeeCode();
                String text = String.format(
                        "⏰ Nhắc lại: %s ơi, bạn vẫn chưa khai báo task hôm nay (%s).",
                        name, LocalDate.now()
                );
                try {
                    telegramClient.sendMessage(botToken,Map.of(
                            "chat_id", user.getTelegramChatId(),
                            "text", text
                    ));
                } catch (Exception e) {
                    System.err.println("Lỗi gửi lại Telegram tới " + code + ": " + e.getMessage());
                }
            }
        }
    }
}