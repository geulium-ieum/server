package seg.work.geuliumieum.server.scheduler.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import seg.work.geuliumieum.server.common.entity.Reminder;
import seg.work.geuliumieum.server.common.repository.NotificationRepository;
import seg.work.geuliumieum.server.common.repository.ReminderRepository;
import seg.work.geuliumieum.server.notification.service.NotificationService;
import seg.work.geuliumieum.server.reminder.constant.RepeatRule;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    // 매일 오전 9시: 기일 알림(INAPP) 발송
    @Scheduled(cron = "0 0 9 * * *")
    public void sendAnniversaryReminders() {
        LocalDate today = LocalDate.now();
        List<Reminder> reminders = reminderRepository.findByIsActiveTrue();
        int sent = 0;
        for (Reminder r : reminders) {
            LocalDate due = nextOccurrenceDate(r, today);
            if (due != null && due.isEqual(today)) {
                try {
                    notificationService.publish(
                        r.getUserId(),
                        "REMINDER",
                        r.getTitle(),
                        "다가오는 일정 알림: " + r.getTitle(),
                        "REMINDER",
                        r.getId()
                    );
                    sent++;
                } catch (Exception e) {
                    log.warn("Failed to publish reminder notification. reminderId={} userId={}", r.getId(), r.getUserId(), e);
                }
            }
        }
        log.info("[Scheduler] sendAnniversaryReminders: {} notifications sent", sent);
    }

    // 매주 일요일 03:00: 90일 지난 읽은 알림 삭제
    @Scheduled(cron = "0 0 3 * * SUN")
    public void cleanupOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
        int deleted = notificationRepository.deleteOldRead(cutoff);
        log.info("[Scheduler] cleanupOldNotifications: {} rows deleted (cutoff={})", deleted, cutoff);
    }

    // 매월 1일 02:00: 감사 로그 아카이브 (스키마 미정 → 일단 로그)
    @Scheduled(cron = "0 0 2 1 * *")
    public void archiveAuditLogs() {
        // 스키마/전략 확정 전이므로 가벼운 로그만 남깁니다.
        log.info("[Scheduler] archiveAuditLogs: not implemented (placeholder)");
    }

    // 매일 자정: 일일 리포트 로그
    @Scheduled(cron = "0 0 0 * * *")
    public void generateDailyReport() {
        // 리포트 저장소가 없으므로 간단한 로그만 남깁니다.
        log.info("[Scheduler] generateDailyReport: snapshot logged");
    }

    private LocalDate nextOccurrenceDate(Reminder r, LocalDate today) {
        if (r.getIsActive() != null && !r.getIsActive()) {
            return null;
        }
        int daysBefore = r.getDaysBefore() == null ? 0 : r.getDaysBefore();
        LocalDate base = r.getReminderDate();
        if (base == null) {
            return null;
        }
        RepeatRule rule = r.getRepeatRule() == null ? RepeatRule.YEARLY : r.getRepeatRule();

        LocalDate candidate;
        switch (rule) {
            case NONE -> {
                candidate = base.minusDays(daysBefore);
                return candidate.isEqual(today) ? candidate : null;
            }
            case MONTHLY -> {
                YearMonth ym = YearMonth.from(today);
                int dom = Math.min(base.getDayOfMonth(), ym.lengthOfMonth());
                candidate = LocalDate.of(ym.getYear(), ym.getMonth(), dom).minusDays(daysBefore);
                if (candidate.isBefore(today)) {
                    YearMonth nextYm = ym.plusMonths(1);
                    dom = Math.min(base.getDayOfMonth(), nextYm.lengthOfMonth());
                    candidate = LocalDate.of(nextYm.getYear(), nextYm.getMonth(), dom).minusDays(daysBefore);
                }
                return candidate.isEqual(today) ? candidate : null;
            }
            case YEARLY -> {
                int month = base.getMonthValue();
                int day = base.getDayOfMonth();
                YearMonth ym = YearMonth.of(today.getYear(), month);
                int dom = Math.min(day, ym.lengthOfMonth());
                candidate = LocalDate.of(today.getYear(), month, dom).minusDays(daysBefore);
                if (candidate.isBefore(today)) {
                    YearMonth nextYm = YearMonth.of(today.getYear() + 1, month);
                    dom = Math.min(day, nextYm.lengthOfMonth());
                    candidate = LocalDate.of(today.getYear() + 1, month, dom).minusDays(daysBefore);
                }
                return candidate.isEqual(today) ? candidate : null;
            }
            default -> {
                return null;
            }
        }
    }
}
