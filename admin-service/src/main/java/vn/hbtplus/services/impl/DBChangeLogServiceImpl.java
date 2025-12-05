package vn.hbtplus.services.impl;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.hbtplus.repositories.entity.DdlChangeLogEntity;
import vn.hbtplus.repositories.impl.DBChangeLogRepository;
import vn.hbtplus.services.DBChangeLogService;
import vn.hbtplus.utils.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DBChangeLogServiceImpl implements DBChangeLogService {
    private final DBChangeLogRepository dbChangeLogRepository;
    @Value("${app.database.audit-log-path:}")
    private String auditLogPath;

    @Override
    public void auditChangeLog() {
        //Lay thong tin log cuoi cung
        if(Utils.isNullOrEmpty(auditLogPath)) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(auditLogPath))) {
            Date lastEventTime = dbChangeLogRepository.getLastEventTime();
            String line;
            List<DdlChangeLogEntity> listSave = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                DdlChangeLogEntity entity = readChangeLogLine(line, lastEventTime);
                if (entity != null) {
                    listSave.add(entity);
                }
            }
            dbChangeLogRepository.insertBatch(DdlChangeLogEntity.class, listSave, Utils.getUserNameLogin());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private DdlChangeLogEntity readChangeLogLine(String line, Date lastEventTime) {
        var parser = new CSVParserBuilder()
                .withSeparator(',')
                .withQuoteChar('\'') // xử lý quote '
                .build();

        try (CSVReader reader = new CSVReaderBuilder(new StringReader(line))
                .withCSVParser(parser)
                .build()) {
            String str[] = reader.readNext();
            if (str != null && str.length == 10) {
                DdlChangeLogEntity entity = new DdlChangeLogEntity();
                entity.setEventTime(Utils.stringToDate(str[0], "yyyyMMdd HH:mm:ss"));
                entity.setUserhost(str[2]);
                entity.setDbName(str[7]);
                entity.setCommand(str[8]);
                if (lastEventTime == null
                    || lastEventTime.before(entity.getEventTime())) {
                    return entity;
                }
                return null;
            } else {
                log.error("Error parsing CSV line: " + line);
                return null;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }
}
