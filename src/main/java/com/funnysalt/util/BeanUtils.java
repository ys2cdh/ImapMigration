package com.funnysalt.util;

import com.funnysalt.bean.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

public class BeanUtils {
    public static Object getBean(String beanName) {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        return applicationContext.getBean(beanName);
    }
}
