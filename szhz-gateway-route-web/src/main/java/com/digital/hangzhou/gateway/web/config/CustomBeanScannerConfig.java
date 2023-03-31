package com.digital.hangzhou.gateway.web.config;

import com.digital.hangzhou.gateway.common.annotion.AuthTypeAnnotation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * @author lz
 * @date 2023/3/2 14:48
 */
public class CustomBeanScannerConfig implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware, BeanFactoryPostProcessor {

    private ApplicationContext applicationContext;

    private String basePackage;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        CustomPathScanner scanner = new CustomPathScanner(registry);
        scanner.addIncludeFilter(new AnnotationTypeFilter(AuthTypeAnnotation.class));
        scanner.scan(basePackage);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }
}
