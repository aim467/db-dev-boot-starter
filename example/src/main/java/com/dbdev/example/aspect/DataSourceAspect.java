package com.dbdev.example.aspect;

import com.dbdev.example.aspect.DataSource;
import com.dbdev.example.config.RoutingDataSource;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataSourceAspect {

    @Before("@annotation(dataSource)")
    public void before(DataSource dataSource) {
        RoutingDataSource.setDataSource(dataSource.value());
    }

    @After("@annotation(dataSource)")
    public void after(DataSource dataSource) {
        RoutingDataSource.clearDataSource();
    }
}