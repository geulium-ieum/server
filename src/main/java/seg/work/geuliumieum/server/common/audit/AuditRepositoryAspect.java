package seg.work.geuliumieum.server.common.audit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditRepositoryAspect {

    private final ApplicationEventPublisher eventPublisher;

    // save(S entity)
    @AfterReturning(pointcut = "this(seg.work.geuliumieum.server.common.repository.BaseRepository+) && execution(* save(..))", returning = "ret")
    public void afterSaveOne(JoinPoint jp, Object ret) {
        if (ret == null) {
            return;
        }
        publishForEntity(ret, isCreate(jp.getArgs()[0]));
    }

    // saveAll(Iterable<S> entities)
    @AfterReturning(pointcut = "this(seg.work.geuliumieum.server.common.repository.BaseRepository+) && execution(* saveAll(..))", returning = "ret")
    public void afterSaveAll(JoinPoint jp, Object ret) {
        Object arg = jp.getArgs()[0];
        if (!(arg instanceof Iterable<?> iterable)) {
            return;
        }
        List<Object> entities = new ArrayList<>();
        for (Object o : iterable) {
            entities.add(o);
        }
        for (Object e : entities) {
            publishForEntity(e, isCreate(e));
        }
    }

    // delete, deleteById
    @AfterReturning("this(seg.work.geuliumieum.server.common.repository.BaseRepository+) && (execution(* delete(..)) || execution(* deleteById(..)) || execution(* deleteAll(..)) )")
    public void afterDelete(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object first = args[0];
        if (first instanceof Iterable<?> it) {
            for (Object e : it) {
                publishForEntity(e, AuditAction.DELETE);
            }
        } else {
            publishForEntity(first, AuditAction.DELETE);
        }
    }

    private void publishForEntity(Object entity, boolean create) {
        publishForEntity(entity, create ? AuditAction.CREATE : AuditAction.UPDATE);
    }

    private void publishForEntity(Object entity, AuditAction action) {
        String type = resolveType(entity);
        // Avoid auditing the audit log itself to prevent infinite loops
        if ("AuditLog".equals(type)) {
            if (log.isDebugEnabled()) {
                log.debug("[AUDIT] skip publishing for type=AuditLog to avoid recursion");
            }
            return;
        }
        Long id = extractId(entity);
        AuditEvent evt = AuditEvent.builder()
            .action(action)
            .targetType(type)
            .targetId(id)
            .build();
        try {
            eventPublisher.publishEvent(evt);
            if (log.isDebugEnabled()) {
                log.debug("[AUDIT] published event action={} type={} id={}", action, type, id);
            }
        } catch (Exception e) {
            log.warn("Failed to publish audit event: {} {} id={} cause={}", action, type, id, e.getMessage());
        }
    }

    private boolean isCreate(Object entity) {
        Long id = extractId(entity);
        return id == null || id == 0L;
    }

    private Long extractId(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            Method m = entity.getClass().getMethod("getId");
            Object v = m.invoke(entity);
            if (v instanceof Long l) {
                return l;
            }
            if (v instanceof Number n) {
                return n.longValue();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String resolveType(Object entity) {
        if (entity == null) return "Unknown";
        Class<?> clazz = entity.getClass();
        String simple = clazz.getSimpleName();
        if (simple.contains("$$")) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return superClass.getSimpleName();
            }
        }
        return simple;
    }
}
