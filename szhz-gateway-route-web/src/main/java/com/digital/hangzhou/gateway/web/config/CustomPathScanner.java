package com.digital.hangzhou.gateway.web.config;

import com.digital.hangzhou.gateway.common.annotion.AuthTypeAnnotation;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.*;
import org.springframework.util.Assert;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author lz
 * @date 2023/3/2 14:54
 */
public class CustomPathScanner extends ClassPathBeanDefinitionScanner {

    private  BeanDefinitionRegistry registry;

    public CustomPathScanner(BeanDefinitionRegistry registry) {
        super(registry);
        this.registry = registry;
    }

    @SneakyThrows
    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Assert.notEmpty(basePackages, "At least one base package must be specified");
        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
            for (BeanDefinition candidate : candidates) {
                Class beanClass = Class.forName(candidate.getBeanClassName());
                String beanName;
                AuthTypeAnnotation annotation = (AuthTypeAnnotation) beanClass.getAnnotation(AuthTypeAnnotation.class);
                if (null == annotation){
                    continue;
                }
                beanName = annotation.value().name();
                if (candidate instanceof AbstractBeanDefinition) {
                    postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
                }
                if (candidate instanceof AnnotatedBeanDefinition) {
                    AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
                }
                if (checkCandidate(beanName, candidate)) {
                    BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
                    beanDefinitions.add(definitionHolder);
                    registerBeanDefinition(definitionHolder, this.registry);
                }
            }
        }
        return beanDefinitions;
    }
}
