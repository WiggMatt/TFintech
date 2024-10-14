package ru.matthew.service;

import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;
import ru.matthew.exception.InvalidDateRangeException;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

@Service
public class DateService {

    public LocalDate[] determineDates(String dateFrom, String dateTo) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate;
        LocalDate toDate;

        if (!StringUtils.isBlank(dateFrom) && !StringUtils.isBlank(dateTo)) {
            fromDate = LocalDate.parse(dateFrom);
            toDate = LocalDate.parse(dateTo);
        } else {
            fromDate = today.with(ChronoField.DAY_OF_WEEK, 1);
            toDate = today.with(ChronoField.DAY_OF_WEEK, 7);
        }

        if (fromDate.isAfter(toDate)) {
            throw new InvalidDateRangeException("Дата начала не может быть позже даты окончания.");
        }

        return new LocalDate[]{fromDate, toDate};
    }
}
