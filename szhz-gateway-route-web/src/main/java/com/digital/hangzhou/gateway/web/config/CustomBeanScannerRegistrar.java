package com.digital.hangzhou.gateway.web.config;

import com.digital.hangzhou.gateway.web.config.annotation.CustomImport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author lz
 * @date 2023/3/2 14:33
 */
public class CustomBeanScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {

    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //获取注解的属性值
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(CustomImport.class.getName()));
        if (annotationAttributes.containsKey("basePackage")){
            String packagePath = annotationAttributes.getString("basePackage");
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
            //注册扫描包的bean
            builder.getBeanDefinition().setBeanClass(CustomBeanScannerConfig.class);
            builder.addPropertyValue("basePackage", packagePath);
            registry.registerBeanDefinition(CustomBeanScannerConfig.class.getName(), builder.getBeanDefinition());
        }
    }
}
