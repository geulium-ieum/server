package seg.work.geuliumieum.server.reminder.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.entity.Reminder;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.common.repository.ReminderRepository;
import seg.work.geuliumieum.server.reminder.constant.RepeatRule;
import seg.work.geuliumieum.server.reminder.dto.request.ReminderRequest;
import seg.work.geuliumieum.server.reminder.dto.request.ReminderUpdateRequest;
import seg.work.geuliumieum.server.reminder.dto.response.ReminderResponse;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final MemorialRepository memorialRepository;

    public Slice<ReminderResponse> myReminders(UserInfo user, @ParameterObject Pageable pageable) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return reminderRepository.findByUserId(user.getId(), pageable).map(ReminderService::toResponse);
    }

    public Slice<ReminderResponse> memorialReminders(UserInfo user, Long memorialId, @ParameterObject Pageable pageable) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        // 존재 확인
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        return reminderRepository.findByUserIdAndMemorialId(user.getId(), memorialId, pageable).map(ReminderService::toResponse);
    }

    @Transactional
    public ReminderResponse create(UserInfo user, Long memorialId, ReminderRequest request) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Memorial memorial = memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        Reminder reminder = new Reminder();
        reminder.setMemorialId(memorial.getId());
        reminder.setUserId(user.getId());
        reminder.setTitle(request.getTitle());
        reminder.setReminderDate(request.getReminderDate());
        reminder.setRepeatRule(request.getRepeatRule());
        reminder.setDaysBefore(request.getDaysBefore());
        reminder.setIsActive(request.getIsActive());
        reminder.setChannel(request.getChannel());
        reminderRepository.save(reminder);
        return toResponse(reminder);
    }

    @Transactional
    public void update(UserInfo user, Long reminderId, ReminderUpdateRequest request) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Reminder reminder = reminderRepository.findById(reminderId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(reminder.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (request.getTitle() != null) {
            reminder.setTitle(request.getTitle());
        }
        if (request.getReminderDate() != null) {
            reminder.setReminderDate(request.getReminderDate());
        }
        if (request.getRepeatRule() != null) {
            reminder.setRepeatRule(request.getRepeatRule());
        }
        if (request.getDaysBefore() != null) {
            reminder.setDaysBefore(request.getDaysBefore());
        }
        if (request.getIsActive() != null) {
            reminder.setIsActive(request.getIsActive());
        }
        if (request.getChannel() != null) {
            reminder.setChannel(request.getChannel());
        }
        reminderRepository.save(reminder);
    }

    @Transactional
    public void delete(UserInfo user, Long reminderId) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Reminder reminder = reminderRepository.findById(reminderId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(reminder.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        reminderRepository.delete(reminder);
    }

    public List<ReminderResponse> upcoming(UserInfo user) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(30);
        List<Reminder> actives = reminderRepository.findByUserIdAndIsActiveTrue(user.getId());
        List<ReminderResponse> results = new ArrayList<>();
        for (Reminder reminder : actives) {
            LocalDate next = nextOccurrenceDate(reminder, today);
            if (next != null && (next.isEqual(today) || (next.isAfter(today) && !next.isAfter(until)))) {
                ReminderResponse resp = toResponse(reminder);
                // attach nextOccurrence via builder recreation
                results.add(ReminderResponse.builder()
                    .id(resp.getId())
                    .memorialId(resp.getMemorialId())
                    .userId(resp.getUserId())
                    .title(resp.getTitle())
                    .reminderDate(resp.getReminderDate())
                    .repeatRule(resp.getRepeatRule())
                    .daysBefore(resp.getDaysBefore())
                    .isActive(resp.getIsActive())
                    .channel(resp.getChannel())
                    .createdAt(resp.getCreatedAt())
                    .updatedAt(resp.getUpdatedAt())
                    .nextOccurrence(next)
                    .build());
            }
        }
        results.sort(Comparator.comparing(ReminderResponse::getNextOccurrence));
        return results;
    }

    private static ReminderResponse toResponse(Reminder r) {
        return ReminderResponse.from(r);
    }

    private LocalDate nextOccurrenceDate(Reminder reminder, LocalDate today) {
        if (Boolean.FALSE.equals(reminder.getIsActive())) {
            return null;
        }
        int daysBefore = reminder.getDaysBefore() == null ? 0 : reminder.getDaysBefore();
        LocalDate base = reminder.getReminderDate();
        RepeatRule rule = reminder.getRepeatRule() == null ? RepeatRule.YEARLY : reminder.getRepeatRule();

        LocalDate candidate;
        switch (rule) {
            case NONE -> {
                candidate = base.minusDays(daysBefore);
                return candidate.isBefore(today) ? null : candidate;
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
                return candidate;
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
                return candidate;
            }
            default -> {
                return null;
            }
        }
    }
}
