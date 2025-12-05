package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hbtplus.annotations.Parameter;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.repositories.ParameterRepository;
import vn.hbtplus.services.ParameterService;
import vn.hbtplus.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParameterServiceImpl implements ParameterService {
    private final ParameterRepository parameterRepository;

    @Override
    public <T> T getConfig(Class<T> className, Date date)
            throws InstantiationException, IllegalAccessException, BaseAppException {
        List<String> codes = new ArrayList<>();
        for (Field field : className.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getAnnotation(Parameter.class) != null) {
                codes.add(field.getAnnotation(Parameter.class).code().toUpperCase());
            }
        }
        List<Map<String, Object>> entities = parameterRepository.getListParameters(codes, date);
        Map<String, String> mapValues = new HashMap<>();
        entities.stream().forEach(item -> {
            mapValues.put(((String) item.get("CONFIG_CODE")).toUpperCase(), (String) item.get("CONFIG_VALUE"));
        });
        //to init T with value mapValues
        T configObject = className.newInstance();

        // Iterate through the fields and set their values from mapValues
        for (Field field : className.getDeclaredFields()) {
            field.setAccessible(true);
            Parameter parameter = field.getAnnotation(Parameter.class);
            if (parameter != null) {
                String value = mapValues.get(parameter.code().toUpperCase());
                if (value == null && !parameter.required()) {
                    value = "";
                }
                if (value != null) {
                    Class<?> fieldType = field.getType();
                    if ("java.lang.String".equalsIgnoreCase(fieldType.getName())) {
                        field.set(configObject, value);
                    } else if ("java.lang.Double".equalsIgnoreCase(fieldType.getName())) {
                        field.set(configObject, Double.valueOf(value));
                    } else if ("java.lang.Long".equalsIgnoreCase(fieldType.getName())) {
                        field.set(configObject, Long.valueOf(value));
                    } else if ("java.lang.Integer".equalsIgnoreCase(fieldType.getName())) {
                        field.set(configObject, Integer.valueOf(value));
                    } else if (fieldType.isAssignableFrom(List.class) && field.getGenericType() instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        Class<?> genericType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        if (genericType.equals(Long.class)) {
                            List<Long> longList = Arrays.stream(value.split(","))
                                    .map(Long::valueOf)
                                    .collect(Collectors.toList());
                            field.set(configObject, longList);
                        } else if (genericType.equals(String.class)) {
                            value = value.replace(" ", "");
                            field.set(configObject, Arrays.asList(value.split(",")));
                        }
                    }
                } else {
                    if (Utils.isNullOrEmpty(parameter.name())) {
                        throw new BaseAppException("Chưa có dữ dữ liệu cấu hình của " + field.getAnnotation(Parameter.class).code());

                    } else {
                        throw new BaseAppException("Chưa có dữ dữ liệu cấu hình của " + field.getAnnotation(Parameter.class).name());

                    }
                }
            }
        }

        return configObject;
    }

    @Override
    public <T> T getConfigValue(String configCode, Date date, Class<T> className) {
        return parameterRepository.getConfigValue(configCode, date, className);
    }
}
