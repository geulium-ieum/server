package seg.work.geuliumieum.server.util;

import io.hypersistence.tsid.TSID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class TsIdUtil {

    public static String tsId() {
        return TSID.fast().toString();
    }

    public static void mdcTraceId() {
        MDC.put("traceId", tsId());
    }
}