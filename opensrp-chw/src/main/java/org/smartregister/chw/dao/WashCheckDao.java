package org.smartregister.chw.dao;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.core.dao.AbstractDao;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mvel2.DataConversion.convert;

public class WashCheckDao extends AbstractDao {

    public static long getLastWashCheckDate(String familyBaseEntityID) {
        String sql = "select eventDate from event where eventType = 'WASH check' and " +
                "baseEntityId = '" + familyBaseEntityID + "' order by eventDate desc limit 1";

        DataMap<Date> dataMap = c -> getCursorValueAsDate(c, "eventDate", getDobDateFormat());
        List<Date> res = AbstractDao.readData(sql, dataMap);
        if (res == null || res.size() == 0)
            return 0;

        return res.get(0).getTime();
    }

    public static List<String> getAllWashCheckVisits(SQLiteDatabase db) {
        String sql = "select visit_id from visits where visit_type = 'WASH check'";

        DataMap<String> dataMap = c -> getCursorValue(c, "visit_id");
        List<String> res = AbstractDao.readData(sql, dataMap, db);
        if (res == null || res.size() == 0)
            return new ArrayList<>();

        return res;
    }

    public static List<EventClient> getWashCheckEvents(SQLiteDatabase db) {
        String sql = "select json from event where eventType = 'WASH check' order by eventDate asc";

        DataMap<EventClient> dataMap = c -> processEventClientCursor(getCursorValue(c, "json"));
        List<EventClient> res = AbstractDao.readData(sql, dataMap, db);
        if (res == null || res.size() == 0)
            return new ArrayList<>();

        return res;
    }

    private static EventClient processEventClientCursor(String jsonEventStr) {
        if (StringUtils.isBlank(jsonEventStr)
                || "{}".equals(jsonEventStr)) { // Skip blank/empty json string
            return null;
        }
        String eventJson = jsonEventStr.replaceAll("'", "");
        Event event = convert(eventJson, Event.class);

        return new EventClient(event, null);
    }


}
