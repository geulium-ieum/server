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
import org.springframework.data.domain.SliceImpl;
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
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        return reminderRepository.findByUserId(user.getId(), pageable).map(ReminderService::toResponse);
    }

    public Slice<ReminderResponse> memorialReminders(UserInfo user, Long memorialId, @ParameterObject Pageable pageable) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        // 존재 확인
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        return reminderRepository.findByUserIdAndMemorialId(user.getId(), memorialId, pageable)
            .map(ReminderService::toResponse);
    }

    @Transactional
    public ReminderResponse create(UserInfo user, Long memorialId, ReminderRequest request) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        Memorial m = memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        Reminder r = new Reminder();
        r.setMemorialId(m.getId());
        r.setUserId(user.getId());
        r.setTitle(request.getTitle());
        r.setReminderDate(request.getReminderDate());
        r.setRepeatRule(request.getRepeatRule());
        r.setDaysBefore(request.getDaysBefore());
        r.setIsActive(request.getIsActive());
        r.setChannel(request.getChannel());
        reminderRepository.save(r);
        return toResponse(r);
    }

    @Transactional
    public void update(UserInfo user, Long reminderId, ReminderUpdateRequest request) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        Reminder r = reminderRepository.findById(reminderId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(r.getUserId())) throw new ApiException(ErrorCode.FORBIDDEN);
        if (request.getTitle() != null) r.setTitle(request.getTitle());
        if (request.getReminderDate() != null) r.setReminderDate(request.getReminderDate());
        if (request.getRepeatRule() != null) r.setRepeatRule(request.getRepeatRule());
        if (request.getDaysBefore() != null) r.setDaysBefore(request.getDaysBefore());
        if (request.getIsActive() != null) r.setIsActive(request.getIsActive());
        if (request.getChannel() != null) r.setChannel(request.getChannel());
        reminderRepository.save(r);
    }

    @Transactional
    public void delete(UserInfo user, Long reminderId) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        Reminder r = reminderRepository.findById(reminderId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(r.getUserId())) throw new ApiException(ErrorCode.FORBIDDEN);
        reminderRepository.delete(r);
    }

    public List<ReminderResponse> upcoming(UserInfo user) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(30);
        List<Reminder> actives = reminderRepository.findByUserIdAndIsActiveTrue(user.getId());
        List<ReminderResponse> results = new ArrayList<>();
        for (Reminder r : actives) {
            LocalDate next = nextOccurrenceDate(r, today);
            if (next != null && (next.isEqual(today) || (next.isAfter(today) && !next.isAfter(until)))) {
                ReminderResponse resp = toResponse(r);
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

    private LocalDate nextOccurrenceDate(Reminder r, LocalDate today) {
        if (Boolean.FALSE.equals(r.getIsActive())) return null;
        int daysBefore = r.getDaysBefore() == null ? 0 : r.getDaysBefore();
        LocalDate base = r.getReminderDate();
        RepeatRule rule = r.getRepeatRule() == null ? RepeatRule.YEARLY : r.getRepeatRule();

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
