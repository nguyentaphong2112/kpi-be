package vn.hbtplus.services;

import vn.hbtplus.exceptions.BaseAppException;

import java.util.Date;

public interface ParameterService {
    <T> T getConfig(Class<T> className, Date date) throws InstantiationException, IllegalAccessException, BaseAppException;
    <T> T getConfigValue(String configCode, Date date, Class<T> className);
}
